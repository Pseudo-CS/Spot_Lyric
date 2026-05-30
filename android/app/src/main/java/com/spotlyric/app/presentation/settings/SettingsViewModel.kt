package com.spotlyric.app.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spotlyric.app.data.local.datastore.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.spotlyric.app.domain.repository.BackupRepository

data class SettingsUiState(
    val isEnabled: Boolean = SettingsPreferences.DEFAULT_FILTER_ENABLED,
    val threshold: Float = SettingsPreferences.DEFAULT_THRESHOLD,
    val spotifyRequests: Int = 0,
    val geminiRequests: Int = 0,
    val serpApiRequests: Int = 0,
    val localStorageUsage: String = "Calculating...",
    val spotifyCustomClientId: String = "",
    val serpApiCustomApiKey: String = "",
    val geminiCustomApiKey: String = "",
    val geminiCustomModel: String = SettingsPreferences.DEFAULT_GEMINI_MODEL,
    val isBackupLoading: Boolean = false,
    val isImportLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
    private val backupRepository: BackupRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _storageUsage = MutableStateFlow("Calculating...")
    val storageUsage: StateFlow<String> = _storageUsage.asStateFlow()

    private val _isBackupLoading = MutableStateFlow(false)
    private val _isImportLoading = MutableStateFlow(false)

    init {
        refreshStorageUsage()
    }

    fun exportBackup(outputStream: java.io.OutputStream, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val result = backupRepository.exportData(outputStream)
            _isBackupLoading.value = false
            onResult(result)
            refreshStorageUsage()
        }
    }

    fun importBackup(inputStream: java.io.InputStream, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            _isImportLoading.value = true
            val result = backupRepository.importData(inputStream)
            _isImportLoading.value = false
            onResult(result)
            refreshStorageUsage()
        }
    }

    val state: StateFlow<SettingsUiState> = combine(
        settingsPreferences.isRelevanceFilterEnabled,
        settingsPreferences.relevanceThreshold,
        settingsPreferences.spotifyRequestCount,
        settingsPreferences.geminiRequestCount,
        settingsPreferences.serpApiRequestCount,
        _storageUsage,
        settingsPreferences.spotifyCustomClientId,
        settingsPreferences.serpApiCustomApiKey,
        settingsPreferences.geminiCustomApiKey,
        settingsPreferences.geminiCustomModel,
        _isBackupLoading,
        _isImportLoading
    ) { array ->
        SettingsUiState(
            isEnabled = array[0] as Boolean,
            threshold = array[1] as Float,
            spotifyRequests = array[2] as Int,
            geminiRequests = array[3] as Int,
            serpApiRequests = array[4] as Int,
            localStorageUsage = array[5] as String,
            spotifyCustomClientId = (array[6] as? String) ?: "",
            serpApiCustomApiKey = (array[7] as? String) ?: "",
            geminiCustomApiKey = (array[8] as? String) ?: "",
            geminiCustomModel = array[9] as String,
            isBackupLoading = array[10] as Boolean,
            isImportLoading = array[11] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun refreshStorageUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = getLocalStorageUsageBytes(context)
                _storageUsage.value = formatSize(bytes)
            } catch (e: Exception) {
                _storageUsage.value = "Unknown"
            }
        }
    }

    private fun getLocalStorageUsageBytes(context: Context): Long {
        val directories = listOf(
            context.filesDir,
            context.cacheDir,
            context.noBackupFilesDir,
            context.codeCacheDir,
            context.getDatabasePath("spotlyric.db").parentFile
        )
        return directories.filterNotNull().sumOf { calculateDirectorySize(it) }
    }

    private fun calculateDirectorySize(file: java.io.File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        
        var size = 0L
        val files = file.listFiles()
        if (files != null) {
            for (f in files) {
                size += calculateDirectorySize(f)
            }
        }
        return size
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> "%.2f MB".format(mb)
            kb >= 1.0 -> "%.2f KB".format(kb)
            else -> "$bytes Bytes"
        }
    }

    fun toggleRelevanceFilter() {
        viewModelScope.launch {
            val current = state.value.isEnabled
            settingsPreferences.setRelevanceFilterEnabled(!current)
        }
    }

    fun updateRelevanceThreshold(threshold: Float) {
        viewModelScope.launch {
            settingsPreferences.setRelevanceThreshold(threshold)
        }
    }

    fun updateSpotifyCustomClientId(clientId: String) {
        viewModelScope.launch {
            settingsPreferences.setSpotifyCustomClientId(clientId)
        }
    }

    fun updateSerpApiCustomApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsPreferences.setSerpApiCustomApiKey(apiKey)
        }
    }

    fun updateGeminiCustomApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsPreferences.setGeminiCustomApiKey(apiKey)
        }
    }

    fun updateGeminiCustomModel(model: String) {
        viewModelScope.launch {
            settingsPreferences.setGeminiCustomModel(model)
        }
    }
}
