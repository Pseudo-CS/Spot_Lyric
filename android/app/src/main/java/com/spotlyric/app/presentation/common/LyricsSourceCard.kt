package com.spotlyric.app.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.presentation.theme.WarningAmber

@Composable
fun LyricsSourceCard(
    source: LyricsSource,
    isBookmarked: Boolean,
    isBookmarking: Boolean,
    isExtracting: Boolean,
    onToggleBookmark: () -> Unit,
    onVisit: () -> Unit,
    onExtract: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreferred = source.isPreferred
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPreferred) WarningAmber.copy(alpha = 0.06f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isPreferred) BorderStroke(1.5.dp, WarningAmber.copy(alpha = 0.6f)) else null
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPreferred) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Preferred source",
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Column {
                        Text(
                            text = source.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isPreferred) WarningAmber else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = source.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bookmark Button
                    IconButton(
                        onClick = onToggleBookmark,
                        enabled = !isBookmarking
                    ) {
                        if (isBookmarking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Visit Web Button
                    IconButton(onClick = onVisit) {
                        Icon(
                            imageVector = Icons.Filled.Launch,
                            contentDescription = "Visit Website"
                        )
                    }
                }
            }

            if (!source.snippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = source.snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Extract Lyrics Button
            Button(
                onClick = onExtract,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExtracting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                if (isExtracting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extracting with Gemini...", fontSize = 14.sp)
                } else {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Gemini",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extract & Translate with Gemini", fontSize = 14.sp)
                }
            }
        }
    }
}
