package com.spotlyric.app.data.remote.gemini

/**
 * Response data classes for Gemini API responses.
 * Replaces untyped Map<String, Any> with compile-time type safety.
 */

data class ExtractionResponse(
    val success: Boolean,
    val originalLyrics: String,
    val translatedLyrics: String,
    val originalLanguage: String,
    val translationLanguage: String,
    val confidenceScore: Float,
    val extractionNotes: String,
    val foundIndicators: List<String>
) {
    companion object {
        fun error(reason: String) = ExtractionResponse(
            success = false,
            originalLyrics = "",
            translatedLyrics = "",
            originalLanguage = "",
            translationLanguage = "",
            confidenceScore = 0f,
            extractionNotes = "Error: $reason",
            foundIndicators = emptyList()
        )
    }
}

data class TranslationResponse(
    val success: Boolean,
    val wordToWordTranslation: String,
    val romanizedLyrics: String,
    val detectedLanguage: String,
    val confidenceScore: Float,
    val translationNotes: String,
    val romanizationScheme: String
) {
    companion object {
        fun error(reason: String) = TranslationResponse(
            success = false,
            wordToWordTranslation = "",
            romanizedLyrics = "",
            detectedLanguage = "",
            confidenceScore = 0f,
            translationNotes = "Error: $reason",
            romanizationScheme = ""
        )
    }
}
