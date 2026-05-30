"""
Gemini API utilities for lyrics and translation extraction
"""
import os
import json
import logging
import requests
from datetime import datetime
from dotenv import load_dotenv

try:
    from bs4 import BeautifulSoup
except ImportError:
    BeautifulSoup = None
    print("Warning: beautifulsoup4 not installed. Install with: pip install beautifulsoup4")

try:
    import trafilatura
except ImportError:
    trafilatura = None
    print("Warning: trafilatura not installed. Install with: pip install trafilatura")

try:
    import google.generativeai as genai
except ImportError:
    genai = None
    print("Warning: google-generativeai not installed. Install with: pip install google-generativeai")

# Set up logging
logger = logging.getLogger(__name__)

# Load environment variables
load_dotenv()

# Configure Gemini API
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if GEMINI_API_KEY and genai:
    genai.configure(api_key=GEMINI_API_KEY)
    client = genai
else:
    client = None


def extract_page_content(url, timeout=10):
    """
    Extract the HTML content from a given URL with memory safeguards
    Returns the raw HTML content
    """
    try:
        headers = {
            "User-Agent": (
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/91.0.4472.124 Safari/537.36"
            )
        }
        response = requests.get(url, headers=headers, timeout=timeout)
        response.raise_for_status()

        content_length = len(response.text)
        if content_length > 200000:  # 200KB limit
            print(f"Page content too large: {content_length} characters, truncating")
            return response.text[:100000]  # Return first 100KB only

        return response.text
    except requests.RequestException as e:
        print(f"Error fetching page content from {url}: {e}")
        return None


def clean_html_for_gemini(html_content):
    """
    Extract main content from HTML using Trafilatura and clean it for Gemini processing
    Returns clean text content optimized for lyrics extraction
    """
    if not html_content:
        return None

    try:
        if trafilatura:
            logger.info("Using Trafilatura for content extraction")
            main_content = trafilatura.extract(
                html_content,
                include_comments=False,
                include_tables=True,
                include_formatting=True,
                output_format="txt",
            )

            if main_content and len(main_content.strip()) > 100:
                lines = []
                for line in main_content.split("\n"):
                    line = line.strip()
                    if line and len(line) > 2:
                        lines.append(line)

                cleaned_content = "\n".join(lines)
                logger.info(f"Trafilatura extracted {len(cleaned_content)} characters")

                if len(cleaned_content) > 15000:
                    print(f"Content too long ({len(cleaned_content)} chars), truncating to 15000")
                    cleaned_content = cleaned_content[:15000] + "..."

                return cleaned_content

        if BeautifulSoup:
            logger.info("Falling back to BeautifulSoup for content extraction")
            soup = BeautifulSoup(html_content, "html.parser")

            for element in soup(
                ["script", "style", "nav", "header", "footer", "aside", "form", "button", "input", "select"]
            ):
                element.decompose()

            main_content = soup.find(["main", "article", "div"]) or soup
            cleaned_content = main_content.get_text(separator="\n", strip=True)

            lines = [line.strip() for line in cleaned_content.split("\n") if line.strip()]
            cleaned_content = "\n".join(lines)

            if len(cleaned_content) > 15000:
                print(f"Content too long ({len(cleaned_content)} chars), truncating to 15000")
                cleaned_content = cleaned_content[:15000] + "..."

            logger.info(f"BeautifulSoup extracted {len(cleaned_content)} characters")
            return cleaned_content

        logger.warning("No HTML parsing library available, using raw content")
        if len(html_content) > 15000:
            return html_content[:15000] + "..."
        return html_content

    except Exception as e:
        logger.error(f"Error cleaning HTML content: {e}")
        return None


