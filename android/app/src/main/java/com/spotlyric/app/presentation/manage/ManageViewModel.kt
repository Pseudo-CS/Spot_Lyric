package com.spotlyric.app.presentation.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spotlyric.app.domain.model.BookmarkedSong
import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.domain.usecase.DeleteSongUseCase
import com.spotlyric.app.domain.usecase.GetSongsUseCase
import com.spotlyric.app.domain.usecase.SearchLyricsSourcesUseCase
import com.spotlyric.app.domain.repository.ManageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageUiState(
    val songs: List<BookmarkedSong> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val deletingId: Long? = null,
    val aiTranslatingId: Long? = null,
    val showAiSourcesDialog: Boolean = false,
    val aiSources: List<LyricsSource> = emptyList(),
    val isLoadingAiSources: Boolean = false,
    val pendingAiSongId: Long? = null,
    val showDeleteConfirmId: Long? = null,
)

sealed interface ManageUiEvent {
    data class ShowToast(val message: String) : ManageUiEvent
}

@HiltViewModel
class ManageViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val searchLyricsSourcesUseCase: SearchLyricsSourcesUseCase,
    private val manageRepository: ManageRepository,
) : ViewModel() {

    private val _uiEvent = kotlinx.coroutines.channels.Channel<ManageUiEvent>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _state = MutableStateFlow(ManageUiState())
    val state: StateFlow<ManageUiState> = _state.asStateFlow()

    private var songsJob: Job? = null

    init {
        observeSongs("")
    }

    fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        observeSongs(query)
    }

    private fun observeSongs(query: String) {
        songsJob?.cancel()
        songsJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getSongsUseCase.search(query).collect { songs ->
                _state.update { it.copy(songs = songs, isLoading = false) }
            }
        }
    }

    fun showDeleteConfirm(id: Long) {
        _state.update { it.copy(showDeleteConfirmId = id) }
    }

    fun dismissDeleteConfirm() {
        _state.update { it.copy(showDeleteConfirmId = null) }
    }

    fun deleteSong(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(deletingId = id, showDeleteConfirmId = null) }
            try {
                deleteSongUseCase(id)
            } catch (_: Exception) {
                // Song list will update via Flow automatically
            }
            _state.update { it.copy(deletingId = null) }
        }
    }

    fun startAiTranslate(song: BookmarkedSong) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    pendingAiSongId = song.id,
                    showAiSourcesDialog = true,
                    isLoadingAiSources = true,
                )
            }
            try {
                val sources = searchLyricsSourcesUseCase(song.songName, song.artistName)
                _state.update {
                    it.copy(
                        aiSources = sources,
                        isLoadingAiSources = false,
                    )
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        showAiSourcesDialog = false,
                        isLoadingAiSources = false,
                        pendingAiSongId = null,
                    )
                }
            }
        }
    }

    fun dismissAiSourcesDialog() {
        _state.update {
            it.copy(
                showAiSourcesDialog = false,
                aiSources = emptyList(),
                pendingAiSongId = null,
            )
        }
    }

    fun confirmAiTranslate(sourceUrl: String) {
        val songId = _state.value.pendingAiSongId ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    showAiSourcesDialog = false,
                    aiTranslatingId = songId,
                    aiSources = emptyList(),
                    pendingAiSongId = null,
                )
            }
            try {
                manageRepository.generateAiTranslation(songId, sourceUrl)
            } catch (e: Exception) {
                android.util.Log.e("ManageViewModel", "Error generating AI translation", e)
                _uiEvent.send(ManageUiEvent.ShowToast(e.message ?: "AI translation failed"))
            }
            _state.update { it.copy(aiTranslatingId = null) }
        }
    }
}
