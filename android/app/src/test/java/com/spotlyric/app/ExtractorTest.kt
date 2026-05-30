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
}
