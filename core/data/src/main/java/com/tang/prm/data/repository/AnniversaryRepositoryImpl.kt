package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.AnniversaryDao
import com.tang.prm.data.local.entity.AnniversaryWithContact
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.effectiveDate
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.util.DateCalcUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnniversaryRepositoryImpl @Inject constructor(
    private val anniversaryDao: AnniversaryDao
) : AnniversaryRepository {

    private fun AnniversaryWithContact.toDomain() = anniversary.toDomain(
        contactName = contact?.name,
        contactAvatar = contact?.avatar
    )

    // B-5 修复：effectiveDate() 已提到 domain 层作为 Anniversary 的公共扩展函数，
    // 消除 Repository 内的 private 实现，单一真相源；UI 层（OrbitalCalendarState）也可复用。

    override fun getAllAnniversaries(): Flow<List<Anniversary>> =
        anniversaryDao.getAllAnniversariesWithContact().mapList { it.toDomain() }

    override fun getAnniversaryById(id: Long): Flow<Anniversary?> =
        anniversaryDao.getAnniversaryByIdWithContact(id).mapNullable { it.toDomain() }

    override fun getAnniversariesByContact(contactId: Long): Flow<List<Anniversary>> =
        anniversaryDao.getAnniversariesByContactWithContact(contactId).mapList { it.toDomain() }

    override fun getUpcomingAnniversaries(limit: Int): Flow<List<Anniversary>> {
        val today = DateCalcUtils.getTodayStart()
        return anniversaryDao.getUpcomingCandidatesWithContact(today).mapList { it.toDomain() }.map { list ->
            list
                .map { it to it.effectiveDate() }
                .filter { it.second >= today }
                .sortedBy { it.second }
                .take(limit)
                .map { it.first }
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun insertAnniversary(anniversary: Anniversary): Long =
        anniversaryDao.insertAnniversary(anniversary.toEntity())

    override suspend fun updateAnniversary(anniversary: Anniversary) =
        anniversaryDao.updateAnniversary(anniversary.toEntity())

    override suspend fun deleteAnniversary(id: Long) =
        anniversaryDao.deleteAnniversaryById(id)

    override fun getAnniversaryCount(): Flow<Int> = anniversaryDao.getAnniversaryCount()
}
