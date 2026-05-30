package com.spotlyric.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RelevanceFilterTest {

    @Test
    fun testPerfectMatchIsRelevant() {
        // Song title and artist in result
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "Blinding Lights",
            artistName = "The Weeknd",
            resultTitle = "Blinding Lights - The Weeknd lyrics translation"
        )
        assertTrue(isRelevant)
    }

    @Test
    fun testPartialMatchIsRelevant() {
        // Some tokens match
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "Blinding Lights",
            artistName = "The Weeknd",
            resultTitle = "Blinding Lights (Official Video)"
        )
        assertTrue(isRelevant)
    }

    @Test
    fun testCompleteMismatchIsNotRelevant() {
        // Completely different song
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "Blinding Lights",
            artistName = "The Weeknd",
            resultTitle = "Levitating - Dua Lipa lyrics"
        )
        assertFalse(isRelevant)
    }

    @Test
    fun testArtistOnlyMatchIsNotRelevant() {
        // Artist matches, song might be different
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "Song A",
            artistName = "Taylor Swift",
            resultTitle = "Taylor Swift lyrics"
        )
        assertFalse(isRelevant)
    }

    @Test
    fun testSongTitleMismatchIsNotRelevant() {
        // Song title completely missing
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "Blinding Lights",
            artistName = "The Weeknd",
            resultTitle = "The Weeknd Official Website"
        )
        assertFalse(isRelevant)
    }

    @Test
    fun testWithSnippetIncreaseRelevance() {
        // Result title doesn't match much, but snippet does
        val queryTokens = RelevanceFilter.tokenize("Starboy The Weeknd")
        val titleTokens = RelevanceFilter.tokenize("Official Video")
        val snippetTokens = RelevanceFilter.tokenize("Starboy by The Weeknd featuring Daft Punk. Listen now...")

        val scoreWithoutSnippet = RelevanceFilter.calculateRelevanceScore(queryTokens, titleTokens)
        val scoreWithSnippet = RelevanceFilter.calculateRelevanceScore(queryTokens, titleTokens + snippetTokens)
        
        assertTrue(scoreWithSnippet > scoreWithoutSnippet)
    }

    @Test
    fun testCaseInsensitive() {
        val isRelevant1 = RelevanceFilter.isRelevant(
            songName = "blinding lights",
            artistName = "the weeknd",
            resultTitle = "Blinding Lights - The Weeknd"
        )
        
        val isRelevant2 = RelevanceFilter.isRelevant(
            songName = "BLINDING LIGHTS",
            artistName = "THE WEEKND",
            resultTitle = "blinding lights - the weeknd"
        )
        
        assertTrue(isRelevant1)
        assertTrue(isRelevant2)
    }

    @Test
    fun testStopwordsIgnored() {
        // "The" and "Of" should be ignored
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "Starboy",
            artistName = "The Weeknd",
            resultTitle = "Starboy by The Weeknd the official music"
        )
        assertTrue(isRelevant)
    }

    @Test
    fun testSpecialCharactersHandled() {
        // Quotes, hyphens, parentheses should be removed
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "Bad Guy",
            artistName = "Billie Eilish",
            resultTitle = "\"Bad Guy\" - (Billie Eilish) [Lyrics]"
        )
        assertTrue(isRelevant)
    }

    @Test
    fun testCalculateRelevanceScoreRange() {
        val queryTokens = RelevanceFilter.tokenize("Blinding Lights The Weeknd")
        val resultTokens = RelevanceFilter.tokenize("Blinding Lights lyrics")
        val score = RelevanceFilter.calculateRelevanceScore(queryTokens, resultTokens)
        
        // Score should be between 0.0 and 1.0
        assertTrue(score >= 0.0f)
        assertTrue(score <= 1.0f)
    }
 
    @Test
    fun testEmptyQueryEdgeCase() {
        // Empty song name should not crash
        val queryTokens = emptySet<String>()
        val resultTokens = RelevanceFilter.tokenize("Some Result")
        val score = RelevanceFilter.calculateRelevanceScore(queryTokens, resultTokens)
        
        // Should return 1.0 for empty query (edge case)
        assertEquals(1.0f, score, 0.001f)
    }

    @Test
    fun testMinimumTitleTokensMatch() {
        // Title tokens should require minimum match
        val irrelevant = RelevanceFilter.isRelevant(
            songName = "Unique Title Here",
            artistName = "Artist Name",
            resultTitle = "Completely Different Song Lyrics"
        )
        
        assertFalse(irrelevant)
    }

    @Test
    fun testTranslationQueryVariations() {
        // Both search variations should work
        val translationVariant = RelevanceFilter.isRelevant(
            songName = "Blinding Lights",
            artistName = "The Weeknd",
            resultTitle = "Blinding Lights lyrics translation"
        )
        
        val lyricsOnlyVariant = RelevanceFilter.isRelevant(
            songName = "Blinding Lights",
            artistName = "The Weeknd",
            resultTitle = "Blinding Lights lyrics"
        )
        
        assertTrue(translationVariant)
        assertTrue(lyricsOnlyVariant)
    }

    @Test
    fun testPreferredSourceRelevanceStrictness() {
        // Even preferred sources should be filtered if irrelevant
        val queryTokens = RelevanceFilter.tokenize("Blinding Lights The Weeknd")
        val resultTokens = RelevanceFilter.tokenize("Levitating Lyrics - Dua Lipa Official")
        val score = RelevanceFilter.calculateRelevanceScore(queryTokens, resultTokens)
        
        // This would fail relevance check regardless of being preferred
        assertTrue(score < 0.2f)
    }

    @Test
    fun testMultiWordSongTitles() {
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "Shape of You",
            artistName = "Ed Sheeran",
            resultTitle = "Shape of You - Ed Sheeran Official"
        )
        assertTrue(isRelevant)
    }

    @Test
    fun testAcronymsAndSpecialChars() {
        val isRelevant = RelevanceFilter.isRelevant(
            songName = "B.O.B",
            artistName = "OutKast",
            resultTitle = "B.O.B - OutKast lyrics"
        )
        // Should handle special chars gracefully
        assertTrue(isRelevant)
    }

    @Test
    fun testThresholdBoundary() {
        val queryTokens = RelevanceFilter.tokenize("Hit Artist")
        val resultTokens = RelevanceFilter.tokenize("Hit by Artist")
        val score = RelevanceFilter.calculateRelevanceScore(queryTokens, resultTokens)
        
        // This should be at or above threshold
        assertTrue(score >= 0.2f)
    }
}
