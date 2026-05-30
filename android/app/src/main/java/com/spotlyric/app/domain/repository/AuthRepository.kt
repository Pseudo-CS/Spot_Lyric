package com.spotlyric.app.domain.repository

import android.content.Intent
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isTokenValid(): Flow<Boolean>
    suspend fun getToken(): String?
    suspend fun buildAuthIntent(): Intent
    suspend fun handleAuthResponse(intent: Intent)
    suspend fun refreshToken(): Boolean
    suspend fun clearToken()
}
