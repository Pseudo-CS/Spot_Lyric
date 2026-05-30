package com.spotlyric.app.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LyricsPairRow(
    originalLine: String,
    translatedLine: String,
    romanizedLine: String? = null,
    modifier: Modifier = Modifier
) {
    var activeWordIndex by remember { mutableStateOf<Int?>(null) }
    var activeWordTranslation by remember { mutableStateOf("") }

    val originalWords = remember(originalLine) { originalLine.trim().split(Regex("\\s+")) }
    val translatedWords = remember(translatedLine) { translatedLine.trim().split(Regex("\\s+")) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Original line with clickable words for word-to-word tooltip mapping
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            originalWords.forEachIndexed { index, word ->
                Box(modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)) {
                    val cleanWord = word.replace(Regex("[.,!?;:()\"']"), "")
                    val correspondingTranslation = translatedWords.getOrNull(index) ?: "No translation"

                    Text(
                        text = word,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeWordIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .clickable {
                                if (activeWordIndex == index) {
                                    activeWordIndex = null
                                } else {
                                    activeWordIndex = index
                                    activeWordTranslation = correspondingTranslation
                                }
                            }
                    )

                    if (activeWordIndex == index) {
                        Popup(
                            onDismissRequest = { activeWordIndex = null },
                            alignment = androidx.compose.ui.Alignment.BottomCenter
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = activeWordTranslation,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Romanized line (if present)
        if (!romanizedLine.isNullOrBlank()) {
            Text(
                text = romanizedLine,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Full translation line below
        if (translatedLine.isNotBlank()) {
            Text(
                text = translatedLine,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
