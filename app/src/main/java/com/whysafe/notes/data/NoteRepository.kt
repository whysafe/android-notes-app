package com.whysafe.notes.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray

/**
 * Хранит заметки в SharedPreferences в виде JSON.
 * Для приложения на сотни заметок этого достаточно; при росте данных
 * репозиторий можно заменить на Room, не трогая UI.
 */
class NoteRepository(context: Context) {

    private val prefs = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
    private val _notes = MutableStateFlow(load())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    fun upsert(note: Note) {
        _notes.update { list -> list.filterNot { it.id == note.id } + note }
        persist()
    }

    fun delete(id: Long) {
        _notes.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    private fun load(): List<Note> {
        if (!prefs.contains(KEY)) return welcomeNotes()
        return runCatching {
            val arr = JSONArray(prefs.getString(KEY, "[]"))
            List(arr.length()) { i -> Note.fromJson(arr.getJSONObject(i)) }
        }.getOrDefault(emptyList())
    }

    private fun persist() {
        val arr = JSONArray()
        _notes.value.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private fun welcomeNotes(): List<Note> {
        val now = System.currentTimeMillis()
        return listOf(
            Note(1, "Добро пожаловать 👋", "Нажмите на заметку, чтобы открыть. Звёздочка закрепляет её наверху списка.", 1, true, now),
            Note(2, "Список покупок", "• Молоко\n• Хлеб\n• Кофе в зёрнах\n• Батарейки AA", 2, false, now - 60_000),
            Note(3, "Идеи для курсовой", "Приложение для заметок на Jetpack Compose: поиск, цвета, закрепление, отмена удаления.", 3, false, now - 120_000),
        )
    }

    private companion object {
        const val KEY = "notes_json"
    }
}
