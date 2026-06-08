package com.spotlyric.app.domain.util

object TextUtil {
    /**
     * Returns true if the text is primarily written in Latin script (e.g. Romaji/English/Spanish).
     */
    fun isTextRomanized(text: String?): Boolean {
        if (text.isNullOrBlank()) return true
        
        var latinChars = 0
        var nonLatinChars = 0
        
        for (char in text) {
            if (!char.isLetter()) continue
            if (char.code <= 0x024F) {
                latinChars++
            } else {
                nonLatinChars++
            }
        }
        
        val total = latinChars + nonLatinChars
        if (total == 0) return true
        
        return (nonLatinChars.toFloat() / total.toFloat()) < 0.15f
    }
}
