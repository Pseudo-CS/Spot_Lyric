package com.spotlyric.app.data.remote.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result from a successful on-device extraction stage.
 * Stage values: "Domain Parser", "Heuristics", "AI"
 */
data class ExtractionResult(
    val originalLyrics: String,
    val translatedLyrics: String = "",
    val stage: String,
    val confidence: Float = 1.0f
)

/**
 * On-device HTML fetching and cleaning for lyrics extraction.
 * Mirrors extract_page_content() + clean_html_for_gemini() from Django's gemini_utils.py,
 * with Genius-specific enhancements (data-lyrics-container, larger content limit).
 */
@Singleton
class LyricsExtractorService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        // Don't truncate incoming HTML — Genius pages are ~240KB and lyrics sit past 100KB mark
        private const val MAX_CONTENT_LENGTH = 800_000  // 800KB safety cap
        private const val MAX_CLEAN_LENGTH = 15_000     // 15K char truncation for Gemini prompt

        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/91.0.4472.124 Safari/537.36"

        /** Jsoup selectors for elements to strip — noise removal for cleaner extraction */
        private val NOISE_SELECTORS = setOf(
            "script", "style", "nav", "header", "footer", "aside",
            "form", "button", "input", "select", "svg",
            "[class*=related]", "[id*=related]",
            "[class*=sidebar]", "[id*=sidebar]",
            "[class*=widget]", "[id*=widget]",
            "[class*=comment]", "[id*=comment]",
            "[class*=reply]", "[id*=reply]",
            "[class*=share]", "[id*=share]",
            "[class*=social]", "[id*=social]",
            "[class*=adsbygoogle]", "ins.adsbygoogle",
            "[class*=advertisement]", "[id*=advertisement]"
        )

        /** CSS class keywords that indicate lyrics containers (Django compat) */
        private val LYRICS_CLASS_KEYWORDS = setOf(
            "lyric", "song", "verse", "text"
        )
    }

    /**
     * Fetch raw HTML from URL with browser User-Agent.
     * No aggressive truncation — sites like Genius pack lyrics past 100KB.
     */
    fun fetchPageContent(url: String, timeoutSeconds: Int = 20): String? {
        android.util.Log.d("LyricsExtractor", "Fetching URL: $url")
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                android.util.Log.d("LyricsExtractor", "Response code: ${response.code}, success: ${response.isSuccessful}")
                if (!response.isSuccessful) {
                    android.util.Log.e("LyricsExtractor", "HTTP error: ${response.code}")
                    return null
                }

                val body = response.body?.string() ?: return null
                android.util.Log.d("LyricsExtractor", "Raw body length: ${body.length}")

                if (body.length > MAX_CONTENT_LENGTH) {
                    android.util.Log.w("LyricsExtractor", "Body exceeds safety cap, truncating to $MAX_CONTENT_LENGTH")
                    body.substring(0, MAX_CONTENT_LENGTH)
                } else {
                    body
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LyricsExtractor", "Error fetching page content", e)
            null
        }
    }

    /**
     * Clean HTML for Gemini processing using Jsoup.
     * Priority order for content extraction:
     *   1. [data-lyrics-container] attribute (Genius-specific)
     *   2. CSS class keywords matching lyric/song/verse/text (Django compat)
     *   3. <main> / <article> / <body> fallback
     */
    fun cleanHtmlForGemini(htmlContent: String): String? {
        if (htmlContent.isBlank()) return null

        return try {
            val doc: Document = Jsoup.parse(htmlContent)

            // Remove noise elements
            val skipTags = setOf("html", "body", "article", "main")
            NOISE_SELECTORS.forEach { selector ->
                doc.select(selector).forEach { element ->
                    if (element.tagName().lowercase() !in skipTags) {
                        element.remove()
                    }
                }
            }

            // 1. Try Genius-specific data-lyrics-container attribute
            val geniusContainers = doc.select("[data-lyrics-container=true]")
            android.util.Log.d("LyricsExtractor", "data-lyrics-container elements: ${geniusContainers.size}")
            if (geniusContainers.isNotEmpty()) {
                val text = geniusContainers.joinToString("\n\n") { extractTextWithLineBreaks(it) }
                android.util.Log.d("LyricsExtractor", "Genius container text length: ${text.length}")
                if (text.length > 50) {
                    return cleanAndTruncate(text)
                }
            }

            // 2. Fallback: main/article/body (Strategy B)
            android.util.Log.d("LyricsExtractor", "Falling back to main/body extraction")
            val main = doc.selectFirst("main") ?: doc.selectFirst("article") ?: doc.body()
            val fallbackText = main?.let { extractTextWithLineBreaks(it) } ?: ""
            android.util.Log.d("LyricsExtractor", "Fallback text length: ${fallbackText.length}")
            cleanAndTruncate(fallbackText)
        } catch (e: Exception) {
            android.util.Log.e("LyricsExtractor", "Error in cleanHtmlForGemini", e)
            null
        }
    }

    /**
     * Stage 1: Try domain-specific parsers for known sites (Genius, LyricsRaag).
     * Returns ExtractionResult if successful, null if domain is unrecognised or parsing fails.
     */
    fun tryDomainParser(html: String, url: String): ExtractionResult? {
        return try {
            val doc: Document = Jsoup.parse(html)
            when {
                url.contains("genius.com", ignoreCase = true) -> tryGeniusParser(doc)
                url.contains("lyricsraag.com", ignoreCase = true) -> tryLyricsRaagParser(doc)
                url.contains("lyricswiz.com", ignoreCase = true) -> tryLyricsWizParser(doc)
                url.contains("lyricsdecoder.com", ignoreCase = true) -> tryLyricsDecoderParser(doc)
                url.contains("bollymeaning.com", ignoreCase = true) || url.contains("bollywoodmeaning.com", ignoreCase = true) -> tryBollyMeaningParser(doc)
                else -> null
            }
        } catch (e: Exception) {
            android.util.Log.e("LyricsExtractor", "Domain parser failed", e)
            null
        }
    }

    private fun tryGeniusParser(doc: Document): ExtractionResult? {
        val containers = doc.select("[data-lyrics-container=true]")
        if (containers.isEmpty()) return null
        val text = containers.joinToString("\n\n") { extractTextWithLineBreaks(it) }.trim()
        if (text.length < 50) return null
        android.util.Log.d("LyricsExtractor", "Genius domain parser succeeded, length: ${text.length}")
        return ExtractionResult(
            originalLyrics = cleanAndTruncate(text),
            stage = "Domain Parser",
            confidence = 0.95f
        )
    }

    private fun tryLyricsRaagParser(doc: Document): ExtractionResult? {
        val columns = doc.select(".wps-column-inner")
        if (columns.isEmpty()) return null
        val original = columns.select("span.original").joinToString("\n") { it.text() }.trim()
        val translated = columns.select("span.translated").joinToString("\n") { it.text() }.trim()
        if (original.length < 50) return null
        android.util.Log.d("LyricsExtractor", "LyricsRaag domain parser succeeded, original: ${original.length} chars")
        return ExtractionResult(
            originalLyrics = original,
            translatedLyrics = translated,
            stage = "Domain Parser",
            confidence = 0.95f
        )
    }

    private fun tryLyricsWizParser(doc: Document): ExtractionResult? {
        val grid = doc.selectFirst(".grid") ?: return null
        val col1 = grid.children().firstOrNull() ?: return null
        
        val originalDivs = col1.select("div.bg-gray-100")
        val translatedDivs = col1.select("div.bg-blue-100")
        
        if (originalDivs.isEmpty()) return null
        
        val originalText = originalDivs.joinToString("\n\n") { extractTextWithLineBreaks(it) }.trim()
        val translatedText = translatedDivs.joinToString("\n\n") { extractTextWithLineBreaks(it) }.trim()
        
        if (originalText.length < 50) return null
        
        android.util.Log.d("LyricsExtractor", "LyricsWiz domain parser succeeded, original length: ${originalText.length}")
        return ExtractionResult(
            originalLyrics = originalText,
            translatedLyrics = translatedText,
            stage = "Domain Parser",
            confidence = 0.95f
        )
    }

    private fun tryLyricsDecoderParser(doc: Document): ExtractionResult? {
        val article = doc.selectFirst("article") ?: return null
        val originalContainer = article.select("div").find {
            val cls = it.className()
            cls.contains("md:text-center") && cls.contains("text-lg")
        } ?: article.selectFirst("div.md\\:text-center")
        
        if (originalContainer == null) return null
        val originalText = extractTextWithLineBreaks(originalContainer).trim()
        if (originalText.length < 50) return null
        
        val heading = article.select("h2").find {
            val t = it.text()
            t.contains("Meaning in English", ignoreCase = true) || t.contains("Translation", ignoreCase = true)
        }
        
        var translatedText = ""
        if (heading != null) {
            val translatedParagraphs = mutableListOf<String>()
            var next = heading.nextElementSibling()
            while (next != null) {
                val tag = next.tagName().lowercase()
                if (tag == "h2" || tag == "h3" || tag == "div" || tag == "footer") {
                    break
                }
                if (tag == "p") {
                    val pText = extractTextWithLineBreaks(next).trim()
                    if (pText.isNotEmpty()) {
                        translatedParagraphs.add(pText)
                    }
                }
                next = next.nextElementSibling()
            }
            translatedText = translatedParagraphs.joinToString("\n\n")
        }
        
        android.util.Log.d("LyricsExtractor", "LyricsDecoder domain parser succeeded, original length: ${originalText.length}")
        return ExtractionResult(
            originalLyrics = originalText,
            translatedLyrics = translatedText,
            stage = "Domain Parser",
            confidence = 0.95f
        )
    }

    private fun tryBollyMeaningParser(doc: Document): ExtractionResult? {
        val postBody = doc.selectFirst(".post-body") ?: doc.selectFirst(".entry-content") ?: return null
        
        val originalStanzas = mutableListOf<String>()
        val translatedStanzas = mutableListOf<String>()
        
        var currentTranslated = StringBuilder()
        var hasLyricsStarted = false
        
        for (node in postBody.childNodes()) {
            if (node is Element && node.tagName().lowercase() == "b") {
                val transStr = currentTranslated.toString().trim()
                if (transStr.isNotEmpty()) {
                    val cleanTrans = transStr.lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString("\n")
                    if (cleanTrans.isNotEmpty()) {
                        translatedStanzas.add(cleanTrans)
                    }
                    currentTranslated = StringBuilder()
                }
                
                val boldText = node.text().trim()
                if (node.select("a").isNotEmpty() || 
                    boldText.startsWith("Check ", ignoreCase = true) || 
                    boldText.contains("Birth of a Song", ignoreCase = true) ||
                    boldText.contains("Love Gulzar", ignoreCase = true)) {
                    continue
                }
                
                if (boldText.length > 5) {
                    hasLyricsStarted = true
                    val cleanOriginal = extractTextWithLineBreaks(node).trim()
                    originalStanzas.add(cleanOriginal)
                }
            } else if (hasLyricsStarted) {
                if (node is TextNode) {
                    val text = node.text().trim()
                    if (text.isNotEmpty()) {
                        currentTranslated.append(text).append("\n")
                    }
                } else if (node is Element) {
                    val tagName = node.tagName().lowercase()
                    val text = node.text().trim()
                    
                    if (tagName == "a" && (text.startsWith("Check ", ignoreCase = true) || text.contains("Gulzar", ignoreCase = true))) {
                        break
                    }
                    
                    if (tagName == "br") {
                        currentTranslated.append("\n")
                    } else if (tagName == "div" || tagName == "p") {
                        val blockText = extractTextWithLineBreaks(node).trim()
                        if (blockText.isNotEmpty()) {
                            currentTranslated.append(blockText).append("\n")
                        }
                    } else {
                        val inlineText = node.text().trim()
                        if (inlineText.isNotEmpty()) {
                            currentTranslated.append(inlineText).append("\n")
                        }
                    }
                }
            }
        }
        
        val finalTransStr = currentTranslated.toString().trim()
        if (finalTransStr.isNotEmpty()) {
            val cleanTrans = finalTransStr.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
            if (cleanTrans.isNotEmpty()) {
                translatedStanzas.add(cleanTrans)
            }
        }
        
        val originalText = originalStanzas.joinToString("\n\n")
        val translatedText = translatedStanzas.joinToString("\n\n")
        
        if (originalText.length < 50) return null
        
        android.util.Log.d("LyricsExtractor", "BollyMeaning domain parser succeeded, original length: ${originalText.length}")
        return ExtractionResult(
            originalLyrics = originalText,
            translatedLyrics = translatedText,
            stage = "Domain Parser",
            confidence = 0.95f
        )
    }

    /**
     * Stage 2: Heuristic extraction by CSS class/id selectors that commonly wrap lyrics.
     * Returns ExtractionResult if a sufficiently long text is found, null otherwise.
     */
    fun tryHeuristics(html: String): ExtractionResult? {
        return try {
            val doc: Document = Jsoup.parse(html)
            val skipTags = setOf("html", "body", "article", "main")
            NOISE_SELECTORS.forEach { selector ->
                doc.select(selector).forEach { element ->
                    if (element.tagName().lowercase() !in skipTags) element.remove()
                }
            }

            val lyricSelectors = listOf(
                "[class*=lyric-content]", "[id*=lyric-content]",
                "[class*=song-lyrics]", "[id*=song-lyrics]",
                ".lyrics", "#lyrics",
                "[class*=entry-content]", "[id*=entry-content]"
            )

            for (selector in lyricSelectors) {
                val containers = doc.select(selector)
                if (containers.isNotEmpty()) {
                    val texts = containers.map { extractTextWithLineBreaks(it) }.filter { it.length > 50 }
                    if (texts.isNotEmpty()) {
                        val combined = texts.joinToString("\n\n")
                        android.util.Log.d("LyricsExtractor", "Heuristic ($selector) succeeded, length: ${combined.length}")
                        return ExtractionResult(
                            originalLyrics = cleanAndTruncate(combined),
                            stage = "Heuristics",
                            confidence = 0.70f
                        )
                    }
                }
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("LyricsExtractor", "Heuristic parser failed", e)
            null
        }
    }

    /**
     * Combined fetch + clean pipeline (used for Stage 3 / Gemini fallback).
     */
    fun extractAndClean(url: String): String? {
        val html = fetchPageContent(url) ?: return null
        return cleanHtmlForGemini(html)
    }

    /**
     * Iteratively extract text from an element, preserving <br> and block-level line breaks.
     * Uses an explicit stack instead of recursion to avoid StackOverflow on deep DOM trees.
     */
    private fun extractTextWithLineBreaks(root: Element): String {
        val sb = StringBuilder()
        // Stack-based traversal: pairs of (Node, visited)
        val stack = ArrayDeque<Pair<org.jsoup.nodes.Node, Boolean>>()
        stack.addLast(root to false)

        while (stack.isNotEmpty()) {
            val (node, closing) = stack.removeLast()

            when {
                node is TextNode -> {
                    val text = node.text()
                    if (text.isNotBlank()) sb.append(text)
                }
                node is Element && node.tagName().lowercase() == "br" -> {
                    sb.append("\n")
                }
                node is Element -> {
                    val tagName = node.tagName().lowercase()
                    val isBlock = tagName in setOf("p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "li", "tr", "td", "section", "article", "main")

                    if (closing) {
                        // Closing pass for block elements
                        if (isBlock && sb.isNotEmpty() && !sb.endsWith("\n")) {
                            sb.append("\n")
                        }
                    } else {
                        // Opening pass: push closing marker, then children in reverse order
                        if (isBlock) {
                            stack.addLast(node to true) // closing marker
                            if (sb.isNotEmpty() && !sb.endsWith("\n")) sb.append("\n")
                        }
                        for (child in node.childNodes().reversed()) {
                            stack.addLast(child to false)
                        }
                    }
                }
            }
        }
        return sb.toString().trim()
    }

    private fun cleanAndTruncate(raw: String): String {
        if (raw.isBlank()) return ""
        val lines = raw.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 2 }
        val result = lines.joinToString("\n")
        android.util.Log.d("LyricsExtractor", "Final clean length: ${result.length}")
        return if (result.length > MAX_CLEAN_LENGTH) {
            result.substring(0, MAX_CLEAN_LENGTH) + "..."
        } else {
            result
        }
    }
}
