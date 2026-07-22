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
}
