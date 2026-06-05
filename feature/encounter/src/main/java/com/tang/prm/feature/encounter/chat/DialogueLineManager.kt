package com.tang.prm.feature.encounter.chat

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

private val lineIdCounter = AtomicLong(0)

data class DialogueLineInput(
    val id: Long = lineIdCounter.incrementAndGet(),
    val isMe: Boolean = true,
    val content: String = "",
    val imageUri: String? = null
)

class DialogueLineManager @Inject constructor() {

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
        val lines = description.split("\n").filter { it.isNotBlank() }
        return lines.mapNotNull { line ->
            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("我：") || trimmed.startsWith("我:") -> {
                    val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                    if (sepIdx >= 0) {
                        val raw = trimmed.substring(sepIdx + 1).trim()
                        val (text, uri) = extractImageTag(raw)
                        DialogueLineInput(isMe = true, content = text, imageUri = uri)
                    } else null
                }
                contactName != null && (trimmed.startsWith("$contactName：") || trimmed.startsWith("$contactName:")) -> {
                    val sepIdx = trimmed.indexOfFirst { it == '：' || it == ':' }
                    if (sepIdx >= 0) {
                        val raw = trimmed.substring(sepIdx + 1).trim()
                        val (text, uri) = extractImageTag(raw)
                        DialogueLineInput(isMe = false, content = text, imageUri = uri)
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
        return lines.filter { it.content.isNotBlank() || it.imageUri != null }.joinToString("\n") { line ->
            val speaker = if (line.isMe) "我" else (contactName ?: "对方")
            val imageTag = if (line.imageUri != null) "[img:${line.imageUri}]" else ""
            val text = line.content + if (imageTag.isNotEmpty()) " $imageTag" else ""
            "$speaker：$text"
        }
    }
}
