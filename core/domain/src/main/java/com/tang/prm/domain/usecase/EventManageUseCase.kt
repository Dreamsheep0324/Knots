package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Event
import com.tang.prm.domain.repository.EventRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EventManageUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend fun insertEventWithParticipants(event: Event, participantIds: List<Long>): Long {
        val eventId = eventRepository.insertEvent(event)
        participantIds.forEach { contactId ->
            eventRepository.insertEventParticipant(eventId, contactId)
        }
        return eventId
    }

    suspend fun updateEventWithParticipants(event: Event, participantIds: List<Long>) {
        eventRepository.updateEvent(event)
        val currentParticipants = eventRepository.getParticipantIdsForEvent(event.id).first()
        val toAdd = participantIds - currentParticipants.toSet()
        val toRemove = currentParticipants - participantIds.toSet()
        toAdd.forEach { eventRepository.insertEventParticipant(event.id, it) }
        toRemove.forEach { eventRepository.deleteEventParticipant(event.id, it) }
    }
}
