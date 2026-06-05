package com.tang.prm.data.remote

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ChatDtoTest {

    @Test
    fun chatRequest_defaultValues_streamTrue_temp07() {
        val request = ChatRequest(model = "test", messages = emptyList())
        assertThat(request.stream).isTrue()
        assertThat(request.temperature).isEqualTo(0.7)
        assertThat(request.maxTokens).isEqualTo(2048)
    }

    @Test
    fun chatMessage_constructor_setsFields() {
        val message = ChatMessage(role = "user", content = "hello")
        assertThat(message.role).isEqualTo("user")
        assertThat(message.content).isEqualTo("hello")
    }

    @Test
    fun chatStreamResponse_nullChoices_noCrash() {
        val response = ChatStreamResponse(null)
        assertThat(response.choices).isNull()
    }

    @Test
    fun deltaContent_nullContent_noCrash() {
        val delta = DeltaContent(null)
        assertThat(delta.content).isNull()
    }

    @Test
    fun chatRequest_customModel_setsModel() {
        val request = ChatRequest(model = "deepseek-v4", messages = emptyList())
        assertThat(request.model).isEqualTo("deepseek-v4")
    }
}
