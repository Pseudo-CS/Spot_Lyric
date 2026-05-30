package com.spotlyric.app.domain.usecase

import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.domain.repository.PlayerRepository
import com.spotlyric.app.domain.repository.SourcesRepository
import javax.inject.Inject

class SearchLyricsSourcesUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val sourcesRepository: SourcesRepository
) {
    suspend operator fun invoke(songName: String, artistName: String): List<LyricsSource> {
        val preferredDomains = sourcesRepository.getEnabledSources().map { it.domain }
        return playerRepository.searchLyricsSources(songName, artistName, preferredDomains)
    }
}
