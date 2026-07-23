package com.tang.prm.domain.repository

import com.tang.prm.domain.model.ContactRelation
import kotlinx.coroutines.flow.Flow

interface ContactRelationRepository {
    fun observeAllRelations(): Flow<List<ContactRelation>>
    fun observeRelationsForContact(contactId: Long): Flow<List<ContactRelation>>
    fun getRelationCount(): Flow<Int>
    suspend fun findRelation(contactIdA: Long, contactIdB: Long): ContactRelation?
    suspend fun upsertRelation(
        contactIdA: Long,
        contactIdB: Long,
        relationTypeId: Long,
        note: String?
    ): Long
    suspend fun deleteRelation(id: Long)
}
