package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.CircleDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.repository.CircleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CircleRepositoryImpl @Inject constructor(
    private val circleDao: CircleDao
) : CircleRepository {

    override fun getAllCircles(): Flow<List<Circle>> {
        return circleDao.getAllCirclesWithMembers().map { circlesWithMembers ->
            circlesWithMembers.map { it.toDomain() }
        }
    }

    override fun getCircleById(id: Long): Flow<Circle?> {
        return combine(circleDao.getCircleById(id), circleDao.getMemberIdsForCircle(id)) { entity, memberIds ->
            entity?.toDomain(memberIds)
        }
    }

    override fun getCirclesForContact(contactId: Long): Flow<List<Circle>> {
        return circleDao.getAllCirclesWithMembers().map { circlesWithMembers ->
            circlesWithMembers.filter { it.members.any { it.contactId == contactId } }
                .map { it.toDomain() }
        }
    }

    override fun getChildCircles(parentId: Long): Flow<List<Circle>> {
        return circleDao.getAllCirclesWithMembers().map { circlesWithMembers ->
            circlesWithMembers.filter { it.circle.parentCircleId == parentId }
                .map { it.toDomain() }
        }
    }

    override fun getRootCircles(): Flow<List<Circle>> {
        return circleDao.getAllCirclesWithMembers().map { circlesWithMembers ->
            circlesWithMembers.filter { it.circle.parentCircleId == null }
                .map { it.toDomain() }
        }
    }

    override suspend fun insertCircle(circle: Circle): Long {
        return circleDao.insertCircleWithMembers(circle.toEntity(), circle.memberIds)
    }

    override suspend fun updateCircle(circle: Circle) {
        circleDao.updateCircleWithMembers(circle.toEntity(), circle.memberIds)
    }

    override suspend fun deleteCircle(id: Long) {
        circleDao.deleteCircleById(id)
    }

    override suspend fun deleteCircleWithChildren(id: Long) {
        circleDao.deleteCircleWithChildren(id)
    }

    override suspend fun updateCircleOrder(circles: List<Circle>) {
        circles.forEachIndexed { index, circle ->
            circleDao.updateCircle(circle.copy(sortOrder = index).toEntity())
        }
    }

    override fun getCircleCount(): Flow<Int> = circleDao.getCircleCount()
}
