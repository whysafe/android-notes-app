package com.whysafe.notes.data

import org.json.JSONObject

data class Note(
    val id: Long,
    val title: String,
    val text: String,
    val color: Int = 0,
    val pinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val isBlank: Boolean get() = title.isBlank() && text.isBlank()

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("text", text)
        .put("color", color)
        .put("pinned", pinned)
        .put("updatedAt", updatedAt)

    companion object {
        fun fromJson(o: JSONObject) = Note(
            id = o.getLong("id"),
            title = o.optString("title"),
            text = o.optString("text"),
            color = o.optInt("color"),
            pinned = o.optBoolean("pinned"),
            updatedAt = o.optLong("updatedAt"),
        )
    }
}
