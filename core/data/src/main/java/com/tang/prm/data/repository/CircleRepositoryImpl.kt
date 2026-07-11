package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.CircleDao
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.repository.CircleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CircleRepositoryImpl @Inject constructor(
    private val circleDao: CircleDao
) : CircleRepository {

    override fun getAllCircles(): Flow<List<Circle>> {
        return circleDao.getAllCirclesWithMembers().mapList { it.toDomain() }
    }

    override fun getCircleById(id: Long): Flow<Circle?> {
        return combine(circleDao.getCircleById(id), circleDao.getMemberIdsForCircle(id)) { entity, memberIds ->
            entity?.toDomain(memberIds)
        }
    }

    override fun getCirclesForContact(contactId: Long): Flow<List<Circle>> {
        return circleDao.getCirclesForContactWithMembers(contactId).mapList { it.toDomain() }
    }

    override fun getChildCircles(parentId: Long): Flow<List<Circle>> {
        return circleDao.getChildCirclesWithMembers(parentId).mapList { it.toDomain() }
    }

    override fun getRootCircles(): Flow<List<Circle>> {
        return circleDao.getRootCirclesWithMembers().mapList { it.toDomain() }
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
        circleDao.updateCircleOrder(circles.mapIndexed { index, circle ->
            circle.copy(sortOrder = index).toEntity()
        })
    }

    override fun getCircleCount(): Flow<Int> = circleDao.getCircleCount()
}
