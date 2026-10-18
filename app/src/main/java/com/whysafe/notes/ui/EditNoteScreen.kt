package com.whysafe.notes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whysafe.notes.data.Note
import com.whysafe.notes.ui.theme.NoteColors
import com.whysafe.notes.ui.theme.PinColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(note: Note, onSave: (Note) -> Unit, onDelete: (Note) -> Unit) {
    var title by rememberSaveable(note.id) { mutableStateOf(note.title) }
    var text by rememberSaveable(note.id) { mutableStateOf(note.text) }
    var color by rememberSaveable(note.id) { mutableIntStateOf(note.color) }
    var pinned by rememberSaveable(note.id) { mutableStateOf(note.pinned) }

    val current = { note.copy(title = title.trim(), text = text.trimEnd(), color = color, pinned = pinned) }
    BackHandler { onSave(current()) }

    val background = NoteColors.getOrElse(color) { Color.White }
    val transparent = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onSave(current()) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Сохранить и выйти")
                    }
                },
                actions = {
                    IconButton(onClick = { pinned = !pinned }) {
                        Icon(Icons.Filled.Star, contentDescription = if (pinned) "Открепить" else "Закрепить",
                            tint = if (pinned) PinColor else Color(0x55000000))
                    }
                    IconButton(onClick = { onDelete(current()) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить заметку")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background),
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding()) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Заголовок", fontSize = 22.sp) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                colors = transparent,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Текст заметки") },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = transparent,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                NoteColors.forEachIndexed { index, c ->
                    val selected = index == color
                    Column(
                        Modifier
                            .size(36.dp)
                            .background(c, CircleShape)
                            .border(if (selected) 3.dp else 1.dp, if (selected) Color(0xFF3D3A2E) else Color(0x22000000), CircleShape)
                            .clickable { color = index }
                            .semantics {
                                contentDescription = "Цвет ${index + 1}"
                                this.selected = selected
                            },
                    ) {}
                }
            }
            Text(
                text = if (note.title.isEmpty() && note.text.isEmpty()) "Новая заметка"
                else "Изменено " + SimpleDateFormat("d MMMM, HH:mm", Locale("ru")).format(Date(note.updatedAt)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
            )
        }
    }
}
