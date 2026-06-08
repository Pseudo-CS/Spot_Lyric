package com.spotlyric.app.presentation.lyrics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spotlyric.app.domain.model.Lyrics
import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.domain.usecase.AiTranslateUseCase
import com.spotlyric.app.domain.usecase.ExtractAndTranslateUseCase
import com.spotlyric.app.domain.usecase.GetLyricsUseCase
import com.spotlyric.app.domain.usecase.SearchLyricsSourcesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.spotlyric.app.domain.model.CurrentSongResult
import com.spotlyric.app.domain.usecase.GetCurrentSongUseCase

enum class ViewMode {
    NORMAL,
    AI_TRANSLATION,
}

sealed interface LyricsUiState {
    data object Loading : LyricsUiState
    data object NotFound : LyricsUiState
    data class Error(val message: String) : LyricsUiState

    data class Loaded(
        val lyrics: Lyrics,
        val songName: String,
        val artistName: String,
        val viewMode: ViewMode = ViewMode.NORMAL,
        val isGeneratingAi: Boolean = false,
        val showAiSourcesDialog: Boolean = false,
        val aiSources: List<LyricsSource> = emptyList(),
        val isLoadingAiSources: Boolean = false,
        // Source metadata for badge display
        val sourceUrl: String? = null,
        val extractionStage: String? = null,
        // Switch source dialog
        val showSwitchSourceDialog: Boolean = false,
        val switchSources: List<LyricsSource> = emptyList(),
        val isLoadingSwitchSources: Boolean = false,
    ) : LyricsUiState
}

