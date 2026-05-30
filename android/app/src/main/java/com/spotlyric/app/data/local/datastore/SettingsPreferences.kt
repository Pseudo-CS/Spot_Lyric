package com.spotlyric.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class SettingsPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val RELEVANCE_FILTER_ENABLED = booleanPreferencesKey("relevance_filter_enabled")
        private val RELEVANCE_THRESHOLD = floatPreferencesKey("relevance_threshold")
        
        private val SPOTIFY_REQUEST_COUNT = intPreferencesKey("spotify_request_count")
        private val GEMINI_REQUEST_COUNT = intPreferencesKey("gemini_request_count")
        private val SERPAPI_REQUEST_COUNT = intPreferencesKey("serpapi_request_count")
        private val LAST_TRACKED_MONTH = stringPreferencesKey("last_tracked_month")
        private val SPOTIFY_CUSTOM_CLIENT_ID = stringPreferencesKey("spotify_custom_client_id")
        private val SERPAPI_CUSTOM_API_KEY = stringPreferencesKey("serpapi_custom_api_key")
        private val GEMINI_CUSTOM_API_KEY = stringPreferencesKey("gemini_custom_api_key")
        private val GEMINI_CUSTOM_MODEL = stringPreferencesKey("gemini_custom_model")
        
        const val DEFAULT_FILTER_ENABLED = true
        const val DEFAULT_THRESHOLD = 0.2f
        const val DEFAULT_GEMINI_MODEL = "gemini-2.5-flash-lite"
    }

    val isRelevanceFilterEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[RELEVANCE_FILTER_ENABLED] ?: DEFAULT_FILTER_ENABLED
    }

    val relevanceThreshold: Flow<Float> = context.settingsDataStore.data.map { prefs ->
        prefs[RELEVANCE_THRESHOLD] ?: DEFAULT_THRESHOLD
    }

    val spotifyRequestCount: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        getMonthlyRequestCount(prefs, SPOTIFY_REQUEST_COUNT)
    }

    val geminiRequestCount: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        getMonthlyRequestCount(prefs, GEMINI_REQUEST_COUNT)
    }

    val serpApiRequestCount: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        getMonthlyRequestCount(prefs, SERPAPI_REQUEST_COUNT)
    }

    private fun getMonthlyRequestCount(prefs: Preferences, key: Preferences.Key<Int>): Int {
        val currentMonth = java.time.YearMonth.now().toString()
        val storedMonth = prefs[LAST_TRACKED_MONTH] ?: ""
        return if (storedMonth == currentMonth) {
            prefs[key] ?: 0
        } else {
            0
        }
    }

    suspend fun incrementSpotifyRequests() {
        incrementRequestCount(SPOTIFY_REQUEST_COUNT)
    }

    suspend fun incrementGeminiRequests() {
        incrementRequestCount(GEMINI_REQUEST_COUNT)
    }

    suspend fun incrementSerpApiRequests() {
        incrementRequestCount(SERPAPI_REQUEST_COUNT)
    }

    private suspend fun incrementRequestCount(key: Preferences.Key<Int>) {
        context.settingsDataStore.edit { prefs ->
            val currentMonth = java.time.YearMonth.now().toString()
            val storedMonth = prefs[LAST_TRACKED_MONTH] ?: ""
            if (storedMonth != currentMonth) {
                // Month changed, reset all counters and update stored month
                prefs[SPOTIFY_REQUEST_COUNT] = 0
                prefs[GEMINI_REQUEST_COUNT] = 0
                prefs[SERPAPI_REQUEST_COUNT] = 0
                prefs[LAST_TRACKED_MONTH] = currentMonth
            }
            val currentVal = prefs[key] ?: 0
            prefs[key] = currentVal + 1
        }
    }

    suspend fun setRelevanceFilterEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[RELEVANCE_FILTER_ENABLED] = enabled
        }
    }

    suspend fun setRelevanceThreshold(threshold: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[RELEVANCE_THRESHOLD] = threshold
        }
    }

    val spotifyCustomClientId: Flow<String?> = context.settingsDataStore.data.map { prefs ->
        prefs[SPOTIFY_CUSTOM_CLIENT_ID]
    }

    suspend fun setSpotifyCustomClientId(clientId: String?) {
        context.settingsDataStore.edit { prefs ->
            if (clientId.isNullOrBlank()) {
                prefs.remove(SPOTIFY_CUSTOM_CLIENT_ID)
            } else {
                prefs[SPOTIFY_CUSTOM_CLIENT_ID] = clientId.trim()
            }
        }
    }

    val serpApiCustomApiKey: Flow<String?> = context.settingsDataStore.data.map { prefs ->
        prefs[SERPAPI_CUSTOM_API_KEY]
    }

    suspend fun setSerpApiCustomApiKey(apiKey: String?) {
        context.settingsDataStore.edit { prefs ->
            if (apiKey.isNullOrBlank()) {
                prefs.remove(SERPAPI_CUSTOM_API_KEY)
            } else {
                prefs[SERPAPI_CUSTOM_API_KEY] = apiKey.trim()
            }
        }
    }

    val geminiCustomApiKey: Flow<String?> = context.settingsDataStore.data.map { prefs ->
        prefs[GEMINI_CUSTOM_API_KEY]
    }

    suspend fun setGeminiCustomApiKey(apiKey: String?) {
        context.settingsDataStore.edit { prefs ->
            if (apiKey.isNullOrBlank()) {
                prefs.remove(GEMINI_CUSTOM_API_KEY)
            } else {
                prefs[GEMINI_CUSTOM_API_KEY] = apiKey.trim()
            }
        }
    }

    val geminiCustomModel: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[GEMINI_CUSTOM_MODEL] ?: DEFAULT_GEMINI_MODEL
    }

    suspend fun setGeminiCustomModel(model: String?) {
        context.settingsDataStore.edit { prefs ->
            if (model.isNullOrBlank()) {
                prefs.remove(GEMINI_CUSTOM_MODEL)
            } else {
                prefs[GEMINI_CUSTOM_MODEL] = model.trim()
            }
        }
    }
}

