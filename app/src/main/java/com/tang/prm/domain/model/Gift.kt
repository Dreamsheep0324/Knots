package com.tang.prm.domain.model

data class Gift(
    val id: Long = 0,
    val contactId: Long,
    val giftName: String,
    val giftType: String = "",
    val date: Long,
    val isSent: Boolean,
    val amount: Double? = null,
    val occasion: String? = null,
    val description: String? = null,
    val location: String? = null,
    val photos: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
