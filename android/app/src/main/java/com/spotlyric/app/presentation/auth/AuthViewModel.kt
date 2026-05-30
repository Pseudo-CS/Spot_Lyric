package com.spotlyric.app.presentation.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spotlyric.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _authIntent = MutableSharedFlow<Intent>()
    val authIntent: SharedFlow<Intent> = _authIntent.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.isTokenValid().collect { valid ->
                _state.value = if (valid) AuthState.Authenticated else AuthState.Unauthenticated
            }
        }
    }

    fun startAuth() {
        viewModelScope.launch {
            try {
                _state.value = AuthState.Loading
                val intent = authRepository.buildAuthIntent()
                _authIntent.emit(intent)
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Failed to start authentication")
            }
        }
    }

    fun handleAuthResponse(intent: Intent) {
        viewModelScope.launch {
            try {
                _state.value = AuthState.Loading
                authRepository.handleAuthResponse(intent)
                _state.value = AuthState.Authenticated
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }
}