def create_lyrics_extraction_prompt(song_name, artist_name):
    """
    Create a detailed prompt for Gemini to extract lyrics and translations
    """
    return f"""You are an expert lyrics extraction assistant. Your task is to analyze webpage content and extract lyrics and translations for the song "{song_name}" by {artist_name}.

INSTRUCTIONS:
1. Carefully examine the provided webpage content
2. Identify and extract the original lyrics (in the original language)
3. Identify and extract any translation of the lyrics (usually in English)
4. Determine the languages of both the original and translated lyrics
5. Assess the quality and completeness of the extraction

CRITICAL REQUIREMENTS:
- Extract ONLY the actual song lyrics - no comments, annotations, or website navigation text
- If lyrics are in multiple languages, separate the original from the translation
- Preserve line breaks and verse structure exactly as they appear
- Include ALL verses, chorus, bridge, etc. - do not truncate or summarize
- If no lyrics are found, return empty strings but still provide the JSON structure

RESPONSE FORMAT - You MUST respond with ONLY valid JSON in this exact structure:
{{
    "success": true/false,
    "original_lyrics": "Complete original lyrics with proper line breaks",
    "translated_lyrics": "Complete translated lyrics with proper line breaks (empty string if no translation)",
    "original_language": "detected language code (e.g., 'te' for Telugu, 'hi' for Hindi, 'es' for Spanish)",
    "translation_language": "detected language code (usually 'en' for English, empty string if no translation)",
    "confidence_score": 0.95,
    "extraction_notes": "Brief note about extraction quality/issues",
    "found_indicators": ["list", "of", "keywords", "that", "helped", "identify", "lyrics"]
}}

LANGUAGE DETECTION GUIDELINES:
- Use ISO 639-1 language codes (en, es, fr, de, hi, te, ta, etc.)
- Common patterns: Telugu (te), Hindi (hi), Tamil (ta), English (en), Spanish (es)
- If uncertain about language, use "unknown" but make your best guess

CONFIDENCE SCORING (0.0 to 1.0):
- 0.9-1.0: Complete lyrics with clear structure found
- 0.7-0.9: Most lyrics found, minor formatting issues
- 0.5-0.7: Partial lyrics or unclear structure
- 0.3-0.5: Lyrics fragments found
- 0.0-0.3: No clear lyrics identified

EXTRACTION NOTES - Include information about:
- Whether complete song was found or just fragments
- Any formatting issues or unclear sections
- Translation quality if applicable
- Any missing verses or repetitions

Now analyze this webpage content for lyrics of "{song_name}" by {artist_name}:

WEBPAGE CONTENT:
"""


def validate_gemini_response(response_text):
    """
    Validate and parse Gemini's JSON response with robust error handling
    """
    try:
        response_text = response_text.strip()

        if response_text.startswith("```json"):
            response_text = response_text[7:]
        if response_text.startswith("```"):
            response_text = response_text[3:]
        if response_text.endswith("```"):
            response_text = response_text[:-3]

        response_text = response_text.strip()

        data = json.loads(response_text)

        required_fields = ["success", "original_lyrics", "translated_lyrics"]
        for field in required_fields:
            if field not in data:
                logger.warning(f"Missing required field: {field}")
                data[field] = "" if field != "success" else False

        string_fields = [
            "original_lyrics",
            "translated_lyrics",
            "original_language",
            "translation_language",
            "extraction_notes",
        ]
        for field in string_fields:
            if field in data and not isinstance(data[field], str):
                data[field] = str(data[field]) if data[field] is not None else ""

        if "confidence_score" in data:
            try:
                confidence = float(data["confidence_score"])
                data["confidence_score"] = max(0.0, min(1.0, confidence))
            except (ValueError, TypeError):
                data["confidence_score"] = 0.5
        else:
            data["confidence_score"] = 0.5

        if "found_indicators" not in data:
            data["found_indicators"] = []
        elif not isinstance(data["found_indicators"], list):
            data["found_indicators"] = []

        return data

    except json.JSONDecodeError as e:
        logger.error(f"JSON parsing error: {e}")
        logger.error(f"Raw response: {response_text[:500]}...")

        return {
            "success": False,
            "error": f"JSON parsing failed: {str(e)}",
            "original_lyrics": "",
            "translated_lyrics": "",
            "original_language": "",
            "translation_language": "",
            "confidence_score": 0.0,
            "extraction_notes": f"Failed to parse Gemini response as JSON. Raw response length: {len(response_text)}",
            "found_indicators": [],
            "raw_response": response_text[:1000],
        }
    except Exception as e:
        logger.error(f"Unexpected error validating response: {e}")
        return {
            "success": False,
            "error": f"Validation error: {str(e)}",
            "original_lyrics": "",
            "translated_lyrics": "",
            "original_language": "",
            "translation_language": "",
            "confidence_score": 0.0,
            "extraction_notes": "Unexpected error during response validation",
            "found_indicators": [],
            "raw_response": response_text[:1000] if isinstance(response_text, str) else str(response_text)[:1000],
        }


