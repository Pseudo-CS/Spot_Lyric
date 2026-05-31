import os
import json
import logging
import google.generativeai as genai
from services import database

logger = logging.getLogger(__name__)

def get_gemini_credentials():
    """Fetch Gemini API Key and Model Name from settings, falling back to env vars."""
    api_key = database.get_setting("gemini_custom_api_key")
    model_name = database.get_setting("gemini_custom_model", "gemini-2.5-flash-lite")

    if not api_key:
        api_key = os.getenv("GEMINI_API_KEY")
    return api_key, model_name

def get_gemini_model(temperature, top_p, top_k, max_output_tokens):
    """Dynamically configure and return a Gemini GenerativeModel."""
    api_key, model_name = get_gemini_credentials()
    if not api_key:
        raise ValueError("Gemini API key is not configured.")

    genai.configure(api_key=api_key)
    
    generation_config = genai.types.GenerationConfig(
        temperature=temperature,
        top_p=top_p,
        top_k=top_k,
        max_output_tokens=max_output_tokens,
    )
    
    safety_settings = {
        "HARM_CATEGORY_HATE_SPEECH": "BLOCK_NONE",
        "HARM_CATEGORY_HARASSMENT": "BLOCK_NONE",
        "HARM_CATEGORY_SEXUALLY_EXPLICIT": "BLOCK_NONE",
        "HARM_CATEGORY_DANGEROUS_CONTENT": "BLOCK_NONE",
    }

    model = genai.GenerativeModel(
        model_name=model_name,
        generation_config=generation_config,
        safety_settings=safety_settings
    )
    return model

def strip_code_fences(text):
    """Strip markdown code block tags from json response."""
    result = text.strip()
    if result.startswith("```json"):
        result = result[7:]
    elif result.startswith("```"):
        result = result[3:]
    if result.endswith("```"):
        result = result[:-3]
    return result.strip()

# --- Stage 3: AI Extraction Fallback ---
def create_lyrics_extraction_prompt(song_name, artist_name):
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
    "success": true,
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

Now analyze this webpage content for lyrics of "{song_name}" by {artist_name}:

WEBPAGE CONTENT:
"""

def extract_lyrics_from_content(content, song_name, artist_name):
    """
    Use Gemini AI to extract lyrics from webpage content.
    Returns:
        dict: Parsed JSON response.
    """
    if not content or len(content.strip()) < 50:
        return {"success": False, "error": "Content is too short or empty."}

    try:
        model = get_gemini_model(temperature=0.1, top_p=0.8, top_k=40, max_output_tokens=2048)
        prompt = create_lyrics_extraction_prompt(song_name, artist_name) + content
        
        database.increment_request_count("gemini")
        response = model.generate_content(prompt)
        if not response or not response.text:
            return {"success": False, "error": "Empty response from Gemini."}

        cleaned = strip_code_fences(response.text)
        data = json.loads(cleaned)
        
        # Coerce confidence
        data["confidence_score"] = max(0.0, min(1.0, float(data.get("confidence_score", 0.5))))
        return data
    except Exception as e:
        logger.error(f"Error extracting lyrics with Gemini: {e}")
        return {"success": False, "error": str(e)}

# --- AI Word-to-word Translation and Romanization ---
def create_ai_translation_prompt(original_lyrics, song_name, artist_name):
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
    "success": true,
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

def generate_ai_translation(original_lyrics, song_name, artist_name):
    """Generate word-to-word translation and romanization using Gemini."""
    if not original_lyrics or not original_lyrics.strip():
        return {"success": False, "error": "Original lyrics are empty."}

    try:
        model = get_gemini_model(temperature=0.2, top_p=0.9, top_k=50, max_output_tokens=3072)
        prompt = create_ai_translation_prompt(original_lyrics, song_name, artist_name)
        
        database.increment_request_count("gemini")
        response = model.generate_content(prompt)
        if not response or not response.text:
            return {"success": False, "error": "Empty response from Gemini."}

        cleaned = strip_code_fences(response.text)
        data = json.loads(cleaned)
        data["confidence_score"] = max(0.0, min(1.0, float(data.get("confidence_score", 0.5))))
        return data
    except Exception as e:
        logger.error(f"Error generating AI translation: {e}")
        return {"success": False, "error": str(e)}

# --- Chunked translation for long tracks ---
def split_into_chunks(lines, chunk_size=40):
    """Split lyrics into chunks, breaking at stanza boundaries (blank lines) if possible."""
    chunks = []
    i = 0
    while i < len(lines):
        target_end = min(i + chunk_size, len(lines))
        adjusted_end = target_end
        if target_end < len(lines):
            # Look backward for a blank line within the last 10 lines of this chunk
            for j in range(target_end, max(target_end - 10, i + 1), -1):
                if not lines[j - 1].strip():
                    adjusted_end = j
                    break
        chunks.append(lines[i:adjusted_end])
        i = adjusted_end
    return chunks

def generate_ai_translation_chunked(original_lyrics, song_name, artist_name):
    """Translate long lyrics using chunking to avoid response truncation."""
    lines = original_lyrics.splitlines()
    if len(lines) <= 80:
        return generate_ai_translation(original_lyrics, song_name, artist_name)

    logger.info(f"Long song ({len(lines)} lines) — chunking translation.")
    chunks = split_into_chunks(lines, chunk_size=40)
    
    romanized_parts = []
    translation_parts = []
    detected_language = ""
    romanization_scheme = ""

    for index, chunk in enumerate(chunks):
        chunk_text = "\n".join(chunk)
        logger.info(f"Translating chunk {index + 1}/{len(chunks)}")
        result = generate_ai_translation(chunk_text, song_name, artist_name)
        if not result.get("success"):
            return result  # Propagate failure
            
        romanized_parts.append(result.get("romanized_lyrics", ""))
        translation_parts.append(result.get("word_to_word_translation", ""))
        
        if not detected_language:
            detected_language = result.get("detected_language", "")
        if not romanization_scheme:
            romanization_scheme = result.get("romanization_scheme", "")

    return {
        "success": True,
        "word_to_word_translation": "\n".join(translation_parts),
        "romanized_lyrics": "\n".join(romanized_parts),
        "detected_language": detected_language,
        "confidence_score": 0.85,
        "translation_notes": f"Chunked translation ({len(chunks)} chunks of ~40 lines)",
        "romanization_scheme": romanization_scheme
    }
