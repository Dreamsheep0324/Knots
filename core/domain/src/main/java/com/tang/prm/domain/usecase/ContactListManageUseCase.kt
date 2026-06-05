package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.CircleRepository
import com.tang.prm.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class CircleWithMembers(
    val circle: Circle,
    val members: List<Contact>
)

data class ContactListAggregate(
    val circles: List<CircleWithMembers>,
    val contacts: List<Contact>
)

enum class CircleSortMode {
    DEFAULT, MEMBER_COUNT_ASC, MEMBER_COUNT_DESC, INTIMACY_ASC, INTIMACY_DESC
}

class ContactListManageUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val circleRepository: CircleRepository
) {

    fun getContactListAggregate(): Flow<ContactListAggregate> {
        return combine(
            contactRepository.getAllContacts(),
            circleRepository.getAllCircles()
        ) { contacts, circles ->
            val circleWithMembers = circles.map { circle ->
                val members = contacts.filter { it.id in circle.memberIds }
                CircleWithMembers(circle = circle, members = members)
            }
            ContactListAggregate(circles = circleWithMembers, contacts = contacts)
        }
    }

    suspend fun createCircle(name: String, description: String?, color: String, waveform: String): Long {
        val newCircle = Circle(name = name, description = description, color = color, waveform = waveform)
        return circleRepository.insertCircle(newCircle)
    }

    suspend fun updateCircle(circle: Circle, name: String, description: String?, color: String, waveform: String) {
        val updated = circle.copy(
            name = name,
            description = description,
            color = color,
            waveform = waveform,
            updatedAt = System.currentTimeMillis()
        )
        circleRepository.updateCircle(updated)
    }

    suspend fun deleteCircle(circleId: Long) {
        circleRepository.deleteCircleWithChildren(circleId)
    }

    suspend fun addMemberToCircle(circle: Circle, contactId: Long) {
        if (!circle.memberIds.contains(contactId)) {
            val updated = circle.copy(
                memberIds = circle.memberIds + contactId,
                updatedAt = System.currentTimeMillis()
            )
            circleRepository.updateCircle(updated)
        }
    }

    suspend fun removeMemberFromCircle(circle: Circle, contactId: Long) {
        val updated = circle.copy(
            memberIds = circle.memberIds - contactId,
            updatedAt = System.currentTimeMillis()
        )
        circleRepository.updateCircle(updated)
    }

    fun getAvailableContacts(allContacts: List<Contact>, circle: Circle): List<Contact> {
        return allContacts.filter { it.id !in circle.memberIds }
    }

    fun getFilteredContacts(contacts: List<Contact>, query: String): List<Contact> {
        return if (query.isBlank()) contacts
        else contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phone?.contains(query, ignoreCase = true) == true
        }
    }

    fun getSortedCircles(circles: List<CircleWithMembers>, sortMode: CircleSortMode): List<CircleWithMembers> {
        return when (sortMode) {
            CircleSortMode.DEFAULT -> circles
            CircleSortMode.MEMBER_COUNT_ASC -> circles.sortedBy { it.members.size }
            CircleSortMode.MEMBER_COUNT_DESC -> circles.sortedByDescending { it.members.size }
            CircleSortMode.INTIMACY_ASC -> circles.sortedBy { it.members.map { m -> m.intimacyScore }.averageOrZero() }
            CircleSortMode.INTIMACY_DESC -> circles.sortedByDescending { it.members.map { m -> m.intimacyScore }.averageOrZero() }
        }
    }

    private fun List<Int>.averageOrZero(): Double {
        return if (isEmpty()) 0.0 else average()
    }

    companion object {
        val PresetColors = listOf(
            "#2196F3" to "科技蓝",
            "#00BCD4" to "青蓝",
            "#3F51B5" to "靛蓝",
            "#673AB7" to "深紫",
            "#9C27B0" to "紫色",
            "#E91E63" to "粉红",
            "#F44336" to "红色",
            "#FF5722" to "橙红",
            "#FF9800" to "橙色",
            "#FFC107" to "琥珀",
            "#8BC34A" to "浅绿",
            "#4CAF50" to "绿色"
        )

        val WaveformTypes = listOf(
            "sine" to "正弦波",
            "cosine" to "余弦波",
            "square" to "方波",
            "sawtooth" to "锯齿波",
            "triangle" to "三角波",
            "pulse" to "脉冲波",
            "noise" to "噪声波",
            "heartbeat" to "心跳波",
            "exponential" to "指数波",
            "damped" to "阻尼波",
            "step" to "阶梯波",
            "compound" to "复合波"
        )
    }
}
