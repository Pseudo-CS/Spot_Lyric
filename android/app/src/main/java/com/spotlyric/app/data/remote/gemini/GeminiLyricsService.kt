package com.spotlyric.app.data.remote.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.spotlyric.app.data.local.datastore.SettingsPreferences
import com.spotlyric.app.domain.error.ErrorCode
import com.spotlyric.app.domain.error.LyricsResult
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini AI service for lyrics extraction and AI translation/romanization.
 * Type-safe with proper error handling using LyricsResult.
 * API key must be provided at runtime (no embedded secrets).
 */
@Singleton
class GeminiLyricsService @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) {
    private var lastApiKey: String? = null
    private var lastModelName: String? = null
    private var cachedExtractionModel: GenerativeModel? = null
    private var cachedTranslationModel: GenerativeModel? = null

    private suspend fun getApiKey(): String {
        return settingsPreferences.geminiCustomApiKey.first() ?: ""
    }

    private suspend fun getGeminiModelName(): String {
        return settingsPreferences.geminiCustomModel.first()
    }

    private suspend fun getExtractionModel(): GenerativeModel {
        val apiKey = getApiKey()
        val modelName = getGeminiModelName()
        if (cachedExtractionModel == null || lastApiKey != apiKey || lastModelName != modelName) {
            lastApiKey = apiKey
            lastModelName = modelName
            cachedExtractionModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.1f
                    topP = 0.8f
                    topK = 40
                    maxOutputTokens = 2048
                },
                safetySettings = safetySettings
            )
        }
        return cachedExtractionModel!!
    }

    private suspend fun getTranslationModel(): GenerativeModel {
        val apiKey = getApiKey()
        val modelName = getGeminiModelName()
        if (cachedTranslationModel == null || lastApiKey != apiKey || lastModelName != modelName) {
            lastApiKey = apiKey
            lastModelName = modelName
            cachedTranslationModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.2f
                    topP = 0.9f
                    topK = 50
                    maxOutputTokens = 3072
                },
                safetySettings = safetySettings
            )
        }
        return cachedTranslationModel!!
    }

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
    )

    /**
     * Extract lyrics from cleaned page content.
     * @return LyricsResult with ExtractionResponse or typed error
     */
    suspend fun extractLyricsFromContent(
        content: String,
        songName: String,
        artistName: String
    ): LyricsResult<ExtractionResponse> {
        if (content.isBlank() || content.length < 50) {
            return LyricsResult.Error(
                code = ErrorCode.INVALID_DATA_FORMAT,
                message = "Content too short or empty (${content.length} chars)"
            )
        }

        if (getApiKey().isBlank()) {
            return LyricsResult.Error(
                code = ErrorCode.AUTH_INVALID_CREDENTIALS,
                message = "API key not configured"
            )
        }

        return try {
            android.util.Log.d("GeminiLyricsService", "Content length: ${content.length}")
            val prompt = createLyricsExtractionPrompt(songName, artistName) + content
            
            val response = getExtractionModel().generateContent(prompt)
            settingsPreferences.incrementGeminiRequests()
            val responseText = response.text
                ?: return LyricsResult.Error(
                    code = ErrorCode.INVALID_JSON_RESPONSE,
                    message = "Empty response from Gemini"
                )
            
            android.util.Log.d("GeminiLyricsService", "Response length: ${responseText.length} chars")
            
            parseJsonResponse<ExtractionResponse>(responseText)
                .map { it.copy(confidenceScore = it.confidenceScore.coerceIn(0f, 1f)) }
        } catch (e: Exception) {
            val (code, message) = GeminiErrorHandler.mapException(e)
            LyricsResult.Error(code = code, message = message, cause = e)
        }
    }

    /**
     * Generate AI translation and romanization for existing lyrics.
     * @return LyricsResult with TranslationResponse or typed error
     */
    suspend fun generateAiTranslation(
        originalLyrics: String,
        songName: String,
        artistName: String
    ): LyricsResult<TranslationResponse> {
        if (originalLyrics.isBlank()) {
            return LyricsResult.Error(
                code = ErrorCode.INVALID_DATA_FORMAT,
                message = "Original lyrics cannot be empty"
            )
        }

        if (getApiKey().isBlank()) {
            return LyricsResult.Error(
                code = ErrorCode.AUTH_INVALID_CREDENTIALS,
                message = "API key not configured"
            )
        }

        return try {
            val prompt = createAiTranslationPrompt(originalLyrics, songName, artistName)
            
            val response = getTranslationModel().generateContent(prompt)
            settingsPreferences.incrementGeminiRequests()
            val responseText = response.text
                ?: return LyricsResult.Error(
                    code = ErrorCode.INVALID_JSON_RESPONSE,
                    message = "Empty response from Gemini"
                )
            
            android.util.Log.d("GeminiLyricsService", "Translation response length: ${responseText.length}")
            
            parseJsonResponse<TranslationResponse>(responseText)
                .map { it.copy(confidenceScore = it.confidenceScore.coerceIn(0f, 1f)) }
        } catch (e: Exception) {
            val (code, message) = GeminiErrorHandler.mapException(e)
            LyricsResult.Error(code = code, message = message, cause = e)
        }
    }

    /**
     * Parse JSON response into typed data class.
     * Shared helper for both extraction and translation responses.
     */
    private inline fun <reified T> parseJsonResponse(responseText: String): LyricsResult<T> {
        return try {
            val cleaned = stripCodeFences(responseText)
            val json = JSONObject(cleaned)

            val response = when (T::class) {
                ExtractionResponse::class -> parseExtractionResponse(json) as T
                TranslationResponse::class -> parseTranslationResponse(json) as T
                else -> return LyricsResult.Error(
                    code = ErrorCode.UNKNOWN_ERROR,
                    message = "Unknown response type"
                )
            }

            LyricsResult.Success(response)
        } catch (e: Exception) {
            android.util.Log.e("GeminiLyricsService", "JSON parse error", e)
            LyricsResult.Error(
                code = ErrorCode.INVALID_JSON_RESPONSE,
                message = "Failed to parse response: ${e.message}",
                cause = e
            )
        }
    }

    private fun parseExtractionResponse(json: JSONObject): ExtractionResponse {
        val success = json.optBoolean("success", false)
        val originalLyrics = json.optString("original_lyrics", "")
        val translatedLyrics = json.optString("translated_lyrics", "")
        val confidenceScore = json.optDouble("confidence_score", 0.5).toFloat()
            .coerceIn(0f, 1f)

        return ExtractionResponse(
            success = success,
            originalLyrics = originalLyrics,
            translatedLyrics = translatedLyrics,
            originalLanguage = json.optString("original_language", ""),
            translationLanguage = json.optString("translation_language", ""),
            confidenceScore = confidenceScore,
            extractionNotes = json.optString("extraction_notes", ""),
            foundIndicators = json.optJSONArray("found_indicators")?.let { arr ->
                (0 until arr.length()).mapNotNull { arr.optString(it) }
            } ?: emptyList()
        )
    }

    private fun parseTranslationResponse(json: JSONObject): TranslationResponse {
        val confidenceScore = json.optDouble("confidence_score", 0.5).toFloat()
            .coerceIn(0f, 1f)

        return TranslationResponse(
            success = json.optBoolean("success", false),
            wordToWordTranslation = json.optString("word_to_word_translation", ""),
            romanizedLyrics = json.optString("romanized_lyrics", ""),
            detectedLanguage = json.optString("detected_language", ""),
            confidenceScore = confidenceScore,
            translationNotes = json.optString("translation_notes", ""),
            romanizationScheme = json.optString("romanization_scheme", "")
        )
    }

    // ---- Prompts (verbatim from Django's gemini_utils.py) ----

    private fun createLyricsExtractionPrompt(songName: String, artistName: String): String {
        return """You are an expert lyrics extraction assistant. Your task is to analyze webpage content and extract lyrics and translations for the song "$songName" by $artistName.

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
{
    "success": true,
    "original_lyrics": "Complete original lyrics with proper line breaks",
    "translated_lyrics": "Complete translated lyrics with proper line breaks (empty string if no translation)",
    "original_language": "detected language code (e.g., 'te' for Telugu, 'hi' for Hindi, 'es' for Spanish)",
    "translation_language": "detected language code (usually 'en' for English, empty string if no translation)",
    "confidence_score": 0.95,
    "extraction_notes": "Brief note about extraction quality/issues",
    "found_indicators": ["list", "of", "keywords", "that", "helped", "identify", "lyrics"]
}

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

Now analyze this webpage content for lyrics of "$songName" by $artistName:

WEBPAGE CONTENT:
"""
    }

    private fun createAiTranslationPrompt(
        originalLyrics: String,
        songName: String,
        artistName: String
    ): String {
        return """You are an expert language translator and romanization specialist. Your task is to analyze the original lyrics for the song "$songName" by $artistName and provide:

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
{
    "success": true,
    "word_to_word_translation": "Complete word-to-word English translation with proper line breaks",
    "romanized_lyrics": "Complete romanized version with proper line breaks",
    "detected_language": "detected language code (e.g., 'te' for Telugu, 'hi' for Hindi)",
    "confidence_score": 0.95,
    "translation_notes": "Brief note about translation approach and any cultural context",
    "romanization_scheme": "Name of romanization scheme used (e.g., 'ISO 15919', 'IAST', 'Simplified')"
}

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
$originalLyrics

Now provide the word-to-word English translation and romanization:
"""
    }

    /**
     * Generate AI translation with automatic chunking for long lyrics (>80 lines).
     * Splits into 40-line chunks at stanza boundaries and calls generateAiTranslation sequentially,
     * then merges the results. Avoids Gemini truncation on very long songs.
     */
    suspend fun generateAiTranslationChunked(
        originalLyrics: String,
        songName: String,
        artistName: String
    ): LyricsResult<TranslationResponse> {
        val lines = originalLyrics.lines()
        if (lines.size <= 80) {
            return generateAiTranslation(originalLyrics, songName, artistName)
        }

        android.util.Log.d("GeminiLyricsService", "Long song (${lines.size} lines) — chunking translation")
        val chunks = splitIntoChunks(lines, chunkSize = 40)
        val romanizedParts = mutableListOf<String>()
        val translationParts = mutableListOf<String>()
        var detectedLanguage = ""
        var romanizationScheme = ""

        for ((index, chunk) in chunks.withIndex()) {
            android.util.Log.d("GeminiLyricsService", "Translating chunk ${index + 1}/${chunks.size} (${chunk.size} lines)")
            when (val result = generateAiTranslation(chunk.joinToString("\n"), songName, artistName)) {
                is LyricsResult.Success -> {
                    romanizedParts.add(result.data.romanizedLyrics)
                    translationParts.add(result.data.wordToWordTranslation)
                    if (detectedLanguage.isBlank()) detectedLanguage = result.data.detectedLanguage
                    if (romanizationScheme.isBlank()) romanizationScheme = result.data.romanizationScheme
                }
                is LyricsResult.Error -> return result
            }
        }

        return LyricsResult.Success(
            TranslationResponse(
                success = true,
                wordToWordTranslation = translationParts.joinToString("\n"),
                romanizedLyrics = romanizedParts.joinToString("\n"),
                detectedLanguage = detectedLanguage,
                confidenceScore = 0.85f,
                translationNotes = "Chunked translation (${chunks.size} chunks of ~40 lines)",
                romanizationScheme = romanizationScheme
            )
        )
    }

    /**
     * Split lines into chunks of [chunkSize], preferring to break at stanza boundaries
     * (blank lines) within the last 10 lines of each chunk.
     */
    private fun splitIntoChunks(lines: List<String>, chunkSize: Int): List<List<String>> {
        val chunks = mutableListOf<List<String>>()
        var i = 0
        while (i < lines.size) {
            val targetEnd = minOf(i + chunkSize, lines.size)
            var adjustedEnd = targetEnd
            if (targetEnd < lines.size) {
                // Look backward for a blank line within the last 10 lines of this chunk
                for (j in targetEnd downTo maxOf(targetEnd - 10, i + 1)) {
                    if (lines[j - 1].isBlank()) {
                        adjustedEnd = j
                        break
                    }
                }
            }
            chunks.add(lines.subList(i, adjustedEnd))
            i = adjustedEnd
        }
        return chunks
    }

    private fun stripCodeFences(text: String): String {
        var result = text.trim()
        if (result.startsWith("```json")) result = result.removePrefix("```json")
        if (result.startsWith("```")) result = result.removePrefix("```")
        if (result.endsWith("```")) result = result.removeSuffix("```")
        return result.trim()
    }
}
