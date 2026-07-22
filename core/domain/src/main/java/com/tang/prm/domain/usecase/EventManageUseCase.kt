package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Event
import com.tang.prm.domain.repository.EventRepository
import javax.inject.Inject

/**
 * B-8 修复：写操作通过 [EventRepository.insertEventWithParticipants] /
 * [EventRepository.updateEventWithParticipants] 在单一 Room 事务中完成，
 * 任一失败回滚，避免事件半残状态。
 */
class EventManageUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend fun insertEventWithParticipants(event: Event, participantIds: List<Long>): Long =
        eventRepository.insertEventWithParticipants(event, participantIds)

    suspend fun updateEventWithParticipants(event: Event, participantIds: List<Long>) =
        eventRepository.updateEventWithParticipants(event, participantIds)
}
