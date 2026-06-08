package com.spotlyric.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val SPOTIFY_TOKEN = stringPreferencesKey("spotify_token")
        private val SPOTIFY_REFRESH_TOKEN = stringPreferencesKey("spotify_refresh_token")
        private val SPOTIFY_EXPIRES_AT = longPreferencesKey("spotify_expires_at")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SPOTIFY_TOKEN]
    }

    suspend fun saveToken(accessToken: String, refreshToken: String?, expiresIn: Long) {
        context.dataStore.edit { prefs ->
            prefs[SPOTIFY_TOKEN] = accessToken
            refreshToken?.let { prefs[SPOTIFY_REFRESH_TOKEN] = it }
            prefs[SPOTIFY_EXPIRES_AT] = System.currentTimeMillis() / 1000 + expiresIn
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[SPOTIFY_TOKEN] }.first()
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.map { it[SPOTIFY_REFRESH_TOKEN] }.first()
    }

    suspend fun getExpiresAt(): Long? {
        return context.dataStore.data.map { it[SPOTIFY_EXPIRES_AT] }.first()
    }

    /**
     * Token validity check with 60-second buffer — mirrors Django's is_token_expired().
     * A session is valid if we have an access token and it's either not expired or we have a refresh token.
     */
    fun isTokenValid(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        val expiresAt = prefs[SPOTIFY_EXPIRES_AT]
        val now = System.currentTimeMillis() / 1000
        val hasAccessToken = !prefs[SPOTIFY_TOKEN].isNullOrBlank()
        val isExpired = expiresAt == null || now >= (expiresAt - 60)
        
        val hasRefreshToken = !prefs[SPOTIFY_REFRESH_TOKEN].isNullOrBlank()
        
        hasAccessToken && (!isExpired || hasRefreshToken)
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(SPOTIFY_TOKEN)
            prefs.remove(SPOTIFY_REFRESH_TOKEN)
            prefs.remove(SPOTIFY_EXPIRES_AT)
        }
    }
}
