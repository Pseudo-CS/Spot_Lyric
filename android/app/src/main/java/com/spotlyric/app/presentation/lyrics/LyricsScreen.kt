package com.spotlyric.app.presentation.lyrics

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spotlyric.app.presentation.common.LoadingOverlay
import com.spotlyric.app.presentation.common.LyricsPairRow
import com.spotlyric.app.presentation.theme.DarkBackground
import com.spotlyric.app.presentation.theme.ErrorRed
import com.spotlyric.app.presentation.theme.SecondaryBlue
import com.spotlyric.app.presentation.theme.SpotifyGreen
import com.spotlyric.app.presentation.theme.TextSecondary
import com.spotlyric.app.presentation.theme.WarningAmber
import kotlinx.coroutines.delay

enum class ScrollMode {
    OFF,
    SLOW,
    FAST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    songName: String,
    artistName: String,
    onBack: () -> Unit,
    viewModel: LyricsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Keep screen awake while viewing lyrics
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Intercept back actions (system gesture/button) to route to Library
    BackHandler {
        onBack()
    }

    // Auto-scroll variables
    var scrollMode by remember { mutableStateOf(ScrollMode.OFF) }
    var isPaused by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Reset auto-scroll mode and position when the song changes
    LaunchedEffect(songName, artistName) {
        scrollMode = ScrollMode.OFF
        isPaused = false
        listState.scrollToItem(0)
    }

