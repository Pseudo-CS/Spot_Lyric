import requests
from bs4 import BeautifulSoup, Comment, NavigableString
import logging

logger = logging.getLogger(__name__)

MAX_CONTENT_LENGTH = 800 * 1024  # 800KB safety cap
MAX_CLEAN_LENGTH = 15000         # 15K char truncation for Gemini prompt

USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/91.0.4472.124 Safari/537.36"
)

NOISE_SELECTORS = [
    "script", "style", "nav", "header", "footer", "aside",
    "form", "button", "input", "select", "svg",
    "[class*=related]", "[id*=related]",
    "[class*=sidebar]", "[id*=sidebar]",
    "[class*=widget]", "[id*=widget]",
    "[class*=comment]", "[id*=comment]",
    "[class*=reply]", "[id*=reply]",
    "[class*=share]", "[id*=share]",
    "[class*=social]", "[id*=social]",
    "[class*=adsbygoogle]", "ins.adsbygoogle",
    "[class*=advertisement]", "[id*=advertisement]"
]

def fetch_page_content(url, timeout=20):
    """Fetch HTML content from URL with browser User-Agent."""
    try:
        headers = {
            "User-Agent": USER_AGENT,
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.5"
        }
        response = requests.get(url, headers=headers, timeout=timeout)
        if not response.ok:
            logger.error(f"HTTP error: {response.status_code}")
            return None
            
        body = response.text
        if len(body) > MAX_CONTENT_LENGTH:
            logger.warning(f"Body exceeds safety cap, truncating to {MAX_CONTENT_LENGTH}")
            body = body[:MAX_CONTENT_LENGTH]
        return body
    except Exception as e:
        logger.error(f"Error fetching page content from {url}: {e}")
        return None

