package com.tang.prm.feature.chat

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// 结构化对话行 — 用于 JSON 序列化
@Serializable
data class DialogueLineDto(
    val speaker: String,
    val content: String,
    val imageUri: String? = null
)

data class DialogueLineInput(
    val id: Long = generateLineId(),
    val isMe: Boolean = true,
    val content: String = "",
    val imageUri: String? = null
)

private fun generateLineId(): Long {
    val timestamp = System.currentTimeMillis()
    val seq = lineIdCounter.incrementAndGet() and 0x3FFFFF
    return (timestamp shl 22) or seq
}

private val lineIdCounter = java.util.concurrent.atomic.AtomicLong(0)

class DialogueLineManager @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    fun createLine(speaker: String, text: String): DialogueLineInput {
        val isMe = speaker == "我"
        val (content, uri) = extractImageTag(text)
        return DialogueLineInput(isMe = isMe, content = content, imageUri = uri)
    }

    fun addLine(lines: List<DialogueLineInput>, speaker: String): List<DialogueLineInput> {
        val isMe = speaker == "我"
        val newLine = DialogueLineInput(isMe = isMe)
        return lines + newLine
    }

    fun updateLine(lines: List<DialogueLineInput>, lineId: Long, text: String): List<DialogueLineInput> {
        return lines.map { line ->
            if (line.id == lineId) line.copy(content = text) else line
        }
    }

    fun updateLineImage(lines: List<DialogueLineInput>, lineId: Long, uri: String?): List<DialogueLineInput> {
        return lines.map { line ->
            if (line.id == lineId) line.copy(imageUri = uri) else line
        }
    }

    fun toggleSpeaker(lines: List<DialogueLineInput>, lineId: Long): List<DialogueLineInput> {
        return lines.map { line ->
            if (line.id == lineId) line.copy(isMe = !line.isMe) else line
        }
    }

    fun removeLine(lines: List<DialogueLineInput>, lineId: Long): List<DialogueLineInput> {
        return lines.filter { it.id != lineId }
    }

    fun moveLine(lines: List<DialogueLineInput>, fromIndex: Int, toIndex: Int): List<DialogueLineInput> {
        if (fromIndex < 0 || fromIndex >= lines.size) return lines
        if (toIndex < 0 || toIndex >= lines.size) return lines
        val mutableLines = lines.toMutableList()
        val item = mutableLines.removeAt(fromIndex)
        mutableLines.add(toIndex, item)
        return mutableLines
    }

    fun parseDescriptionToLines(description: String?, contactName: String?): List<DialogueLineInput> {
        if (description.isNullOrBlank()) return emptyList()

        // 优先尝试 JSON 格式（新格式）
        try {
            val dtos = json.decodeFromString<List<DialogueLineDto>>(description)
            if (dtos.isNotEmpty()) {
                return dtos.map { dto ->
                    val (text, uri) = extractImageTag(dto.content)
                    DialogueLineInput(
                        isMe = dto.speaker == "我",
                        content = text,
                        imageUri = uri ?: dto.imageUri
                    )
                }
            }
        } catch (_: Exception) {
            // 非 JSON，走旧格式解析
        }

        // 旧格式：按行解析（兼容历史数据）
        return parseLegacyDescription(description, contactName)
    }

    private fun parseLegacyDescription(description: String, contactName: String?): List<DialogueLineInput> {
        val lines = description.split("\n").filter { it.isNotBlank() }
        return lines.mapNotNull { line ->
            val trimmed = line.trimStart()
            when {
                // 联系人名优先匹配（最长前缀，避免"我方"被"我："截断）
                contactName != null && (trimmed.startsWith("$contactName：") || trimmed.startsWith("$contactName:")) -> {
                    val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                    if (sepIdx >= 0) {
                        val raw = trimmed.substring(sepIdx + 1).trim()
                        val (text, uri) = extractImageTag(raw)
                        DialogueLineInput(isMe = false, content = text, imageUri = uri)
                    } else null
                }
                trimmed.startsWith("我：") || trimmed.startsWith("我:") -> {
                    val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                    if (sepIdx >= 0) {
                        val raw = trimmed.substring(sepIdx + 1).trim()
                        val (text, uri) = extractImageTag(raw)
                        DialogueLineInput(isMe = true, content = text, imageUri = uri)
                    } else null
                }
                trimmed.startsWith("对方：") || trimmed.startsWith("对方:") -> {
                    val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                    if (sepIdx >= 0) {
                        val raw = trimmed.substring(sepIdx + 1).trim()
                        val (text, uri) = extractImageTag(raw)
                        DialogueLineInput(isMe = false, content = text, imageUri = uri)
                    } else null
                }
                else -> null
            }
        }
    }

    fun extractImageTag(raw: String): Pair<String, String?> {
        val regex = Regex("""\[img:(.+?)]""")
        val match = regex.find(raw)
        return if (match != null) {
            val uri = match.groupValues[1]
            val text = raw.replace(match.value, "").trim()
            text to uri
        } else {
            raw to null
        }
    }

    fun buildDescription(lines: List<DialogueLineInput>, contactName: String?): String {
        val dtos = lines.filter { it.content.isNotBlank() || it.imageUri != null }.map { line ->
            DialogueLineDto(
                speaker = if (line.isMe) "我" else (contactName ?: "对方"),
                content = line.content,
                imageUri = line.imageUri
            )
        }
        return json.encodeToString(dtos)
    }
}
