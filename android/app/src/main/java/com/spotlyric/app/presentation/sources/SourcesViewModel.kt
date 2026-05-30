package com.spotlyric.app.presentation.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spotlyric.app.domain.model.PreferredSource
import com.spotlyric.app.domain.repository.SourcesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SourcesUiState(
    val sources: List<PreferredSource> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val addDomainInput: String = "",
    val addNameInput: String = "",
    val addError: String? = null,
    val deletingId: Long? = null
)

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val sourcesRepository: SourcesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SourcesUiState())
    val state: StateFlow<SourcesUiState> = _state.asStateFlow()

    init {
        sourcesRepository.getAllSourcesFlow()
            .onEach { sources -> _state.update { it.copy(sources = sources, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    fun showAddDialog() = _state.update { it.copy(showAddDialog = true, addDomainInput = "", addNameInput = "", addError = null) }
    fun dismissAddDialog() = _state.update { it.copy(showAddDialog = false) }
    fun onDomainChange(value: String) = _state.update { it.copy(addDomainInput = value, addError = null) }
    fun onNameChange(value: String) = _state.update { it.copy(addNameInput = value) }

    fun addSource() {
        val rawDomain = _state.value.addDomainInput.trim().lowercase()
            .removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/")
        if (rawDomain.isBlank() || !rawDomain.contains(".")) {
            _state.update { it.copy(addError = "Enter a valid domain (e.g. genius.com)") }
            return
        }
        val name = _state.value.addNameInput.trim().ifBlank {
            rawDomain.split(".").first().replaceFirstChar { it.uppercase() }
        }
        viewModelScope.launch {
            try {
                sourcesRepository.addSource(rawDomain, name)
                _state.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _state.update { it.copy(addError = "Domain already exists or error: ${e.message}") }
            }
        }
    }

    fun deleteSource(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(deletingId = id) }
            sourcesRepository.deleteSource(id)
            _state.update { it.copy(deletingId = null) }
        }
    }

    fun toggleEnabled(source: PreferredSource) {
        viewModelScope.launch { sourcesRepository.setEnabled(source.id, !source.enabled) }
    }
}
