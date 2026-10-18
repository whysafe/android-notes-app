package com.whysafe.notes.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.whysafe.notes.data.Note
import com.whysafe.notes.ui.theme.NoteColors
import com.whysafe.notes.ui.theme.PinColor

@Composable
fun NotesApp(vm: NotesViewModel = viewModel()) {
    val editing = vm.editing
    if (editing == null) {
        NotesListScreen(vm)
    } else {
        EditNoteScreen(note = editing, onSave = vm::save, onDelete = vm::delete)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesListScreen(vm: NotesViewModel) {
    val notes by vm.notes.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.lastDeleted) {
        if (vm.lastDeleted == null) return@LaunchedEffect
        val result = snackbar.showSnackbar("Заметка удалена", actionLabel = "Вернуть", duration = SnackbarDuration.Short)
        if (result == SnackbarResult.ActionPerformed) vm.undoDelete() else vm.clearDeleted()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Заметки", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = vm::createNote, containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Filled.Add, contentDescription = "Новая заметка")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Поиск по заметкам") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { vm.onQueryChange("") }) { Icon(Icons.Filled.Close, contentDescription = "Очистить поиск") }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
            )
            Spacer(Modifier.height(12.dp))
            if (notes.isEmpty()) {
                EmptyState(searching = query.isNotBlank())
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalItemSpacing = 10.dp,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(note, onClick = { vm.open(note) }, onPin = { vm.togglePin(note) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onPin: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = NoteColors.getOrElse(note.color) { Color.White }),
        border = BorderStroke(1.dp, Color(0x14000000)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(top = 10.dp),
                )
                IconButton(onClick = onPin, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = if (note.pinned) "Открепить" else "Закрепить",
                        tint = if (note.pinned) PinColor else Color(0x33000000),
                    )
                }
            }
            if (note.text.isNotBlank()) {
                Text(
                    text = note.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 10.dp, top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyState(searching: Boolean) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = if (searching) "Ничего не найдено. Попробуйте другое слово."
            else "Заметок пока нет.\nНажмите +, чтобы создать первую.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}
