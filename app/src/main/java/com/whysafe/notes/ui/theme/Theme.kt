package com.whysafe.notes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Цвета заметок: индекс хранится в Note.color. */
val NoteColors = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFFFF4C2),
    Color(0xFFDCF5E3),
    Color(0xFFDDEBFF),
    Color(0xFFFDE2E4),
    Color(0xFFEDE4FF),
)

val PinColor = Color(0xFFE0A100)

private val Scheme = lightColorScheme(
    primary = Color(0xFF3D3A2E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE27A),
    onPrimaryContainer = Color(0xFF3D3A2E),
    background = Color(0xFFF6F5F1),
    surface = Color(0xFFF6F5F1),
)

@Composable
fun NotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
