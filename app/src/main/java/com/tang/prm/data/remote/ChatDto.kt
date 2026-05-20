package com.tang.prm.data.remote

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatStreamResponse(
    val choices: List<StreamChoice>?
)

data class StreamChoice(
    val delta: DeltaContent?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class DeltaContent(
    val content: String?
)
