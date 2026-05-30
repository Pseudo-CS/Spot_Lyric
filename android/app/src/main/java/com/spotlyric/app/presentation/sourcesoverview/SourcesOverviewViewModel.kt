package com.spotlyric.app.presentation.sourcesoverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao
import com.spotlyric.app.data.local.db.dao.SongLyricsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.net.URI
import javax.inject.Inject

data class DomainMetric(
    val domain: String,
    val songsCount: Int,
    val percentage: Float,
    val stage1Count: Int,
    val stage2Count: Int,
    val stage3Count: Int,
    val translationCount: Int,
    val translationRate: Float,
    val averageConfidence: Float
)

data class SourcesOverviewUiState(
    val totalBookmarks: Int = 0,
    val totalSuccessfulLyrics: Int = 0,
    val totalStage1: Int = 0,
    val totalStage2: Int = 0,
    val totalStage3: Int = 0,
    val metrics: List<DomainMetric> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SourcesOverviewViewModel @Inject constructor(
    private val bookmarkedSongDao: BookmarkedSongDao,
    private val songLyricsDao: SongLyricsDao
) : ViewModel() {

    val state: StateFlow<SourcesOverviewUiState> = combine(
        bookmarkedSongDao.getAllFlow(),
        songLyricsDao.getAllFlow()
    ) { bookmarks, lyricsList ->
        if (bookmarks.isEmpty()) {
            return@combine SourcesOverviewUiState(isLoading = false)
        }

        // Map lyrics by bookmarkId for quick lookup
        val lyricsMap = lyricsList.associateBy { it.bookmarkId }

        // Metrics grouping by domain
        val domainGroups = mutableMapOf<String, MutableList<Pair<com.spotlyric.app.data.local.db.entity.BookmarkedSongEntity, com.spotlyric.app.data.local.db.entity.SongLyricsEntity?>>>()

        var totalSuccessful = 0
        var totalSt1 = 0
        var totalSt2 = 0
        var totalSt3 = 0

        for (bookmark in bookmarks) {
            val lyrics = lyricsMap[bookmark.id]
            val url = bookmark.bookmarkedUrl
            val domain = getDomain(url)

            if (lyrics != null) {
                totalSuccessful++
                when (lyrics.extractionStage) {
                    "Domain Parser" -> totalSt1++
                    "Heuristics" -> totalSt2++
                    "AI" -> totalSt3++
                }
            }

            domainGroups.getOrPut(domain) { mutableListOf() }.add(bookmark to lyrics)
        }

        val metricsList = domainGroups.map { (domain, items) ->
            val count = items.size
            val pct = count.toFloat() / bookmarks.size

            var st1 = 0
            var st2 = 0
            var st3 = 0
            var transCount = 0
            var confidenceSum = 0f
            var confidenceCount = 0

            for ((_, lyrics) in items) {
                if (lyrics != null) {
                    when (lyrics.extractionStage) {
                        "Domain Parser" -> st1++
                        "Heuristics" -> st2++
                        "AI" -> st3++
                    }
                    if (lyrics.translatedLyrics.isNotBlank() || !lyrics.aiTranslation.isNullOrBlank()) {
                        transCount++
                    }
                    val confidence = lyrics.confidenceScore
                    if (confidence != null) {
                        confidenceSum += confidence
                        confidenceCount++
                    }
                }
            }

            val transRate = if (count > 0) transCount.toFloat() / count else 0f
            val avgConfidence = if (confidenceCount > 0) confidenceSum / confidenceCount else 0f

            DomainMetric(
                domain = domain,
                songsCount = count,
                percentage = pct,
                stage1Count = st1,
                stage2Count = st2,
                stage3Count = st3,
                translationCount = transCount,
                translationRate = transRate,
                averageConfidence = avgConfidence
            )
        }.sortedByDescending { it.songsCount }

        SourcesOverviewUiState(
            totalBookmarks = bookmarks.size,
            totalSuccessfulLyrics = totalSuccessful,
            totalStage1 = totalSt1,
            totalStage2 = totalSt2,
            totalStage3 = totalSt3,
            metrics = metricsList,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SourcesOverviewUiState()
    )

    private fun getDomain(url: String?): String {
        if (url.isNullOrBlank()) return "Unknown"
        return try {
            val host = URI(url).host ?: ""
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (e: Exception) {
            val cleaned = url.replace("https://", "").replace("http://", "")
            val slashIndex = cleaned.indexOf('/')
            val rawDomain = if (slashIndex != -1) cleaned.substring(0, slashIndex) else cleaned
            if (rawDomain.startsWith("www.")) rawDomain.substring(4) else rawDomain
        }
    }
}
