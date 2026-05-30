package com.spotlyric.app.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spotlyric.app.presentation.theme.DarkBackground
import com.spotlyric.app.presentation.theme.ErrorRed
import com.spotlyric.app.presentation.theme.SpotifyGreen
import com.spotlyric.app.presentation.theme.TextSecondary

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { viewModel.handleAuthResponse(it) }
        }
    }

    // Collect auth intent to launch the OAuth browser flow
    LaunchedEffect(Unit) {
        viewModel.authIntent.collect { intent ->
            authLauncher.launch(intent)
        }
    }

    // Navigate when authenticated
    LaunchedEffect(state) {
        if (state is AuthState.Authenticated) {
            onAuthenticated()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // App logo
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "SpotLyric",
                modifier = Modifier.size(80.dp),
                tint = SpotifyGreen,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App title
            Text(
                text = "SpotLyric",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lyrics & translations for your\ncurrently playing Spotify tracks",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login button / loading state
            AnimatedVisibility(
                visible = state is AuthState.Unauthenticated || state is AuthState.Error,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { viewModel.startAuth() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = "Log in with Spotify",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Error message
                    if (state is AuthState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (state as AuthState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            // Loading indicator
            AnimatedVisibility(
                visible = state is AuthState.Loading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                CircularProgressIndicator(
                    color = SpotifyGreen,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}
