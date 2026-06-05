package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Circle
import kotlinx.coroutines.flow.Flow

interface CircleRepository {
    fun getAllCircles(): Flow<List<Circle>>
    fun getCircleById(id: Long): Flow<Circle?>
    fun getCirclesForContact(contactId: Long): Flow<List<Circle>>
    fun getChildCircles(parentId: Long): Flow<List<Circle>>
    fun getRootCircles(): Flow<List<Circle>>
    suspend fun insertCircle(circle: Circle): Long
    suspend fun updateCircle(circle: Circle)
    suspend fun deleteCircle(id: Long)
    suspend fun deleteCircleWithChildren(id: Long)
    suspend fun updateCircleOrder(circles: List<Circle>)

    fun getCircleCount(): Flow<Int>
}
