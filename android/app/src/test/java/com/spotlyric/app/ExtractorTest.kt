package com.spotlyric.app

import com.spotlyric.app.data.remote.extractor.LyricsExtractorService
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtractorTest {

    @Test
    fun testExtraction() {
        val stream = javaClass.getResourceAsStream("/test_genius.html")
        assertNotNull("HTML test fixture resource not found", stream)
        val htmlContent = stream!!.bufferedReader().use { it.readText() }
        
        val extractor = LyricsExtractorService(OkHttpClient())
        val cleaned = extractor.cleanHtmlForGemini(htmlContent)

        assertNotNull("Cleaned content should not be null", cleaned)
        assertTrue("Cleaned content should not be blank", cleaned!!.isNotBlank())
        assertTrue("Cleaned content should contain lyrics", cleaned.length > 100)
    }

    @Test
    fun testLyricsraagExtraction() {
        val stream = javaClass.getResourceAsStream("/test_lyricsraag_ok.html")
        assertNotNull("HTML test fixture resource not found", stream)
        val htmlContent = stream!!.bufferedReader().use { it.readText() }
        
        val extractor = LyricsExtractorService(OkHttpClient())
        val cleaned = extractor.cleanHtmlForGemini(htmlContent)

        println("--- CLEANED LYRICSRAAG ---")
        println(cleaned)
        println("--------------------------")

        assertNotNull("Cleaned content should not be null", cleaned)
        assertTrue("Cleaned content should not be blank", cleaned!!.isNotBlank())
        assertTrue("Cleaned content should contain lyrics", cleaned.length > 100)
    }

    @Test
    fun testNeraExtraction() {
        val stream = javaClass.getResourceAsStream("/test_nera.html")
        assertNotNull("HTML test fixture resource not found", stream)
        val htmlContent = stream!!.bufferedReader().use { it.readText() }
        
        val extractor = LyricsExtractorService(OkHttpClient())
        val cleaned = extractor.cleanHtmlForGemini(htmlContent)

        println("--- CLEANED NERA ---")
        println(cleaned)
        println("--------------------")

        assertNotNull("Cleaned content should not be null", cleaned)
        assertTrue("Cleaned content should not be blank", cleaned!!.isNotBlank())
        assertTrue("Cleaned content should contain lyrics", cleaned.length > 100)
    }

    @Test
    fun testLyricswizExtraction() {
        val stream = javaClass.getResourceAsStream("/test_lyricswiz.html")
        assertNotNull("HTML test fixture resource not found", stream)
        val htmlContent = stream!!.bufferedReader().use { it.readText() }
        
        val extractor = LyricsExtractorService(OkHttpClient())
        val result = extractor.tryDomainParser(htmlContent, "https://www.lyricswiz.com/sarrb-kamlee-lyrics-english-translation/")
        
        assertNotNull("Domain parser result should not be null", result)
        org.junit.Assert.assertEquals("Domain Parser", result!!.stage)
        assertTrue("Original lyrics should not be blank", result.originalLyrics.isNotBlank())
        assertTrue("Translated lyrics should not be blank", result.translatedLyrics.isNotBlank())
        assertTrue("Original lyrics should contain Kamlee text", result.originalLyrics.contains("Kamlee"))
    }

    @Test
    fun testLyricsDecoderExtraction() {
        val stream = javaClass.getResourceAsStream("/test_lyricsdecoder.html")
        assertNotNull("HTML test fixture resource not found", stream)
        val htmlContent = stream!!.bufferedReader().use { it.readText() }
        
        val extractor = LyricsExtractorService(OkHttpClient())
        val result = extractor.tryDomainParser(htmlContent, "https://www.lyricsdecoder.com/maahi-ve-english-meaning-highway/")
        
        assertNotNull("Domain parser result should not be null", result)
        org.junit.Assert.assertEquals("Domain Parser", result!!.stage)
        assertTrue("Original lyrics should not be blank", result.originalLyrics.isNotBlank())
        assertTrue("Translated lyrics should not be blank", result.translatedLyrics.isNotBlank())
        assertTrue("Original lyrics should contain Maahi Ve text", result.originalLyrics.contains("Maahi Ve"))
        assertTrue("Translated lyrics should contain Translation text", result.translatedLyrics.contains("When sunlight pours down"))
    }

    @Test
    fun testBollyMeaningExtraction() {
        val stream = javaClass.getResourceAsStream("/test_bollymeaning.html")
        assertNotNull("HTML test fixture resource not found", stream)
        val htmlContent = stream!!.bufferedReader().use { it.readText() }
        
        val extractor = LyricsExtractorService(OkHttpClient())
        val result = extractor.tryDomainParser(htmlContent, "http://www.bollymeaning.com/2012/10/jiya-jiya-re-jiya-re-lyrics-translation.html")
        
        assertNotNull("Domain parser result should not be null", result)
        org.junit.Assert.assertEquals("Domain Parser", result!!.stage)
        assertTrue("Original lyrics should not be blank", result.originalLyrics.isNotBlank())
        assertTrue("Translated lyrics should not be blank", result.translatedLyrics.isNotBlank())
        assertTrue("Original lyrics should contain Chali re", result.originalLyrics.contains("Chali re"))
        assertTrue("Translated lyrics should contain Translation text", result.translatedLyrics.contains("drinking moments"))
    }
}
