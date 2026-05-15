package com.tang.prm.data.local.database

import androidx.room.TypeConverter
import org.json.JSONArray

class ListStringConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(value)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        if (list.isEmpty()) return "[]"
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }
}