def extract_lyrics_with_gemini(page_content, song_name, artist_name, model_name="gemini-2.5-flash-lite"):
    """
    Send cleaned content to Gemini for lyrics and translation extraction
    Returns structured dictionary with extraction results
    """
    if not client or not GEMINI_API_KEY:
        logger.error("Gemini API not configured")
        return {
            "success": False,
            "error": "Gemini API not configured",
            "original_lyrics": "",
            "translated_lyrics": "",
            "confidence_score": 0.0,
            "extraction_notes": "Gemini API key not found or library not installed",
        }

    if not page_content or len(page_content.strip()) < 50:
        logger.warning("Page content too short or empty")
        return {
            "success": False,
            "error": "Insufficient content",
            "original_lyrics": "",
            "translated_lyrics": "",
            "confidence_score": 0.0,
            "extraction_notes": "Page content was too short or empty for lyrics extraction",
        }

    try:
        prompt = create_lyrics_extraction_prompt(song_name, artist_name)
        full_prompt = prompt + page_content

        logger.info(f"Sending request to Gemini model: {model_name}")
        logger.info(f"Prompt length: {len(full_prompt)} characters")

        model = genai.GenerativeModel(model_name)

        generation_config = genai.types.GenerationConfig(
            temperature=0.1,
            top_p=0.8,
            top_k=40,
            max_output_tokens=2048,
        )

        response = model.generate_content(
            full_prompt,
            generation_config=generation_config,
            safety_settings={
                "HARM_CATEGORY_HATE_SPEECH": "BLOCK_NONE",
                "HARM_CATEGORY_HARASSMENT": "BLOCK_NONE",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT": "BLOCK_NONE",
                "HARM_CATEGORY_DANGEROUS_CONTENT": "BLOCK_NONE",
            },
        )

        if not response or not response.text:
            logger.error("Empty response from Gemini")
            return {
                "success": False,
                "error": "Empty response from Gemini",
                "original_lyrics": "",
                "translated_lyrics": "",
                "confidence_score": 0.0,
                "extraction_notes": "Gemini returned empty response",
            }

        logger.info(f"Received response from Gemini: {len(response.text)} characters")
        logger.debug(f"Raw response preview: {response.text[:200]}...")

        result = validate_gemini_response(response.text)

        result["model_used"] = model_name
        result["extraction_timestamp"] = str(datetime.now())
        result["content_length_processed"] = len(page_content)

        if result.get("success"):
            original_length = len(result.get("original_lyrics", ""))
            translated_length = len(result.get("translated_lyrics", ""))
            confidence = result.get("confidence_score", 0.0)
            logger.info(
                "✓ Lyrics extraction successful - Original: "
                f"{original_length} chars, Translation: {translated_length} chars, "
                f"Confidence: {confidence:.2f}"
            )
        else:
            logger.warning(f"✗ Lyrics extraction failed: {result.get('error', 'Unknown error')}")

        return result

    except Exception as e:
        logger.error(f"Error calling Gemini API: {e}")
        return {
            "success": False,
            "error": f"Gemini API error: {str(e)}",
            "original_lyrics": "",
            "translated_lyrics": "",
            "confidence_score": 0.0,
            "extraction_notes": f"API call failed with error: {str(e)}",
        }


def process_bookmarked_page_for_lyrics(url, song_name, artist_name):
    """
    Complete workflow: Extract page content -> Send to Gemini -> Return processed results
    """
    print(f"Processing lyrics extraction for: {song_name} by {artist_name} from {url}")

    html_content = extract_page_content(url)
    if not html_content:
        return {
            "success": False,
            "error": "Failed to fetch page content",
            "original_lyrics": "",
            "translated_lyrics": "",
        }

    clean_content = clean_html_for_gemini(html_content)
    if not clean_content:
        return {
            "success": False,
            "error": "Failed to extract lyrics-relevant content",
            "original_lyrics": "",
            "translated_lyrics": "",
        }

    print(f"Advanced cleaned content length: {len(clean_content)} characters")
    print(f"Content preview (first 500 chars): {clean_content[:500]}...")

    if len(clean_content) > 6000:
        print(f"Content still too large after cleaning: {len(clean_content)} chars, truncating")
        clean_content = clean_content[:4000] + "..."

    result = extract_lyrics_with_gemini(clean_content, song_name, artist_name)

    result["source_url"] = url
    result["processed_at"] = str(datetime.now())

    return result


