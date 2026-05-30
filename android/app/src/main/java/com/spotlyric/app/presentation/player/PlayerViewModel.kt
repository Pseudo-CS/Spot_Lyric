package com.spotlyric.app.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spotlyric.app.domain.model.CurrentSongResult
import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.domain.model.Song
import com.spotlyric.app.domain.usecase.ExtractAndTranslateUseCase
import com.spotlyric.app.domain.usecase.GetCurrentSongUseCase
import com.spotlyric.app.domain.usecase.GetLyricsUseCase
import com.spotlyric.app.domain.usecase.SearchLyricsSourcesUseCase
import com.spotlyric.app.domain.usecase.ToggleBookmarkUseCase
import com.spotlyric.app.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

sealed interface PlayerUiEvent {
    data class ShowToast(val message: String) : PlayerUiEvent
}

sealed interface PlayerUiState {
    data object Loading : PlayerUiState
    data object Unauthenticated : PlayerUiState
    data object NothingPlaying : PlayerUiState

    data class ShowingSources(
        val song: Song,
        val sources: List<LyricsSource>,
        val bookmarkedUrls: List<String>,
        val bookmarkingUrl: String? = null,
        val extractingUrl: String? = null,
    ) : PlayerUiState

    data class HasLyrics(
        val song: Song,
    ) : PlayerUiState

    data class Error(val message: String) : PlayerUiState
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getCurrentSongUseCase: GetCurrentSongUseCase,
    private val searchLyricsSourcesUseCase: SearchLyricsSourcesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val extractAndTranslateUseCase: ExtractAndTranslateUseCase,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _uiEvent = kotlinx.coroutines.channels.Channel<PlayerUiEvent>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _state = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    init {
        fetchCurrentSong()
    }

    fun fetchCurrentSong() {
        viewModelScope.launch {
            _state.value = PlayerUiState.Loading
            try {
                when (val result = getCurrentSongUseCase()) {
                    is CurrentSongResult.Playing -> {
                        val song = result.song
                        // Check if we already have lyrics for this song
                        val existingLyrics = getLyricsUseCase(song.songName, song.artistName)
                        if (existingLyrics != null) {
                            _state.value = PlayerUiState.HasLyrics(song)
                        } else {
                            loadSources(song)
                        }
                    }
                    is CurrentSongResult.NothingPlaying -> {
                        _state.value = PlayerUiState.NothingPlaying
                    }
                    is CurrentSongResult.Error -> {
                        if (result.message.contains("Not authenticated", ignoreCase = true)) {
                            _state.value = PlayerUiState.Unauthenticated
                        } else {
                            _state.value = PlayerUiState.Error(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = PlayerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun loadSources(song: Song) {
        try {
            val sources = searchLyricsSourcesUseCase(song.songName, song.artistName)
            val bookmarkedUrls = playerRepository.getBookmarkedUrls(song.songName, song.artistName)
            _state.value = PlayerUiState.ShowingSources(
                song = song,
                sources = sources,
                bookmarkedUrls = bookmarkedUrls,
            )
        } catch (e: Exception) {
            _state.value = PlayerUiState.Error(e.message ?: "Failed to load sources")
        }
    }

    fun toggleBookmark(source: LyricsSource) {
        val current = _state.value
        if (current !is PlayerUiState.ShowingSources) return

        viewModelScope.launch {
            _state.value = current.copy(bookmarkingUrl = source.url)
            try {
                val isNowBookmarked = toggleBookmarkUseCase(
                    songName = current.song.songName,
                    artistName = current.song.artistName,
                    url = source.url,
                    title = source.title,
                )
                val updatedBookmarks = if (isNowBookmarked) {
                    current.bookmarkedUrls + source.url
                } else {
                    current.bookmarkedUrls - source.url
                }
                _state.value = current.copy(
                    bookmarkedUrls = updatedBookmarks,
                    bookmarkingUrl = null,
                )
            } catch (e: Exception) {
                _state.value = current.copy(bookmarkingUrl = null)
            }
        }
    }

    fun extractAndTranslate(source: LyricsSource) {
        val current = _state.value
        if (current !is PlayerUiState.ShowingSources) return

        viewModelScope.launch {
            val activeState = _state.value
            if (activeState is PlayerUiState.ShowingSources) {
                _state.value = activeState.copy(extractingUrl = source.url)
            }
            try {
                extractAndTranslateUseCase(
                    url = source.url,
                    songName = current.song.songName,
                    artistName = current.song.artistName,
                )
                _state.value = PlayerUiState.HasLyrics(current.song)
            } catch (e: Exception) {
                android.util.Log.w("PlayerViewModel", "Failed for ${source.url}: ${e.message}")
                _state.value = current.copy(extractingUrl = null)
                _uiEvent.send(PlayerUiEvent.ShowToast("Extraction failed: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    fun refreshSong() {
        fetchCurrentSong()
    }
}
