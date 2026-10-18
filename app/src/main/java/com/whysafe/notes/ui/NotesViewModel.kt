package com.whysafe.notes.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whysafe.notes.data.Note
import com.whysafe.notes.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NotesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = NoteRepository(app)
    private val query = MutableStateFlow("")

    val searchQuery: StateFlow<String> = query

    val notes: StateFlow<List<Note>> = combine(repo.notes, query) { list, q -> filterAndSort(list, q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), filterAndSort(repo.notes.value, ""))

    /** Открытая в редакторе заметка; null — показываем список. */
    var editing by mutableStateOf<Note?>(null)
        private set

    /** Последняя удалённая заметка, чтобы показать «Вернуть» в снекбаре. */
    var lastDeleted by mutableStateOf<Note?>(null)
        private set

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun createNote() {
        editing = Note(id = System.currentTimeMillis(), title = "", text = "")
    }

    fun open(note: Note) {
        editing = note
    }

    fun save(note: Note) {
        val original = repo.notes.value.find { it.id == note.id }
        when {
            note.isBlank && original != null -> repo.delete(note.id)            // стёрли всё — удаляем
            note.isBlank -> Unit                                                // пустую новую не сохраняем
            original == null || original.copy(updatedAt = 0) != note.copy(updatedAt = 0) ->
                repo.upsert(note.copy(updatedAt = System.currentTimeMillis()))
        }
        editing = null
    }

    fun togglePin(note: Note) = repo.upsert(note.copy(pinned = !note.pinned))

    /** [note]: текущее состояние из редактора, вместе с несохранёнными правками. */
    fun delete(note: Note) {
        val original = repo.notes.value.find { it.id == note.id }
        if (original != null) {
            repo.delete(note.id)
            // «Вернуть» восстанавливает заметку с правками; если всё стёрли, то исходную версию
            lastDeleted = when {
                note.isBlank -> original
                original.copy(updatedAt = 0) != note.copy(updatedAt = 0) -> note.copy(updatedAt = System.currentTimeMillis())
                else -> original
            }
        }
        // новую несохранённую заметку просто закрываем, без снекбара
        editing = null
    }

    fun undoDelete() {
        lastDeleted?.let(repo::upsert)
        lastDeleted = null
    }

    fun clearDeleted() {
        lastDeleted = null
    }

    private fun filterAndSort(list: List<Note>, q: String): List<Note> = list
        .filter { q.isBlank() || it.title.contains(q, ignoreCase = true) || it.text.contains(q, ignoreCase = true) }
        .sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt })
}