    // Auto-scroll loop effect
    LaunchedEffect(scrollMode, isPaused) {
        if (scrollMode != ScrollMode.OFF && !isPaused) {
            val pxPerSecond = if (scrollMode == ScrollMode.SLOW) 37.5f else 62.5f
            val delayMs = 16L
            val pxPerStep = pxPerSecond * (delayMs.toFloat() / 1000f)
            while (true) {
                try {
                    listState.scroll(scrollPriority = MutatePriority.Default) {
                        scrollBy(pxPerStep)
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Catch manual user scroll cancellation and keep looping
                }
                delay(delayMs)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        // Top app bar
        TopAppBar(
            title = {
                val currentSongName = if (state is LyricsUiState.Loaded) (state as LyricsUiState.Loaded).songName else songName
                val currentArtistName = if (state is LyricsUiState.Loaded) (state as LyricsUiState.Loaded).artistName else artistName
                Column {
                    Text(
                        text = currentSongName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = currentArtistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
            actions = {
                // Auto Scroll toggle button
                if (state is LyricsUiState.Loaded) {
                    IconButton(onClick = {
                        if (isPaused) {
                            scrollMode = when (scrollMode) {
                                ScrollMode.OFF -> ScrollMode.SLOW
                                ScrollMode.SLOW -> ScrollMode.FAST
                                ScrollMode.FAST -> ScrollMode.OFF
                            }
                            isPaused = false
                        } else {
                            scrollMode = when (scrollMode) {
                                ScrollMode.OFF -> ScrollMode.SLOW
                                ScrollMode.SLOW -> ScrollMode.FAST
                                ScrollMode.FAST -> ScrollMode.OFF
                            }
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Auto Scroll",
                                tint = when {
                                    scrollMode == ScrollMode.OFF -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    isPaused -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    scrollMode == ScrollMode.SLOW -> SpotifyGreen
                                    else -> WarningAmber
                                }
                            )
                            if (scrollMode != ScrollMode.OFF) {
                                Text(
                                    text = if (isPaused) "PAUSED" else if (scrollMode == ScrollMode.SLOW) "1.5x" else "2.5x",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isPaused -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        scrollMode == ScrollMode.SLOW -> SpotifyGreen
                                        else -> WarningAmber
                                    },
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }

                // AI Translate button
                if (state is LyricsUiState.Loaded) {
                    IconButton(onClick = { viewModel.startAiTranslate() }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Translate",
                            tint = SecondaryBlue,
                        )
                    }
                }

                // Switch Source button
                if (state is LyricsUiState.Loaded) {
                    IconButton(onClick = { viewModel.startSwitchSource() }) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Switch Source",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        )
                    }
                }

                // Manual Song Refresh from Spotify button
                IconButton(onClick = { viewModel.refreshCurrentSong() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Song",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkBackground,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        // Content
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "LyricsContent",
        ) { currentState ->
            when (currentState) {
                is LyricsUiState.Loading -> {
                    LoadingOverlay(message = "Loading lyrics…")
                }

                is LyricsUiState.NotFound -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.height(64.dp),
                            tint = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Lyrics not found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Go back and extract lyrics from a source first.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                is LyricsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ErrorRed,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                is LyricsUiState.Loaded -> {
                    LoadedLyricsContent(
                        state = currentState,
                        listState = listState,
                        onToggleMode = { viewModel.toggleViewMode() },
                        onScreenClick = {
                            if (scrollMode != ScrollMode.OFF) {
                                isPaused = !isPaused
                            }
                        }
                    )
                }
            }
        }
    }

    // AI Sources Dialog
    if (state is LyricsUiState.Loaded) {
        val loadedState = state as LyricsUiState.Loaded
        if (loadedState.showAiSourcesDialog) {
            AiSourcesDialog(
                sources = loadedState.aiSources,
                isLoading = loadedState.isLoadingAiSources,
                onSelectSource = { url -> viewModel.extractAndTranslate(url) },
                onDismiss = { viewModel.dismissAiSourcesDialog() },
            )
        }
        if (loadedState.showSwitchSourceDialog) {
            SwitchSourceDialog(
                sources = loadedState.switchSources,
                isLoading = loadedState.isLoadingSwitchSources,
                onSelectSource = { url -> viewModel.switchSource(url) },
                onDismiss = { viewModel.dismissSwitchSourceDialog() },
            )
        }
    }

    // Loading overlay for AI generation / source switching
    if (state is LyricsUiState.Loaded && (state as LyricsUiState.Loaded).isGeneratingAi) {
        LoadingOverlay(message = "Loading lyrics…")
    }
}

@Composable
private fun LoadedLyricsContent(
    state: LyricsUiState.Loaded,
    listState: LazyListState,
    onToggleMode: () -> Unit,
    onScreenClick: () -> Unit,
) {
    val lyrics = state.lyrics
    val hasAi = lyrics.aiTranslation != null || lyrics.aiRomanized != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onScreenClick()
            }
    ) {
        // Source metadata badge
        val sourceDomain = state.sourceUrl
            ?.removePrefix("https://")?.removePrefix("http://")?.removePrefix("www.")
            ?.substringBefore("/")
        if (sourceDomain != null || state.extractionStage != null) {
            val badgeText = when {
                sourceDomain != null && state.extractionStage != null ->
                    "Source: $sourceDomain  •  ${state.extractionStage}"
                sourceDomain != null -> "Source: $sourceDomain"
                else -> "Stage: ${state.extractionStage}"
            }
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
            )
        }

        // View mode toggle chips
        if (hasAi) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                FilterChip(
                    selected = state.viewMode == ViewMode.NORMAL,
                    onClick = {
                        if (state.viewMode != ViewMode.NORMAL) onToggleMode()
                    },
                    label = { Text("Normal") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen.copy(alpha = 0.2f),
                        selectedLabelColor = SpotifyGreen,
                    ),
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = state.viewMode == ViewMode.AI_TRANSLATION,
                    onClick = {
                        if (state.viewMode != ViewMode.AI_TRANSLATION) onToggleMode()
                    },
                    label = { Text("AI Translation") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryBlue.copy(alpha = 0.2f),
                        selectedLabelColor = SecondaryBlue,
                    ),
                )
            }
        }

        // Lyrics pairs
        val originalLines = lyrics.originalLines()
        val translatedLines = when (state.viewMode) {
            ViewMode.NORMAL -> lyrics.translatedLines()
            ViewMode.AI_TRANSLATION -> {
                lyrics.aiTranslationLines() ?: lyrics.translatedLines()
            }
        }
        val romanizedLines = if (state.viewMode == ViewMode.AI_TRANSLATION) {
            lyrics.aiRomanizedLines()
        } else {
            null
        }

        val maxLines = maxOf(originalLines.size, translatedLines.size, romanizedLines?.size ?: 0)

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(
                items = (0 until maxLines).toList(),
                key = { index, _ -> index },
            ) { index, _ ->
                val original = originalLines.getOrNull(index) ?: ""
                val translated = translatedLines.getOrNull(index) ?: ""
                val romanized = romanizedLines?.getOrNull(index)

                LyricsPairRow(
                    originalLine = original,
                    translatedLine = translated,
                    romanizedLine = romanized,
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SwitchSourceDialog(
    sources: List<com.spotlyric.app.domain.model.LyricsSource>,
    isLoading: Boolean,
    onSelectSource: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Switch Lyrics Source",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            } else if (sources.isEmpty()) {
                Text(
                    text = "No sources found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(sources) { _, source ->
                        TextButton(
                            onClick = { onSelectSource(source.url) },
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun AiSourcesDialog(
    sources: List<com.spotlyric.app.domain.model.LyricsSource>,
    isLoading: Boolean,
    onSelectSource: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Source for AI Translation",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SecondaryBlue)
                }
            } else if (sources.isEmpty()) {
                Text(
                    text = "No sources found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(sources) { _, source ->
                        TextButton(
                            onClick = { onSelectSource(source.url) },
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
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
    )
}
