package com.spotlyric.app.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.spotlyric.app.data.local.datastore.SettingsPreferences
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spotlyric.app.BuildConfig
import com.spotlyric.app.presentation.theme.DarkBackground
import com.spotlyric.app.presentation.theme.DarkSurface
import com.spotlyric.app.presentation.theme.SpotifyGreen
import com.spotlyric.app.presentation.theme.TextSecondary
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSourcesOverview: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        viewModel.exportBackup(outputStream) { result ->
                            try {
                                outputStream.close()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (result.isSuccess) {
                                Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                val err = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                                Toast.makeText(context, "Export failed: $err", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to open output stream", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saving backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        viewModel.importBackup(inputStream) { result ->
                            try {
                                inputStream.close()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (result.isSuccess) {
                                Toast.makeText(context, "Backup imported successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                val err = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                                Toast.makeText(context, "Import failed: $err", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to open input stream", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error loading backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top App Bar Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Core Config Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LYRICS SEARCH RELEVANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Probabilistic Relevance Filter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Removes search results that are clearly a mismatch for the current song using token-overlap matching.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = state.isEnabled,
                            onCheckedChange = { viewModel.toggleRelevanceFilter() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpotifyGreen,
                                checkedTrackColor = SpotifyGreen.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // Sliding configs
                    AnimatedVisibility(
                        visible = state.isEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Relevance Threshold: ${"%.2f".format(state.threshold)} (${(state.threshold * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lower values are more lenient (allows minor typos and variations). Higher values are stricter (only matching titles pass).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Slider(
                                value = state.threshold,
                                onValueChange = { viewModel.updateRelevanceThreshold(it) },
                                valueRange = 0.0f..1.0f,
                                steps = 19, // 20 steps (0.05 increments)
                                colors = SliderDefaults.colors(
                                    thumbColor = SpotifyGreen,
                                    activeTrackColor = SpotifyGreen,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Min (0.0)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("Max (1.0)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Quick Presets",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PresetButton(
                                    label = "Low (0.10)",
                                    isSelected = state.threshold == 0.1f,
                                    onClick = { viewModel.updateRelevanceThreshold(0.1f) },
                                    modifier = Modifier.weight(1f)
                                )
                                PresetButton(
                                    label = "Default (0.20)",
                                    isSelected = state.threshold == 0.2f,
                                    onClick = { viewModel.updateRelevanceThreshold(0.2f) },
                                    modifier = Modifier.weight(1f)
                                )
                                PresetButton(
                                    label = "Strict (0.30)",
                                    isSelected = state.threshold == 0.3f,
                                    onClick = { viewModel.updateRelevanceThreshold(0.3f) },
                                    modifier = Modifier.weight(1f)
                                )
                             }
                        }
                    }
                }
            }

            // API Credentials & Keys Card
            var spotifyClientIdInput by remember(state.spotifyCustomClientId) {
                mutableStateOf(state.spotifyCustomClientId)
            }
            var serpApiKeyInput by remember(state.serpApiCustomApiKey) {
                mutableStateOf(state.serpApiCustomApiKey)
            }
            var geminiApiKeyInput by remember(state.geminiCustomApiKey) {
                mutableStateOf(state.geminiCustomApiKey)
            }
            var geminiModelInput by remember(state.geminiCustomModel) {
                mutableStateOf(state.geminiCustomModel)
            }

            val hasChanges = state.spotifyCustomClientId != spotifyClientIdInput ||
                    state.serpApiCustomApiKey != serpApiKeyInput ||
                    state.geminiCustomApiKey != geminiApiKeyInput ||
                    state.geminiCustomModel != geminiModelInput

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "API CREDENTIALS & KEYS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configure custom API keys. If left empty, default values will be used.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = spotifyClientIdInput,
                        onValueChange = { spotifyClientIdInput = it },
                        label = { Text("Spotify Client ID") },
                        placeholder = { Text("Default pre-compiled client ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            focusedLabelColor = SpotifyGreen,
                            cursorColor = SpotifyGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = serpApiKeyInput,
                        onValueChange = { serpApiKeyInput = it },
                        label = { Text("SerpAPI API Key") },
                        placeholder = { Text("Default pre-compiled SerpAPI key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            focusedLabelColor = SpotifyGreen,
                            cursorColor = SpotifyGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = geminiApiKeyInput,
                        onValueChange = { geminiApiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("Default pre-compiled Gemini key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            focusedLabelColor = SpotifyGreen,
                            cursorColor = SpotifyGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = geminiModelInput,
                        onValueChange = { geminiModelInput = it },
                        label = { Text("Gemini Model") },
                        placeholder = { Text("e.g. gemini-2.5-flash-lite") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            focusedLabelColor = SpotifyGreen,
                            cursorColor = SpotifyGreen
                        )
                    )

                    if (hasChanges) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    spotifyClientIdInput = state.spotifyCustomClientId
                                    serpApiKeyInput = state.serpApiCustomApiKey
                                    geminiApiKeyInput = state.geminiCustomApiKey
                                    geminiModelInput = state.geminiCustomModel
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                            ) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateSpotifyCustomClientId(spotifyClientIdInput)
                                    viewModel.updateSerpApiCustomApiKey(serpApiKeyInput)
                                    viewModel.updateGeminiCustomApiKey(geminiApiKeyInput)
                                    viewModel.updateGeminiCustomModel(geminiModelInput)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                            ) {
                                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // App Usage & Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "APP USAGE & STATISTICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    UsageRow(label = "Local Storage Usage", value = state.localStorageUsage)
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    UsageRow(label = "Spotify API Requests", value = "${state.spotifyRequests} requests")
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    UsageRow(label = "Gemini API Requests", value = "${state.geminiRequests} requests")
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    UsageRow(label = "SerpAPI Search Requests", value = "${state.serpApiRequests} requests")

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSourcesOverview() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sources Overview",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SpotifyGreen
                            )
                            Text(
                                text = "View metrics on all bookmarked lyrics sources",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Navigate to Sources Overview",
                            tint = SpotifyGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Note: API request counters automatically reset at the start of each calendar month. Local storage includes local SQLite database caches, settings preferences, and download caches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }

            // Backup & Import Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DATA MANAGEMENT (BACKUP & IMPORT)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Export all user data (including bookmarked songs, cached lyrics, custom sources, and settings preferences) to a single JSON file, or restore them from a previous backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                exportLauncher.launch("spotlyric_backup.json")
                            },
                            enabled = !state.isBackupLoading && !state.isImportLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (state.isBackupLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Export Backup", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                            },
                            enabled = !state.isBackupLoading && !state.isImportLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = SpotifyGreen
                            ),
                            border = BorderStroke(1.dp, SpotifyGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (state.isImportLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = SpotifyGreen,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Import Backup", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Explainer / How It Works card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "ABOUT RELEVANCE FILTERING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        letterSpacing = 1.sp
                    )

                    InfoRow(
                        title = "How it works",
                        description = "The filter computes token-overlap (Jaccard similarity) between your playing song's name/artist and the search result's title/snippet. Stopwords (like 'the', 'lyrics', 'feat') are ignored."
                    )

                    InfoRow(
                        title = "When to adjust",
                        description = "Increase the threshold (e.g. 0.30) if you see entirely unrelated song titles coming up. Decrease it (e.g. 0.10) if you get 'no results' due to language translations or typos."
                    )

                    InfoRow(
                        title = "Applies everywhere",
                        description = "The filter is applied to all organic results equally—including your preferred sources—ensuring you don't get junk results just because a domain is in your preferred list."
                    )
                }
            }
        }
    }
}

@Composable
fun PresetButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) SpotifyGreen else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) SpotifyGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InfoRow(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = SpotifyGreen,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun UsageRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = SpotifyGreen
        )
    }
}
