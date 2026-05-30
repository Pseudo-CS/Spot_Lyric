package com.spotlyric.app.domain.model

data class Lyrics(
    val id: Long = 0,
    val bookmarkId: Long,
    val originalLyrics: String,
    val translatedLyrics: String,
    val aiRomanized: String? = null,
    val aiTranslation: String? = null,
    val sourceUrl: String? = null,
    val extractionStage: String? = null,
    val confidenceScore: Float? = null,
    val originalLanguage: String? = null
) {
    fun originalLines(): List<String> = originalLyrics.lines()
    fun translatedLines(): List<String> = translatedLyrics.lines()
    fun aiRomanizedLines(): List<String>? = aiRomanized?.lines()
    fun aiTranslationLines(): List<String>? = aiTranslation?.lines()
}
