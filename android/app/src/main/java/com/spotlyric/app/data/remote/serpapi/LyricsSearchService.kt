package com.spotlyric.app.data.remote.serpapi

import com.spotlyric.app.data.local.datastore.SettingsPreferences
import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.domain.util.RelevanceFilter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps SerpApiService, returning domain LyricsSource objects.
 *
 * Fires two concurrent queries per search for broader coverage, deduplicates results
 * by normalised URL, and ranks by a composite score:
 *   - Preferred / user-managed domains: +2.0 (always floated to top)
 *   - Tier 1 reliable domains (genius, lyricsraag, musixmatch): +1.0
 *   - Tier 2 general directories (azlyrics, lyricfinder, letrastraducidas): +0.5
 *   - Tier 3 unreliable / cloudflared domains: -0.5
 *   - Token-overlap relevance score (0.0–1.0): added on top
 *
 * Filtering can be toggled and threshold adjusted via Settings.
 */
@Singleton
class LyricsSearchService @Inject constructor(
    private val serpApiService: SerpApiService,
    private val settingsPreferences: SettingsPreferences
) {
    companion object {
        private val TIER1_DOMAINS = setOf("genius.com", "lyricsraag.com", "musixmatch.com")
        private val TIER2_DOMAINS = setOf("azlyrics.com", "lyricfinder.org", "letrastraducidas.org")
        private val TIER3_DOMAINS = setOf(
            "songlyrics.com", "metrolyrics.com", "lyrics007.com"
        )

        private fun domainScore(url: String): Float {
            val lower = url.lowercase()
            return when {
                TIER1_DOMAINS.any { lower.contains(it) } -> 1.0f
                TIER2_DOMAINS.any { lower.contains(it) } -> 0.5f
                TIER3_DOMAINS.any { lower.contains(it) } -> -0.5f
                else -> 0.0f
            }
        }

        /** Normalise a URL for deduplication: remove protocol, www., trailing slash, and query/fragment. */
        fun normaliseUrl(url: String): String {
            return url
                .lowercase()
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
                .substringBefore("?")
                .substringBefore("#")
                .trimEnd('/')
        }
    }

    /**
     * Search for lyrics translation sources using two concurrent queries.
     * Query A: "{song} {artist} lyrics translation"
     * Query B: "{song} {artist} lyrics english translation"
     *
     * Results from both queries are merged, deduplicated, scored, and sorted.
     */
    suspend fun searchLyricsSources(
        songName: String,
        artistName: String,
        preferredDomains: List<String> = emptyList()
    ): List<LyricsSource> = runDualSearch(
        queryA = "$songName $artistName lyrics translation",
        queryB = "$songName $artistName lyrics english translation",
        songName = songName,
        artistName = artistName,
        preferredDomains = preferredDomains
    )

    /**
     * Search for lyrics sources (without "translation" keyword) for AI translate flow.
     * Query A: "{song} {artist} lyrics"
     * Query B: "{song} {artist} lyrics translation"
     */
    suspend fun searchLyricsOnly(
        songName: String,
        artistName: String,
        preferredDomains: List<String> = emptyList()
    ): List<LyricsSource> = runDualSearch(
        queryA = "$songName $artistName lyrics",
        queryB = "$songName $artistName lyrics translation",
        songName = songName,
        artistName = artistName,
        preferredDomains = preferredDomains
    )

    private suspend fun runDualSearch(
        queryA: String,
        queryB: String,
        songName: String,
        artistName: String,
        preferredDomains: List<String>
    ): List<LyricsSource> {
        return try {
            val isFilterEnabled = settingsPreferences.isRelevanceFilterEnabled.first()
            val threshold = settingsPreferences.relevanceThreshold.first()
            val apiKey = settingsPreferences.serpApiCustomApiKey.first() ?: ""
            if (apiKey.isBlank()) return emptyList()

            // Fire both queries concurrently
            val (resultsA, resultsB) = coroutineScope {
                val deferredA = async {
                    settingsPreferences.incrementSerpApiRequests()
                    runSingleQuery(queryA, apiKey, songName, artistName, preferredDomains, isFilterEnabled, threshold)
                }
                val deferredB = async {
                    settingsPreferences.incrementSerpApiRequests()
                    runSingleQuery(queryB, apiKey, songName, artistName, preferredDomains, isFilterEnabled, threshold)
                }
                deferredA.await() to deferredB.await()
            }

            // Merge, deduplicate by normalised URL (keep first/higher-ranked occurrence)
            val seen = mutableSetOf<String>()
            val merged = (resultsA + resultsB).filter { source ->
                seen.add(normaliseUrl(source.url))
            }

            // Compute composite score and sort descending
            val queryTokens = RelevanceFilter.tokenize(songName) + RelevanceFilter.tokenize(artistName)
            merged
                .sortedByDescending { source -> compositeScore(source, queryTokens, preferredDomains) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun compositeScore(
        source: LyricsSource,
        queryTokens: Set<String>,
        preferredDomains: List<String>
    ): Float {
        val preferredBoost = if (preferredDomains.any { source.url.contains(it, ignoreCase = true) }) 2.0f else 0.0f
        val domain = domainScore(source.url)
        val resultTokens = RelevanceFilter.tokenize(source.title) + RelevanceFilter.tokenize(source.snippet ?: "")
        val relevance = RelevanceFilter.calculateRelevanceScore(queryTokens, resultTokens)
        return preferredBoost + domain + relevance
    }

    private suspend fun runSingleQuery(
        query: String,
        apiKey: String,
        songName: String,
        artistName: String,
        preferredDomains: List<String>,
        isFilterEnabled: Boolean,
        threshold: Float
    ): List<LyricsSource> {
        return try {
            val response = serpApiService.search(query = query, apiKey = apiKey)
            response.organicResults
                ?.take(10)
                ?.mapNotNull { result ->
                    val url = result.link ?: return@mapNotNull null
                    val title = result.title
                        ?: url.substringAfterLast("/").replace("-", " ").replaceFirstChar { it.uppercase() }

                    if (isFilterEnabled && !RelevanceFilter.isRelevant(
                            songName, artistName, title, result.snippet, threshold
                        )
                    ) return@mapNotNull null

                    val isPreferred = preferredDomains.any { domain -> url.contains(domain, ignoreCase = true) }
                    LyricsSource(title = title, url = url, snippet = result.snippet, isPreferred = isPreferred)
                } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
