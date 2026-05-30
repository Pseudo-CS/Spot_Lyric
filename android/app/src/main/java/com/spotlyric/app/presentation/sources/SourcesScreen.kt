package com.spotlyric.app.presentation.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spotlyric.app.domain.model.PreferredSource
import com.spotlyric.app.presentation.theme.DarkBackground
import com.spotlyric.app.presentation.theme.DarkSurface
import com.spotlyric.app.presentation.theme.TextSecondary
import com.spotlyric.app.presentation.theme.WarningAmber

@Composable
fun SourcesScreen(viewModel: SourcesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().background(DarkSurface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = "Preferred Sources",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Results from these sites appear first and highlighted",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.sources.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Language, contentDescription = null,
                        modifier = Modifier.size(72.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No preferred sources", style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap + to add domains like \"genius.com\". Their results will appear ⭐ highlighted at the top of every lyrics search.",
                        style = MaterialTheme.typography.bodyLarge, color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.sources, key = { it.id }) { source ->
                        SourceItemCard(
                            source = source,
                            isDeleting = state.deletingId == source.id,
                            onToggle = { viewModel.toggleEnabled(source) },
                            onDelete = { viewModel.deleteSource(source.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.showAddDialog() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = WarningAmber, contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add source")
        }
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAddDialog() },
            title = { Text("Add Preferred Source", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter a lyrics site domain. Results from this site will appear highlighted at the top.",
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary
                    )
                    OutlinedTextField(
                        value = state.addDomainInput,
                        onValueChange = { viewModel.onDomainChange(it) },
                        label = { Text("Domain") },
                        placeholder = { Text("e.g. genius.com") },
                        singleLine = true,
                        isError = state.addError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.addNameInput,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Display Name (optional)") },
                        placeholder = { Text("e.g. Genius") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.addError != null) {
                        Text(state.addError!!, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = { Button(onClick = { viewModel.addSource() }) { Text("Add") } },
            dismissButton = { TextButton(onClick = { viewModel.dismissAddDialog() }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SourceItemCard(
    source: PreferredSource,
    isDeleting: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (source.enabled) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (source.enabled) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp, 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (source.enabled) WarningAmber else TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (source.enabled) MaterialTheme.colorScheme.onSurface else TextSecondary
                )
                Text(text = source.domain, style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary, fontSize = 11.sp)
            }
            Switch(
                checked = source.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = WarningAmber,
                    checkedTrackColor = WarningAmber.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
