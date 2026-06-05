package com.tang.prm.data.remote

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
    val temperature: Double = 0.7,
    @SerialName("max_tokens")
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatStreamResponse(
    val choices: List<StreamChoice>? = null
)

@Serializable
data class StreamChoice(
    val delta: DeltaContent? = null,
    @SerialName("finish_reason")
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class DeltaContent(
    val content: String? = null
)

@Serializable
data class ChatResponse(
    val choices: List<ChatChoice>? = null
)

@Serializable
data class ChatChoice(
    val message: ChatMessage? = null,
    @SerialName("finish_reason")
    @SerializedName("finish_reason")
    val finishReason: String? = null
)