def test_gemini_connection(model_name="gemini-2.5-flash-lite"):
    """Test if Gemini API is properly configured"""
    if not GEMINI_API_KEY:
        return False, "API key not found"

    if not genai or not client:
        return False, "Gemini library not available"

    try:
        model = genai.GenerativeModel(model_name)
        response = model.generate_content("Hello, this is a test. Please respond with 'Test successful'.")
        if response.text and "test successful" in response.text.lower():
            return True, "Connection successful"
        else:
            return False, f"Unexpected response: {response.text}"
    except Exception as e:
        return False, f"Connection failed: {str(e)}"


def create_ai_translation_prompt(original_lyrics, song_name, artist_name):
    """
    Create a detailed prompt for Gemini to generate word-to-word translation and romanization
    """
    return f"""You are an expert language translator and romanization specialist. Your task is to analyze the original lyrics for the song "{song_name}" by {artist_name} and provide:

1. Word-to-word English translation
2. Romanized version (transliteration to Roman/Latin script)

INSTRUCTIONS FOR WORD-TO-WORD TRANSLATION:
- Translate each word or phrase directly while maintaining grammatical structure
- Preserve the original meaning and context
- Keep poetic/metaphorical expressions when possible
- Maintain line breaks and verse structure exactly
- Do NOT paraphrase or interpret - translate as literally as possible
- Include cultural context in parentheses when needed

INSTRUCTIONS FOR ROMANIZATION:
- Convert all non-Latin script text to Roman/Latin alphabet
- Use standard romanization schemes for the detected language
- Maintain pronunciation accuracy
- Preserve word boundaries and structure
- Keep punctuation and line breaks exactly as in original

RESPONSE FORMAT - You MUST respond with ONLY valid JSON in this exact structure:
{{
    "success": true/false,
    "word_to_word_translation": "Complete word-to-word English translation with proper line breaks",
    "romanized_lyrics": "Complete romanized version with proper line breaks",
    "detected_language": "detected language code (e.g., 'te' for Telugu, 'hi' for Hindi)",
    "confidence_score": 0.95,
    "translation_notes": "Brief note about translation approach and any cultural context",
    "romanization_scheme": "Name of romanization scheme used (e.g., 'ISO 15919', 'IAST', 'Simplified')"
}}

LANGUAGE-SPECIFIC GUIDELINES:
- Telugu (te): Use ISO 15919 or simplified romanization
- Hindi (hi): Use IAST or simplified Devanagari romanization  
- Tamil (ta): Use ISO 15919 or simplified romanization
- For other languages: Use most common/standard romanization

CONFIDENCE SCORING (0.0 to 1.0):
- 0.9-1.0: Complete translation with high accuracy
- 0.7-0.9: Good translation, minor uncertainties
- 0.5-0.7: Adequate translation, some ambiguities
- 0.3-0.5: Partial translation, significant uncertainties
- 0.0-0.3: Translation very uncertain or incomplete

ORIGINAL LYRICS TO TRANSLATE AND ROMANIZE:
{original_lyrics}

Now provide the word-to-word English translation and romanization:
"""


