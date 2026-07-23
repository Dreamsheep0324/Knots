package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.repository.CircleRepository
import javax.inject.Inject

/**
 * 朋友圈管理用例（写操作）。
 *
 * 封装朋友圈的创建/更新/删除及成员增删，通过 [CircleRepository] 持久化。
 */
class CircleManageUseCase @Inject constructor(
    private val circleRepository: CircleRepository
) {

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
}
