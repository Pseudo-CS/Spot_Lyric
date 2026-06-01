from services import database, extractor, translator, search

def extract_and_translate_lyrics(url, song_name, artist_name):
    """
    Full pipeline matching Android:
    fetch HTML -> check Cloudflare -> try domain parser -> try heuristics -> try Gemini AI -> conditional translation -> save to SQLite
    """
    # 1. Fetch Page Content
    html = extractor.fetch_page_content(url)
    if not html:
        raise Exception("Failed to download page. Please check your internet connection or URL.")

    # Check Cloudflare bot protection
    cloudflare_signatures = [
        "challenge-error-text",
        "Enable JavaScript and cookies to continue",
        "cf-browser-verification",
        "_cf_chl_opt"
    ]
    if any(sig in html for sig in cloudflare_signatures):
        raise Exception("This source is protected by Cloudflare bot protection. Please choose another source from the list.")

    # 2. Multi-stage extraction
    original_lyrics = ""
    translated_lyrics = ""
    extraction_stage = ""
    confidence = 1.0
    original_language = ""

    # Stage 3: Gemini fallback (Domain parser and heuristics bypassed)
    clean_content = extractor.clean_html_for_gemini(html)
    if not clean_content:
        raise Exception("Failed to extract readable text from this webpage. Please try another source.")
        
    gemini_result = translator.extract_lyrics_from_content(clean_content, song_name, artist_name)
    if not gemini_result.get("success"):
        error_msg = gemini_result.get("extraction_notes") or gemini_result.get("error") or "Gemini extraction failed."
        raise Exception(error_msg)
        
    original_lyrics = gemini_result.get("original_lyrics", "")
    translated_lyrics = gemini_result.get("translated_lyrics", "")
    extraction_stage = "AI"
    confidence = gemini_result.get("confidence_score", 0.5)
    original_language = gemini_result.get("original_language", "")

    if not original_lyrics or not original_lyrics.strip():
        raise Exception("No lyrics found in the extracted content. Please try another source.")

    # 3. Conditional AI Translation
    # Skip if we already have translated lyrics, or if original language is English
    skip_ai_translation = bool(translated_lyrics.strip()) or original_language.lower() == "en"
    
    ai_romanized = None
    ai_translation = None
    
    if not skip_ai_translation:
        try:
            ai_result = translator.generate_ai_translation_chunked(original_lyrics, song_name, artist_name)
            if ai_result.get("success"):
                ai_romanized = ai_result.get("romanized_lyrics")
                ai_translation = ai_result.get("word_to_word_translation")
                if not original_language:
                    original_language = ai_result.get("detected_language", "")
        except Exception as e:
            print(f"AI translation failed: {e}")

    # 4. Save to Database
    # Ensure bookmark exists
    bookmark = database.get_bookmark(song_name, artist_name)
    if not bookmark:
        bookmark_id = database.add_bookmark(song_name, artist_name, url, song_name)
    else:
        bookmark_id = bookmark["id"]
        # Update bookmark URL if user is swapping source
        if bookmark["bookmarked_url"] != url:
            database.add_bookmark(song_name, artist_name, url, bookmark["title"])

    # Delete any existing lyrics for this bookmark and save new ones
    database.delete_lyrics_by_bookmark_id(bookmark_id)
    lyrics_id = database.save_lyrics(
        bookmark_id=bookmark_id,
        original_lyrics=original_lyrics,
        translated_lyrics=translated_lyrics,
        ai_romanized=ai_romanized,
        ai_translation=ai_translation,
        source_url=url,
        extraction_stage=extraction_stage,
        confidence_score=confidence,
        original_language=original_language if original_language else None
    )

    return {
        "id": lyrics_id,
        "bookmark_id": bookmark_id,
        "original_lyrics": original_lyrics,
        "translated_lyrics": translated_lyrics,
        "ai_romanized": ai_romanized,
        "ai_translation": ai_translation,
        "source_url": url,
        "extraction_stage": extraction_stage,
        "confidence_score": confidence,
        "original_language": original_language
    }

def generate_ai_translation_manual(song_name, artist_name, source_url):
    """
    Manually trigger AI translation.
    Matches playerRepository.generateAiTranslation in Android.
    """
    # 1. Check if lyrics already exist in DB
    lyrics = database.get_lyrics(song_name, artist_name)
    
    if not lyrics or not lyrics.get("original_lyrics", "").strip():
        # Need to extract first
        clean_content = extractor.fetch_page_content(source_url)
        if not clean_content:
            raise Exception("Failed to fetch page content.")
            
        clean_text = extractor.clean_html_for_gemini(clean_content)
        if not clean_text:
            raise Exception("Failed to clean HTML content.")
            
        extraction = translator.extract_lyrics_from_content(clean_text, song_name, artist_name)
        if not extraction.get("success"):
            raise Exception(extraction.get("error", "Lyrics extraction failed."))
            
        original = extraction.get("original_lyrics", "")
        if not original.strip():
            raise Exception("No lyrics found in content.")
            
        # Ensure bookmark exists
        bookmark = database.get_bookmark(song_name, artist_name)
        if not bookmark:
            bookmark_id = database.add_bookmark(song_name, artist_name, source_url, song_name)
        else:
            bookmark_id = bookmark["id"]
            
        database.delete_lyrics_by_bookmark_id(bookmark_id)
        database.save_lyrics(
            bookmark_id=bookmark_id,
            original_lyrics=original,
            translated_lyrics=extraction.get("translated_lyrics", ""),
            source_url=source_url,
            extraction_stage="AI",
            confidence_score=extraction.get("confidence_score", 0.5),
            original_language=extraction.get("original_language")
        )
        
        lyrics = database.get_lyrics(song_name, artist_name)

    # 2. Generate translation
    original_lyrics = lyrics["original_lyrics"]
    ai_result = translator.generate_ai_translation(original_lyrics, song_name, artist_name)
    if not ai_result.get("success"):
        raise Exception(ai_result.get("error", "AI translation failed."))

    ai_romanized = ai_result.get("romanized_lyrics", "")
    ai_translation = ai_result.get("word_to_word_translation", "")
    
    database.update_ai_fields(lyrics["bookmark_id"], ai_romanized, ai_translation)
    
    lyrics["ai_romanized"] = ai_romanized
    lyrics["ai_translation"] = ai_translation
    return lyrics
