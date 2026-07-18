package com.tang.prm.data.local.database

import android.util.Log
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
            // DB-Q-2 修复：移除逗号回退——逗号回退会静默拆分含逗号的字符串（如 "hello, world"），
            // 破坏数据语义。历史数据应在 Migration 中一次性迁移到 JSON 格式。
            // 此处记录警告并返回空列表，让数据损坏可见而非静默掩盖。
            Log.w("ListStringConverter", "无法解析为 JSON 数组: $value", e)
            emptyList()
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
