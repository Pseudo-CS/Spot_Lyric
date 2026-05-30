package com.spotlyric.app.presentation.player

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spotlyric.app.presentation.common.LoadingOverlay
import com.spotlyric.app.presentation.common.LyricsSourceCard
import com.spotlyric.app.presentation.theme.DarkBackground
import com.spotlyric.app.presentation.theme.ErrorRed
import com.spotlyric.app.presentation.theme.SpotifyGreen
import com.spotlyric.app.presentation.theme.TextSecondary

@Composable
fun PlayerScreen(
    onNavigateToLyrics: (songName: String, artistName: String) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PlayerUiEvent.ShowToast -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "PlayerContent",
        ) { currentState ->
            when (currentState) {
                is PlayerUiState.Loading -> {
                    LoadingOverlay(message = "Detecting current song…")
                }

                is PlayerUiState.Unauthenticated -> {
                    CenteredMessage(
                        icon = Icons.Default.MusicOff,
                        title = "Not Authenticated",
                        subtitle = "Please log in again to continue.",
                    )
                }

                is PlayerUiState.NothingPlaying -> {
                    CenteredMessage(
                        icon = Icons.Default.MusicOff,
                        title = "Nothing Playing",
                        subtitle = "Play a song on Spotify and tap refresh.",
                    )
                }

                is PlayerUiState.ShowingSources -> {
                    ShowingSourcesContent(
                        state = currentState,
                        onToggleBookmark = { viewModel.toggleBookmark(it) },
                        onVisit = { url ->
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                            )
                        },
                        onExtract = { viewModel.extractAndTranslate(it) },
                    )
                }

                is PlayerUiState.HasLyrics -> {
                    LaunchedEffect(currentState) {
                        onNavigateToLyrics(
                            currentState.song.songName,
                            currentState.song.artistName,
                        )
                    }
                    LoadingOverlay(message = "Loading lyrics…")
                }

                is PlayerUiState.Error -> {
                    CenteredMessage(
                        icon = Icons.Default.MusicOff,
                        title = "Error",
                        subtitle = currentState.message,
                        isError = true,
                    )
                }
            }
        }

        // Refresh FAB
        FloatingActionButton(
            onClick = { viewModel.refreshSong() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = SpotifyGreen,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
            )
        }
    }
}

@Composable
private fun ShowingSourcesContent(
    state: PlayerUiState.ShowingSources,
    onToggleBookmark: (com.spotlyric.app.domain.model.LyricsSource) -> Unit,
    onVisit: (String) -> Unit,
    onExtract: (com.spotlyric.app.domain.model.LyricsSource) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Song info card
        item {
            SongInfoCard(song = state.song)
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lyrics Sources",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (state.sources.isEmpty()) {
            item {
                Text(
                    text = "No lyrics sources found for this song.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        } else {
            items(state.sources, key = { it.url }) { source ->
                LyricsSourceCard(
                    source = source,
                    isBookmarked = source.url in state.bookmarkedUrls,
                    isBookmarking = state.bookmarkingUrl == source.url,
                    isExtracting = state.extractingUrl == source.url,
                    onToggleBookmark = { onToggleBookmark(source) },
                    onVisit = { onVisit(source.url) },
                    onExtract = { onExtract(source) },
                )
            }
        }

        // Bottom spacing for FAB
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun SongInfoCard(song: com.spotlyric.app.domain.model.Song) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Album art
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.albumArtUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album art for ${song.songName}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.songName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Now playing indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(SpotifyGreen),
            )
        }
    }
}

@Composable
private fun CenteredMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isError: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = if (isError) ErrorRed else TextSecondary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = if (isError) ErrorRed else MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
