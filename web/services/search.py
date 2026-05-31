import re
import urllib.parse
from concurrent.futures import ThreadPoolExecutor
from serpapi import GoogleSearch
from services import database

STOPWORDS = {
    "the", "a", "an",
    "in", "on", "at", "to", "by", "from", "for", "of", "with",
    "and", "or", "but",
    "is", "are", "was", "were", "as",
    "lyrics", "translation", "remix", "cover", "live", "version", "featuring", "feat", "ft",
    "english", "hindi", "spanish", "arabic", "korean", "japanese", "chinese", "mandarin", "romanian"
}

TIER1_DOMAINS = {"genius.com", "lyricsraag.com", "musixmatch.com"}
TIER2_DOMAINS = {"azlyrics.com", "lyricfinder.org", "letrastraducidas.org"}
TIER3_DOMAINS = {"songlyrics.com", "metrolyrics.com", "lyrics007.com"}

def domain_score(url):
    """Calculate baseline domain score matching Android Tier ranking."""
    lower = url.lower()
    if any(d in lower for d in TIER1_DOMAINS):
        return 1.0
    elif any(d in lower for d in TIER2_DOMAINS):
        return 0.5
    elif any(d in lower for d in TIER3_DOMAINS):
        return -0.5
    return 0.0

def normalise_url(url):
    """Normalize URL for deduplication."""
    try:
        lower = url.lower().strip()
        for prefix in ["https://", "http://", "www."]:
            if lower.startswith(prefix):
                lower = lower[len(prefix):]
        # Strip query and fragment
        lower = lower.split("?")[0].split("#")[0]
        return lower.rstrip("/")
    except Exception:
        return url

# --- Relevance Filter ---
def tokenize(text):
    """Tokenize and filter text, removing stopwords."""
    if not text:
        return set()
    cleaned = re.sub(r"[^a-z0-9\s]", " ", text.lower())
    tokens = cleaned.split()
    return {t for t in tokens if len(t) > 1 and t not in STOPWORDS}

def calculate_relevance_score(query_tokens, result_tokens):
    """Calculate overlap/Jaccard similarity coefficient."""
    if not query_tokens:
        return 1.0
    intersection = len(query_tokens.intersection(result_tokens))
    union = len(query_tokens.union(result_tokens))
    return float(intersection / union) if union > 0 else 1.0

def is_relevant(song_name, artist_name, result_title, result_snippet=None, threshold=0.2):
    """Determine if a search result is relevant to the queried track."""
    song_tokens = tokenize(song_name)
    artist_tokens = tokenize(artist_name)
    query_tokens = song_tokens.union(artist_tokens)

    if not query_tokens:
        return True

    title_tokens = tokenize(result_title)
    snippet_tokens = tokenize(result_snippet or "")
    result_tokens = title_tokens.union(snippet_tokens)

    # At least one title token must match the song name (if song name has tokens)
    has_title_match = not song_tokens or any(t in result_tokens for t in song_tokens)
    if not has_title_match:
        return False

    score = calculate_relevance_score(query_tokens, result_tokens)
    return score >= threshold

# --- Scoring ---
def composite_score(url, title, snippet, query_tokens, preferred_domains):
    """Calculate the final ranking score for a result."""
    # Preferred domain check (+2.0 boost)
    preferred_boost = 0.0
    if any(d in url.lower() for d in preferred_domains):
        preferred_boost = 2.0

    domain = domain_score(url)
    
    result_tokens = tokenize(title).union(tokenize(snippet or ""))
    relevance = calculate_relevance_score(query_tokens, result_tokens)

    return preferred_boost + domain + relevance

# --- SerpAPI Search ---
def run_single_query(query, api_key, song_name, artist_name, preferred_domains, is_filter_enabled, threshold):
    """Fetch search results for a single query from SerpAPI."""
    params = {
        "engine": "google",
        "q": query,
        "api_key": api_key,
        "num": 10,
        "gl": "in",
        "hl": "en",
    }
    try:
        database.increment_request_count("serpapi")
        search = GoogleSearch(params)
        results = search.get_dict()

        if "error" in results or "organic_results" not in results:
            return []

        sources = []
        for result in results["organic_results"][:10]:
            url = result.get("link", "")
            title = result.get("title", "")
            if not title:
                title = url.split("/")[-1].replace("-", " ").title() if url else "Unknown Source"
            snippet = result.get("snippet", "")

            # Apply relevance filtering
            if is_filter_enabled:
                if not is_relevant(song_name, artist_name, title, snippet, threshold):
                    continue

            is_pref = any(domain in url.lower() for domain in preferred_domains)
            sources.append({
                "title": title,
                "url": url,
                "snippet": snippet,
                "is_preferred": is_pref
            })
        return sources
    except Exception as e:
        print(f"Error in SerpAPI query '{query}': {e}")
        return []

def search_lyrics_sources(song_name, artist_name, translation_only=True):
    """
    Search and rank lyrics sources using dual concurrent SerpAPI queries.
    """
    api_key = database.get_setting("serpapi_custom_api_key")
    if not api_key:
        # Check environment variables as fallback
        import os
        api_key = os.getenv("SERPAPI_KEY")
        if not api_key:
            return []

    # Get settings from DB
    is_filter_enabled = int(database.get_setting("relevance_filter_enabled", 1)) == 1
    threshold = float(database.get_setting("relevance_threshold", 0.2))

    # Fetch enabled preferred domains
    preferred_sources = database.get_all_preferred_sources()
    preferred_domains = [s["domain"].lower() for s in preferred_sources if s["enabled"] == 1]

    # Dual query setup
    if translation_only:
        query_a = f"{song_name} {artist_name} lyrics translation"
        query_b = f"{song_name} {artist_name} lyrics english translation"
    else:
        query_a = f"{song_name} {artist_name} lyrics"
        query_b = f"{song_name} {artist_name} lyrics translation"

    # Fire queries in parallel threads
    with ThreadPoolExecutor(max_workers=2) as executor:
        future_a = executor.submit(
            run_single_query, query_a, api_key, song_name, artist_name,
            preferred_domains, is_filter_enabled, threshold
        )
        future_b = executor.submit(
            run_single_query, query_b, api_key, song_name, artist_name,
            preferred_domains, is_filter_enabled, threshold
        )
        results_a = future_a.result()
        results_b = future_b.result()

    # Merge and deduplicate by normalized URL
    seen = set()
    merged = []
    for source in (results_a + results_b):
        norm = normalise_url(source["url"])
        if norm not in seen:
            seen.add(norm)
            merged.append(source)

    # Score and sort descending
    query_tokens = tokenize(song_name).union(tokenize(artist_name))
    
    # Add score to result dicts for display/ranking
    for source in merged:
        score = composite_score(source["url"], source["title"], source["snippet"], query_tokens, preferred_domains)
        source["score"] = score

    merged.sort(key=lambda x: x["score"], reverse=True)
    return merged