@HiltViewModel
class LyricsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val aiTranslateUseCase: AiTranslateUseCase,
    private val searchLyricsSourcesUseCase: SearchLyricsSourcesUseCase,
    private val extractAndTranslateUseCase: ExtractAndTranslateUseCase,
    private val getCurrentSongUseCase: GetCurrentSongUseCase,
) : ViewModel() {

    private var currentSongName: String = java.net.URLDecoder.decode(
        savedStateHandle.get<String>("songName") ?: "", "UTF-8",
    )
    private var currentArtistName: String = java.net.URLDecoder.decode(
        savedStateHandle.get<String>("artistName") ?: "", "UTF-8",
    )

    private val _state = MutableStateFlow<LyricsUiState>(LyricsUiState.Loading)
    val state: StateFlow<LyricsUiState> = _state.asStateFlow()

    init {
        loadLyrics()
    }

    fun loadLyrics() {
        viewModelScope.launch {
            _state.value = LyricsUiState.Loading
            try {
                val lyrics = getLyricsUseCase(currentSongName, currentArtistName)
                if (lyrics != null) {
                    _state.value = LyricsUiState.Loaded(
                        lyrics = lyrics,
                        songName = currentSongName,
                        artistName = currentArtistName,
                        sourceUrl = lyrics.sourceUrl,
                        extractionStage = lyrics.extractionStage,
                    )
                } else {
                    _state.value = LyricsUiState.NotFound
                }
            } catch (e: Exception) {
                _state.value = LyricsUiState.Error(e.message ?: "Failed to load lyrics")
            }
        }
    }

    fun refreshCurrentSong() {
        viewModelScope.launch {
            _state.value = LyricsUiState.Loading
            try {
                when (val result = getCurrentSongUseCase()) {
                    is CurrentSongResult.Playing -> {
                        currentSongName = result.song.songName
                        currentArtistName = result.song.artistName
                        val lyrics = getLyricsUseCase(currentSongName, currentArtistName)
                        if (lyrics != null) {
                            _state.value = LyricsUiState.Loaded(
                                lyrics = lyrics,
                                songName = currentSongName,
                                artistName = currentArtistName,
                                sourceUrl = lyrics.sourceUrl,
                                extractionStage = lyrics.extractionStage,
                            )
                        } else {
                            _state.value = LyricsUiState.NotFound
                        }
                    }
                    is CurrentSongResult.NothingPlaying -> {
                        _state.value = LyricsUiState.Error("Nothing is currently playing on Spotify.")
                    }
                    is CurrentSongResult.Error -> {
                        _state.value = LyricsUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _state.value = LyricsUiState.Error(e.message ?: "Failed to refresh song")
            }
        }
    }

    fun toggleViewMode() {
        val current = _state.value
        if (current !is LyricsUiState.Loaded) return

        val newMode = when (current.viewMode) {
            ViewMode.NORMAL -> {
                if (current.lyrics.aiTranslation != null || current.lyrics.aiRomanized != null) {
                    ViewMode.AI_TRANSLATION
                } else {
                    return
                }
            }
            ViewMode.AI_TRANSLATION -> ViewMode.NORMAL
        }
        _state.value = current.copy(viewMode = newMode)
    }

    fun startAiTranslate() {
        val current = _state.value
        if (current !is LyricsUiState.Loaded) return

        val sourceUrl = current.lyrics.sourceUrl
        if (sourceUrl.isNullOrBlank()) return

        extractAndTranslate(sourceUrl)
    }

    fun dismissAiSourcesDialog() {
        _state.update {
            if (it is LyricsUiState.Loaded) it.copy(
                showAiSourcesDialog = false,
                aiSources = emptyList(),
            ) else it
        }
    }

    fun extractAndTranslate(sourceUrl: String) {
        val current = _state.value
        if (current !is LyricsUiState.Loaded) return

        viewModelScope.launch {
            _state.value = current.copy(
                showAiSourcesDialog = false,
                isGeneratingAi = true,
            )
            try {
                val updatedLyrics = aiTranslateUseCase(currentSongName, currentArtistName, sourceUrl)
                _state.value = current.copy(
                    lyrics = updatedLyrics,
                    isGeneratingAi = false,
                    viewMode = ViewMode.AI_TRANSLATION,
                    showAiSourcesDialog = false,
                    aiSources = emptyList(),
                )
            } catch (e: Exception) {
                _state.value = current.copy(
                    isGeneratingAi = false,
                    showAiSourcesDialog = false,
                )
            }
        }
    }

    /** Open the Switch Source dialog and load available sources. */
    fun startSwitchSource() {
        val current = _state.value
        if (current !is LyricsUiState.Loaded) return

        viewModelScope.launch {
            _state.update {
                if (it is LyricsUiState.Loaded) it.copy(
                    showSwitchSourceDialog = true,
                    isLoadingSwitchSources = true,
                ) else it
            }
            try {
                val sources = searchLyricsSourcesUseCase(currentSongName, currentArtistName)
                _state.update {
                    if (it is LyricsUiState.Loaded) it.copy(
                        switchSources = sources,
                        isLoadingSwitchSources = false,
                    ) else it
                }
            } catch (e: Exception) {
                _state.update {
                    if (it is LyricsUiState.Loaded) it.copy(
                        showSwitchSourceDialog = false,
                        isLoadingSwitchSources = false,
                    ) else it
                }
            }
        }
    }

    fun dismissSwitchSourceDialog() {
        _state.update {
            if (it is LyricsUiState.Loaded) it.copy(
                showSwitchSourceDialog = false,
                switchSources = emptyList(),
            ) else it
        }
    }

    /** Re-extract full lyrics from [sourceUrl] and update the displayed lyrics. */
    fun switchSource(sourceUrl: String) {
        val current = _state.value
        if (current !is LyricsUiState.Loaded) return

        viewModelScope.launch {
            _state.update {
                if (it is LyricsUiState.Loaded) it.copy(
                    showSwitchSourceDialog = false,
                    isGeneratingAi = true,
                ) else it
            }
            try {
                val updatedLyrics = extractAndTranslateUseCase(sourceUrl, currentSongName, currentArtistName)
                _state.update {
                    if (it is LyricsUiState.Loaded) it.copy(
                        lyrics = updatedLyrics,
                        isGeneratingAi = false,
                        sourceUrl = updatedLyrics.sourceUrl,
                        extractionStage = updatedLyrics.extractionStage,
                        switchSources = emptyList(),
                    ) else it
                }
            } catch (e: Exception) {
                _state.update {
                    if (it is LyricsUiState.Loaded) it.copy(
                        isGeneratingAi = false,
                        showSwitchSourceDialog = false,
                    ) else it
                }
            }
        }
    }

    fun hasAiContent(): Boolean {
        val current = _state.value
        if (current !is LyricsUiState.Loaded) return false
        return current.lyrics.aiTranslation != null || current.lyrics.aiRomanized != null
    }
}