def generate_ai_translation_and_romanization(
    original_lyrics,
    song_name=None,
    artist_name=None,
    model_name="gemini-2.5-flash-lite",
    force_generation=False,
):
    """
    Send original lyrics to Gemini for AI translation and romanization
    Returns structured dictionary with translation results
    """
    if not client or not GEMINI_API_KEY:
        logger.error("Gemini API not configured")
        return {
            "success": False,
            "error": "Gemini API not configured",
            "word_to_word_translation": "",
            "romanized_lyrics": "",
            "confidence_score": 0.0,
            "translation_notes": "Gemini API key not found or library not installed",
        }

    if not original_lyrics or len(original_lyrics.strip()) < 10:
        logger.warning("Original lyrics too short or empty")
        return {
            "success": False,
            "error": "Insufficient lyrics content",
            "word_to_word_translation": "",
            "romanized_lyrics": "",
            "confidence_score": 0.0,
            "translation_notes": "Original lyrics were too short or empty for translation",
        }

    try:
        prompt = create_ai_translation_prompt(original_lyrics, song_name, artist_name)

        logger.info(f"Sending AI translation request to Gemini model: {model_name}")
        logger.info(f"Original lyrics length: {len(original_lyrics)} characters")

        model = genai.GenerativeModel(model_name)

        generation_config = genai.types.GenerationConfig(
            temperature=0.2,
            top_p=0.9,
            top_k=50,
            max_output_tokens=3072,
        )

        response = model.generate_content(
            prompt,
            generation_config=generation_config,
            safety_settings={
                "HARM_CATEGORY_HATE_SPEECH": "BLOCK_NONE",
                "HARM_CATEGORY_HARASSMENT": "BLOCK_NONE",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT": "BLOCK_NONE",
                "HARM_CATEGORY_DANGEROUS_CONTENT": "BLOCK_NONE",
            },
        )

        if not response or not response.text:
            logger.error("Empty response from Gemini for AI translation")
            return {
                "success": False,
                "error": "Empty response from Gemini",
                "word_to_word_translation": "",
                "romanized_lyrics": "",
                "confidence_score": 0.0,
                "translation_notes": "Gemini returned empty response for translation",
            }

        logger.info(f"Received AI translation response from Gemini: {len(response.text)} characters")

        result = validate_ai_translation_response(response.text)

        result["model_used"] = model_name
        result["translation_timestamp"] = str(datetime.now())
        result["original_lyrics_length"] = len(original_lyrics)

        if result.get("success"):
            translation_length = len(result.get("word_to_word_translation", ""))
            romanized_length = len(result.get("romanized_lyrics", ""))
            confidence = result.get("confidence_score", 0.0)
            logger.info(
                "✓ AI translation successful - Translation: "
                f"{translation_length} chars, Romanization: {romanized_length} chars, "
                f"Confidence: {confidence:.2f}"
            )
        else:
            logger.warning(f"✗ AI translation failed: {result.get('error', 'Unknown error')}")

        return result

    except Exception as e:
        logger.error(f"Error calling Gemini API for AI translation: {e}")
        return {
            "success": False,
            "error": f"Gemini API error: {str(e)}",
            "word_to_word_translation": "",
            "romanized_lyrics": "",
            "confidence_score": 0.0,
            "translation_notes": f"API call failed with error: {str(e)}",
        }


def validate_ai_translation_response(response_text):
    """
    Validate and parse Gemini's AI translation JSON response
    """
    try:
        response_text = response_text.strip()

        if response_text.startswith("```json"):
            response_text = response_text[7:]
        if response_text.startswith("```"):
            response_text = response_text[3:]
        if response_text.endswith("```"):
            response_text = response_text[:-3]

        response_text = response_text.strip()

        data = json.loads(response_text)

        required_fields = ["success", "word_to_word_translation", "romanized_lyrics"]
        for field in required_fields:
            if field not in data:
                logger.warning(f"Missing required field: {field}")
                data[field] = "" if field != "success" else False

        string_fields = [
            "word_to_word_translation",
            "romanized_lyrics",
            "detected_language",
            "translation_notes",
            "romanization_scheme",
        ]
        for field in string_fields:
            if field in data and not isinstance(data[field], str):
                data[field] = str(data[field]) if data[field] is not None else ""

        if "confidence_score" in data:
            try:
                confidence = float(data["confidence_score"])
                data["confidence_score"] = max(0.0, min(1.0, confidence))
            except (ValueError, TypeError):
                data["confidence_score"] = 0.5
        else:
            data["confidence_score"] = 0.5

        return data

    except json.JSONDecodeError as e:
        logger.error(f"JSON parsing error in AI translation: {e}")
        logger.error(f"Raw response: {response_text[:500]}...")

        return {
            "success": False,
            "error": f"JSON parsing failed: {str(e)}",
            "word_to_word_translation": "",
            "romanized_lyrics": "",
            "detected_language": "",
            "confidence_score": 0.0,
            "translation_notes": f"Failed to parse Gemini response as JSON. Raw response length: {len(response_text)}",
            "romanization_scheme": "",
            "raw_response": response_text[:1000],
        }
    except Exception as e:
        logger.error(f"Unexpected error validating AI translation response: {e}")
        return {
            "success": False,
            "error": f"Validation error: {str(e)}",
            "word_to_word_translation": "",
            "romanized_lyrics": "",
            "detected_language": "",
            "confidence_score": 0.0,
            "translation_notes": "Unexpected error during response validation",
            "romanization_scheme": "",
            "raw_response": response_text[:1000] if isinstance(response_text, str) else str(response_text)[:1000],
        }


if __name__ == "__main__":
    success, message = test_gemini_connection()
    print(f"Gemini API Test: {message}")
