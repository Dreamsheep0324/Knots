package com.tang.prm.feature.chat

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DialogueLineManagerTest {

    private lateinit var manager: DialogueLineManager

    @BeforeEach
    fun setUp() {
        manager = DialogueLineManager()
    }

    // --- 1. buildDescription produces valid JSON ---

    @Test
    fun buildDescription_producesValidJson() {
        val lines = listOf(
            DialogueLineInput(isMe = true, content = "你好"),
            DialogueLineInput(isMe = false, content = "嗨", imageUri = "/img/a.jpg")
        )
        val json = manager.buildDescription(lines, contactName = "小明")

        val dtos = Json.decodeFromString<List<DialogueLineDto>>(json)
        assertThat(dtos).hasSize(2)
        assertThat(dtos[0].speaker).isEqualTo("我")
        assertThat(dtos[0].content).isEqualTo("你好")
        assertThat(dtos[0].imageUri).isNull()
        assertThat(dtos[1].speaker).isEqualTo("小明")
        assertThat(dtos[1].content).isEqualTo("嗨")
        assertThat(dtos[1].imageUri).isEqualTo("/img/a.jpg")
    }

    // --- 2. parseDescriptionToLines reads JSON format ---

    @Test
    fun parseDescriptionToLines_readsJsonFormat() {
        val json = """[{"speaker":"我","content":"你好"},{"speaker":"对方","content":"嗨","imageUri":"/img/b.png"}]"""
        val lines = manager.parseDescriptionToLines(json, contactName = null)

        assertThat(lines).hasSize(2)
        assertThat(lines[0].isMe).isTrue()
        assertThat(lines[0].content).isEqualTo("你好")
        assertThat(lines[1].isMe).isFalse()
        assertThat(lines[1].content).isEqualTo("嗨")
        assertThat(lines[1].imageUri).isEqualTo("/img/b.png")
    }

    // --- 3. parseDescriptionToLines falls back to legacy format ---

    @Test
    fun parseDescriptionToLines_fallsBackToLegacyFormat() {
        val legacy = "我：你好\n对方：嗨"
        val lines = manager.parseDescriptionToLines(legacy, contactName = null)

        assertThat(lines).hasSize(2)
        assertThat(lines[0].isMe).isTrue()
        assertThat(lines[0].content).isEqualTo("你好")
        assertThat(lines[1].isMe).isFalse()
        assertThat(lines[1].content).isEqualTo("嗨")
    }

    // --- 4. Contact name takes priority over 我 prefix ---

    @Test
    fun contactName_takesPriorityOverMePrefix() {
        val legacy = "我方：这条应该属于联系人\n我：这条属于我"
        val lines = manager.parseDescriptionToLines(legacy, contactName = "我方")

        assertThat(lines).hasSize(2)
        // "我方" 匹配联系人名，isMe = false
        assertThat(lines[0].isMe).isFalse()
        assertThat(lines[0].content).isEqualTo("这条应该属于联系人")
        // "我：" 匹配自己，isMe = true
        assertThat(lines[1].isMe).isTrue()
        assertThat(lines[1].content).isEqualTo("这条属于我")
    }

    // --- 5. IDs are unique ---

    @Test
    fun ids_areUnique() {
        val lines = List(100) { DialogueLineInput() }
        val ids = lines.map { it.id }.toSet()
        assertThat(ids).hasSize(100)
    }

    // --- 6. Roundtrip preserves data ---

    @Test
    fun roundtrip_preservesData() {
        val original = listOf(
            DialogueLineInput(isMe = true, content = "hello", imageUri = null),
            DialogueLineInput(isMe = false, content = "world", imageUri = "/img/x.png")
        )
        val json = manager.buildDescription(original, contactName = "小红")
        val restored = manager.parseDescriptionToLines(json, contactName = "小红")

        assertThat(restored).hasSize(2)
        assertThat(restored[0].isMe).isTrue()
        assertThat(restored[0].content).isEqualTo("hello")
        assertThat(restored[0].imageUri).isNull()
        assertThat(restored[1].isMe).isFalse()
        assertThat(restored[1].content).isEqualTo("world")
        assertThat(restored[1].imageUri).isEqualTo("/img/x.png")
    }

    // --- 7. extractImageTag works ---

    @Test
    fun extractImageTag_works() {
        val (text, uri) = manager.extractImageTag("[img:/path/img.jpg] some text")
        assertThat(text).isEqualTo("some text")
        assertThat(uri).isEqualTo("/path/img.jpg")
    }

    @Test
    fun extractImageTag_noTag_returnsOriginalText() {
        val (text, uri) = manager.extractImageTag("plain text")
        assertThat(text).isEqualTo("plain text")
        assertThat(uri).isNull()
    }

    // --- 8. Empty/null description returns empty list ---

    @Test
    fun emptyDescription_returnsEmptyList() {
        assertThat(manager.parseDescriptionToLines("", contactName = null)).isEmpty()
    }

    @Test
    fun nullDescription_returnsEmptyList() {
        assertThat(manager.parseDescriptionToLines(null, contactName = null)).isEmpty()
    }

    @Test
    fun blankDescription_returnsEmptyList() {
        assertThat(manager.parseDescriptionToLines("   ", contactName = null)).isEmpty()
    }

    // --- 9. Basic operations: addLine, updateLine, removeLine, toggleSpeaker, moveLine ---

    @Test
    fun addLine_appendsNewLine() {
        val lines = listOf(DialogueLineInput(isMe = true, content = "hi"))
        val result = manager.addLine(lines, speaker = "对方")
        assertThat(result).hasSize(2)
        assertThat(result[1].isMe).isFalse()
        assertThat(result[1].content).isEmpty()
    }

    @Test
    fun updateLine_changesContent() {
        val line = DialogueLineInput(id = 1L, isMe = true, content = "old")
        val result = manager.updateLine(listOf(line), lineId = 1L, text = "new")
        assertThat(result[0].content).isEqualTo("new")
    }

    @Test
    fun removeLine_removesMatchingId() {
        val line1 = DialogueLineInput(id = 1L, isMe = true, content = "a")
        val line2 = DialogueLineInput(id = 2L, isMe = false, content = "b")
        val result = manager.removeLine(listOf(line1, line2), lineId = 1L)
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(2L)
    }

    @Test
    fun toggleSpeaker_flipsIsMe() {
        val line = DialogueLineInput(id = 1L, isMe = true, content = "hi")
        val result = manager.toggleSpeaker(listOf(line), lineId = 1L)
        assertThat(result[0].isMe).isFalse()
    }

    @Test
    fun moveLine_reordersList() {
        val line1 = DialogueLineInput(id = 1L, isMe = true, content = "a")
        val line2 = DialogueLineInput(id = 2L, isMe = false, content = "b")
        val line3 = DialogueLineInput(id = 3L, isMe = true, content = "c")
        val result = manager.moveLine(listOf(line1, line2, line3), fromIndex = 0, toIndex = 2)
        assertThat(result).hasSize(3)
        assertThat(result[0].content).isEqualTo("b")
        assertThat(result[1].content).isEqualTo("c")
        assertThat(result[2].content).isEqualTo("a")
    }

    @Test
    fun moveLine_outOfBounds_returnsOriginal() {
        val line1 = DialogueLineInput(id = 1L, isMe = true, content = "a")
        val lines = listOf(line1)
        val result = manager.moveLine(lines, fromIndex = -1, toIndex = 0)
        assertThat(result).isEqualTo(lines)
    }
}
