package com.tang.prm.domain.util

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val listFieldJson = Json { ignoreUnknownKeys = true; isLenient = true }

/**
 * 解析 JSON 数组字符串或逗号分隔字符串为列表。
 * 如 ["读书","运动"] → [读书, 运动]；兼容旧数据 "读书, 运动" → [读书, 运动]。
 * 空白项会被过滤。
 */
fun parseListField(value: String?): List<String> {
    if (value.isNullOrBlank()) return emptyList()
    return try {
        listFieldJson.decodeFromString(ListSerializer(String.serializer()), value)
            .filter { it.isNotBlank() }
    } catch (e: Exception) {
        value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

/**
 * 将列表序列化为 JSON 数组字符串。空列表返回 null（便于 Room/DB 存储）。
 */
fun serializeListField(list: List<String>): String? {
    if (list.isEmpty()) return null
    return listFieldJson.encodeToString(ListSerializer(String.serializer()), list)
}
