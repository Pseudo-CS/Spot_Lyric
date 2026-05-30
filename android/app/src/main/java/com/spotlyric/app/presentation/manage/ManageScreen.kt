package com.spotlyric.app.presentation.manage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spotlyric.app.domain.model.BookmarkedSong
import com.spotlyric.app.presentation.theme.ChipAiRomanized
import com.spotlyric.app.presentation.theme.ChipAiTranslation
import com.spotlyric.app.presentation.theme.ChipLyrics
import com.spotlyric.app.presentation.theme.DarkBackground
import com.spotlyric.app.presentation.theme.ErrorRed
import com.spotlyric.app.presentation.theme.SecondaryBlue
import com.spotlyric.app.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManageScreen(
    onNavigateToLyrics: (songName: String, artistName: String) -> Unit,
    viewModel: ManageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ManageUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        // Search bar
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.search(it) },
                    onSearch = { },
                    expanded = false,
                    onExpandedChange = { },
                    placeholder = { Text("Search songs…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                )
            },
            expanded = false,
            onExpandedChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) { }

        // Stats row
        val totalSongs = state.songs.size
        val withLyrics = state.songs.count { it.hasLyrics }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AssistChip(
                onClick = { },
                label = { Text("$totalSongs songs") },
                leadingIcon = {
                    Icon(
                        Icons.Default.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
            AssistChip(
                onClick = { },
                label = { Text("$withLyrics with lyrics") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = ChipLyrics.copy(alpha = 0.15f),
                    labelColor = ChipLyrics,
                ),
            )
        }

        // Song list
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        AnimatedVisibility(
            visible = !state.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (state.songs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No bookmarked songs",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bookmark lyrics sources from the Player tab to see them here.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.songs, key = { it.id }) { song ->
                        SongManageCard(
                            song = song,
                            isDeleting = state.deletingId == song.id,
                            isAiTranslating = state.aiTranslatingId == song.id,
                            onViewLyrics = {
                                if (song.hasLyrics) {
                                    onNavigateToLyrics(song.songName, song.artistName)
                                }
                            },
                            onDelete = { viewModel.showDeleteConfirm(song.id) },
                            onAiTranslate = { viewModel.startAiTranslate(song) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Delete confirmation dialog
    state.showDeleteConfirmId?.let { id ->
        val song = state.songs.find { it.id == id }
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirm() },
            title = { Text("Delete Song") },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${song?.songName ?: "this song"}\"? " +
                        "This will remove all associated lyrics and translations.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteSong(id) },
                ) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirm() }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    // AI Sources dialog
    if (state.showAiSourcesDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAiSourcesDialog() },
            title = {
                Text(
                    text = "Select Source for AI Translation",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                if (state.isLoadingAiSources) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = SecondaryBlue)
                    }
                } else if (state.aiSources.isEmpty()) {
                    Text(
                        text = "No sources found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.aiSources) { source ->
                            TextButton(
                                onClick = { viewModel.confirmAiTranslate(source.url) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = source.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (source.snippet != null) {
                                        Text(
                                            text = source.snippet,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAiSourcesDialog() }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongManageCard(
    song: BookmarkedSong,
    isDeleting: Boolean,
    isAiTranslating: Boolean,
    onViewLyrics: () -> Unit,
    onDelete: () -> Unit,
    onAiTranslate: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onViewLyrics,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Song info + actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.songName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Action buttons
                if (isAiTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp),
                        color = SecondaryBlue,
                        strokeWidth = 2.dp,
                    )
                } else {
                    IconButton(
                        onClick = onAiTranslate,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Translate",
                            tint = SecondaryBlue,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp),
                        color = ErrorRed,
                        strokeWidth = 2.dp,
                    )
                } else {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (song.hasLyrics) {
                    StatusBadge("Has Lyrics", ChipLyrics)
                }
                if (song.hasAiTranslation) {
                    StatusBadge("AI Translation", ChipAiTranslation)
                }
                if (song.hasAiRomanized) {
                    StatusBadge("AI Romanized", ChipAiRomanized)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
