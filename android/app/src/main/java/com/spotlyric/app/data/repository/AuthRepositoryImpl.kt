package com.spotlyric.app.data.repository

import android.content.Context
import android.content.Intent
import com.spotlyric.app.data.local.datastore.AuthPreferences
import com.spotlyric.app.data.local.datastore.SettingsPreferences
import com.spotlyric.app.data.remote.spotify.SpotifyAuthConfig
import com.spotlyric.app.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * AppAuth-based PKCE auth implementation.
 * No client secret — public client with code_verifier + code_challenge.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authPreferences: AuthPreferences,
    private val settingsPreferences: SettingsPreferences
) : AuthRepository {

    private val authService = AuthorizationService(context)

    override fun isTokenValid(): Flow<Boolean> = authPreferences.isTokenValid()

    override suspend fun getToken(): String? = authPreferences.getToken()

    override suspend fun buildAuthIntent(): Intent {
        val clientId = settingsPreferences.spotifyCustomClientId.first()
        if (clientId.isNullOrBlank()) {
            throw Exception("Spotify Client ID is not configured. Please set it in Settings.")
        }

        val authRequest = AuthorizationRequest.Builder(
            SpotifyAuthConfig.SERVICE_CONFIG,
            clientId,
            ResponseTypeValues.CODE,
            SpotifyAuthConfig.REDIRECT_URI
        )
            .setScope(SpotifyAuthConfig.SCOPE)
            .build()

        return authService.getAuthorizationRequestIntent(authRequest)
    }

    override suspend fun handleAuthResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        if (response != null) {
            val tokenRequest = response.createTokenExchangeRequest()
            
            val tokenResponse = suspendCoroutine<TokenResponse?> { continuation ->
                authService.performTokenRequest(tokenRequest) { tokenResponse, tokenException ->
                    if (tokenResponse != null) {
                        continuation.resume(tokenResponse)
                    } else {
                        continuation.resumeWithException(tokenException ?: Exception("Token exchange failed"))
                    }
                }
            }

            if (tokenResponse != null) {
                val accessToken = tokenResponse.accessToken ?: throw Exception("No access token returned")
                val refreshToken = tokenResponse.refreshToken
                val expiresIn = tokenResponse.accessTokenExpirationTime?.let {
                    (it - System.currentTimeMillis()) / 1000
                } ?: 3600L

                authPreferences.saveToken(accessToken, refreshToken, expiresIn)
            }
        } else if (exception != null) {
            throw exception
        }
    }

    override suspend fun refreshToken(): Boolean {
        // Spotify PKCE public clients can refresh tokens if we have a refresh token
        val refreshToken = authPreferences.getRefreshToken() ?: return false
        // AppAuth doesn't directly expose refresh for PKCE without a saved AuthState.
        // For simplicity, we redirect to login when token expires.
        return false
    }

    override suspend fun clearToken() {
        authPreferences.clearToken()
    }
}