def extract_text_with_line_breaks(element):
    """
    Extract text while preserving line breaks from <br> and block level elements.
    Matches extractTextWithLineBreaks in Android.
    """
    if not element:
        return ""
        
    parts = []
    block_tags = {"p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "li", "tr", "td", "section", "article", "main"}
    
    def traverse(node):
        if isinstance(node, Comment):
            return
            
        # br tag
        if hasattr(node, "name") and node.name == "br":
            parts.append("\n")
            return
            
        # block elements
        if hasattr(node, "name") and node.name in block_tags:
            if parts and parts[-1] != "\n":
                parts.append("\n")
            for child in node.children:
                traverse(child)
            if parts and parts[-1] != "\n":
                parts.append("\n")
            return
            
        # inline elements / general nodes
        if hasattr(node, "children"):
            for child in node.children:
                traverse(child)
        else:
            text = str(node)
            # Preserve spacing between words if it has printable characters
            if text.strip() or " " in text:
                parts.append(text)
                
    traverse(element)
    
    # Strip double spaces and fix trailing lines
    text = "".join(parts).strip()
    return text

def clean_and_truncate(raw):
    """Normalize whitespace and restrict clean content length."""
    if not raw:
        return ""
    lines = [line.strip() for line in raw.split("\n")]
    filtered = [line for line in lines if line and len(line) > 2]
    result = "\n".join(filtered)
    
    if len(result) > MAX_CLEAN_LENGTH:
        return result[:MAX_CLEAN_LENGTH] + "..."
    return result

def clean_html_for_gemini(html_content):
    """Clean HTML removing noise tags and extract content containing lyrics."""
    if not html_content or not html_content.strip():
        return None
        
    try:
        soup = BeautifulSoup(html_content, "html.parser")
        
        # Remove noise elements
        skip_tags = {"html", "body", "article", "main"}
        for selector in NOISE_SELECTORS:
            for element in soup.select(selector):
                if element.name not in skip_tags:
                    element.decompose()
                    
        # 1. Genius-specific check
        genius_containers = soup.select("[data-lyrics-container=true]")
        if genius_containers:
            text = "\n\n".join(extract_text_with_line_breaks(c) for c in genius_containers)
            if len(text.strip()) > 50:
                return clean_and_truncate(text)
                
        # 2. Main fallback
        main = soup.select_one("main") or soup.select_one("article") or soup.body or soup
        fallback_text = extract_text_with_line_breaks(main)
        return clean_and_truncate(fallback_text)
    except Exception as e:
        logger.error(f"Error in clean_html_for_gemini: {e}")
        return None

# --- Stage 1: Domain Parsers ---
def try_genius_parser(soup):
    containers = soup.select("[data-lyrics-container=true]")
    if not containers:
        return None
    text = "\n\n".join(extract_text_with_line_breaks(c) for c in containers).strip()
    if len(text) < 50:
        return None
    return {
        "original_lyrics": clean_and_truncate(text),
        "translated_lyrics": "",
        "stage": "Domain Parser",
        "confidence": 0.95
    }

def try_lyrics_raag_parser(soup):
    columns = soup.select(".wps-column-inner")
    if not columns:
        return None
        
    original_spans = []
    translated_spans = []
    
    for col in columns:
        original_spans.extend(col.select("span.original"))
        translated_spans.extend(col.select("span.translated"))
        
    original = "\n".join(s.get_text() for s in original_spans).strip()
    translated = "\n".join(s.get_text() for s in translated_spans).strip()
    
    if len(original) < 50:
        return None
    return {
        "original_lyrics": original,
        "translated_lyrics": translated,
        "stage": "Domain Parser",
        "confidence": 0.95
    }

def try_lyrics_wiz_parser(soup):
    grid = soup.select_one(".grid")
    if not grid:
        return None
    children = list(grid.children)
    col1 = None
    for c in children:
        if c.name:
            col1 = c
            break
    if not col1:
        return None
        
    original_divs = col1.select("div.bg-gray-100")
    translated_divs = col1.select("div.bg-blue-100")
    
    if not original_divs:
        return None
        
    original_text = "\n\n".join(extract_text_with_line_breaks(d) for d in original_divs).strip()
    translated_text = "\n\n".join(extract_text_with_line_breaks(d) for d in translated_divs).strip()
    
    if len(original_text) < 50:
        return None
    return {
        "original_lyrics": original_text,
        "translated_lyrics": translated_text,
        "stage": "Domain Parser",
        "confidence": 0.95
    }

def try_lyrics_decoder_parser(soup):
    article = soup.select_one("article")
    if not article:
        return None
        
    original_container = None
    divs = article.select("div")
    for d in divs:
        cls = d.get("class", [])
        cls_str = " ".join(cls) if isinstance(cls, list) else str(cls)
        if "md:text-center" in cls_str and "text-lg" in cls_str:
            original_container = d
            break
            
    if not original_container:
        original_container = article.select_one("div.md\\:text-center")
        
    if not original_container:
        return None
        
    original_text = extract_text_with_line_breaks(original_container).strip()
    if len(original_text) < 50:
        return None
        
    heading = None
    for h2 in article.select("h2"):
        t = h2.get_text()
        if "meaning in english" in t.lower() or "translation" in t.lower():
            heading = h2
            break
            
    translated_text = ""
    if heading:
        translated_paragraphs = []
        next_sibling = heading.next_sibling
        while next_sibling:
            if hasattr(next_sibling, "name") and next_sibling.name is not None:
                tag = next_sibling.name.lower()
                if tag in ["h2", "h3", "div", "footer"]:
                    break
                if tag == "p":
                    p_text = extract_text_with_line_breaks(next_sibling).strip()
                    if p_text:
                        translated_paragraphs.append(p_text)
            next_sibling = next_sibling.next_sibling
        translated_text = "\n\n".join(translated_paragraphs)
        
    return {
        "original_lyrics": original_text,
        "translated_lyrics": translated_text,
        "stage": "Domain Parser",
        "confidence": 0.95
    }

def try_bolly_meaning_parser(soup):
    post_body = soup.select_one(".post-body") or soup.select_one(".entry-content")
    if not post_body:
        return None
        
    original_stanzas = []
    translated_stanzas = []
    
    current_translated = []
    has_lyrics_started = False
    
    # Iterate direct children of post_body
    for node in post_body.contents:
        if hasattr(node, "name") and node.name is not None and node.name.lower() == "b":
            trans_str = "".join(current_translated).strip()
            if trans_str:
                clean_trans = "\n".join(line.strip() for line in trans_str.split("\n") if line.strip())
                if clean_trans:
                    translated_stanzas.append(clean_trans)
                current_translated = []
                
            bold_text = node.get_text().strip()
            if (node.select("a") or 
                bold_text.lower().startswith("check") or 
                "birth of a song" in bold_text.lower() or 
                "love gulzar" in bold_text.lower()):
                continue
                
            if len(bold_text) > 5:
                has_lyrics_started = True
                clean_original = extract_text_with_line_breaks(node).strip()
                original_stanzas.append(clean_original)
                
        elif has_lyrics_started:
            if isinstance(node, NavigableString):
                text = str(node).strip()
                if text:
                    current_translated.append(text + "\n")
            elif hasattr(node, "name") and node.name is not None:
                tag = node.name.lower()
                text = node.get_text().strip()
                
                if tag == "a" and (text.lower().startswith("check") or "gulzar" in text.lower()):
                    break
                    
                if tag == "br":
                    current_translated.append("\n")
                elif tag in ["div", "p"]:
                    block_text = extract_text_with_line_breaks(node).strip()
                    if block_text:
                        current_translated.append(block_text + "\n")
                else:
                    inline_text = node.get_text().strip()
                    if inline_text:
                        current_translated.append(inline_text + "\n")
                        
    # Flush remaining translation
    final_trans = "".join(current_translated).strip()
    if final_trans:
        clean_trans = "\n".join(line.strip() for line in final_trans.split("\n") if line.strip())
        if clean_trans:
            translated_stanzas.append(clean_trans)
            
    original_text = "\n\n".join(original_stanzas)
    translated_text = "\n\n".join(translated_stanzas)
    
    if len(original_text) < 50:
        return None
        
    return {
        "original_lyrics": original_text,
        "translated_lyrics": translated_text,
        "stage": "Domain Parser",
        "confidence": 0.95
    }

def try_domain_parser(html, url):
    """
    Stage 1: Custom parsers for known domains.
    Returns result dict, or None if domain not matched / parsing failed.
    """
    try:
        soup = BeautifulSoup(html, "html.parser")
        low_url = url.lower()
        if "genius.com" in low_url:
            return try_genius_parser(soup)
        elif "lyricsraag.com" in low_url:
            return try_lyrics_raag_parser(soup)
        elif "lyricswiz.com" in low_url:
            return try_lyrics_wiz_parser(soup)
        elif "lyricsdecoder.com" in low_url:
            return try_lyrics_decoder_parser(soup)
        elif "bollymeaning.com" in low_url or "bollywoodmeaning.com" in low_url:
            return try_bolly_meaning_parser(soup)
        return None
    except Exception as e:
        logger.error(f"Domain parser failed for {url}: {e}")
        return None

# --- Stage 2: Heuristics ---
def try_heuristics(html):
    """
    Stage 2: Heuristics based on common class/id patterns for lyrics.
    """
    try:
        soup = BeautifulSoup(html, "html.parser")
        
        # Strip noise first
        skip_tags = {"html", "body", "article", "main"}
        for selector in NOISE_SELECTORS:
            for element in soup.select(selector):
                if element.name not in skip_tags:
                    element.decompose()
                    
        lyric_selectors = [
            "[class*=lyric-content]", "[id*=lyric-content]",
            "[class*=song-lyrics]", "[id*=song-lyrics]",
            ".lyrics", "#lyrics",
            "[class*=entry-content]", "[id*=entry-content]"
        ]
        
        for selector in lyric_selectors:
            containers = soup.select(selector)
            if containers:
                texts = [extract_text_with_line_breaks(c) for c in containers]
                texts = [t for t in texts if len(t) > 50]
                if texts:
                    combined = "\n\n".join(texts)
                    return {
                        "original_lyrics": clean_and_truncate(combined),
                        "translated_lyrics": "",
                        "stage": "Heuristics",
                        "confidence": 0.70
                    }
        return None
    except Exception as e:
        logger.error(f"Heuristics parser failed: {e}")
        return None
