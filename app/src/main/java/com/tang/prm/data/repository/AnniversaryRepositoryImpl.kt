package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.AnniversaryDao
import com.tang.prm.data.local.entity.AnniversaryWithContact
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    private fun List<AnniversaryWithContact>.toDomainList() = map { it.toDomain() }

    override fun getAllAnniversaries(): Flow<List<Anniversary>> =
        anniversaryDao.getAllAnniversariesWithContact().map { it.toDomainList() }

    override fun getAnniversaryById(id: Long): Flow<Anniversary?> =
        anniversaryDao.getAnniversaryByIdWithContact(id).map { it?.toDomain() }

    override fun getAnniversariesByContact(contactId: Long): Flow<List<Anniversary>> =
        anniversaryDao.getAnniversariesByContactWithContact(contactId).map { it.toDomainList() }

    override fun getAnniversariesInRange(startDate: Long, endDate: Long): Flow<List<Anniversary>> =
        anniversaryDao.getAnniversariesInRangeWithContact(startDate, endDate).map { it.toDomainList() }

    override fun getUpcomingAnniversaries(limit: Int): Flow<List<Anniversary>> =
        anniversaryDao.getAllAnniversariesWithContact().map { list ->
            val all = list.toDomainList()
            val today = System.currentTimeMillis()
            all.map { anniversary ->
                val effectiveDate = when {
                    anniversary.type == AnniversaryType.BIRTHDAY -> DateUtils.getNextBirthdayDate(anniversary.date)
                    anniversary.isRepeat -> DateUtils.getNextRepeatDate(anniversary.date)
                    else -> anniversary.date
                }
                anniversary to effectiveDate
            }
                .filter { it.second >= today }
                .sortedBy { it.second }
                .take(limit)
                .map { it.first }
        }

    override fun getPastAnniversaries(limit: Int): Flow<List<Anniversary>> =
        anniversaryDao.getAllAnniversariesWithContact().map { list ->
            val all = list.toDomainList()
            val today = System.currentTimeMillis()
            all.map { anniversary ->
                val effectiveDate = when {
                    anniversary.type == AnniversaryType.BIRTHDAY -> DateUtils.getNextBirthdayDate(anniversary.date)
                    anniversary.isRepeat -> DateUtils.getNextRepeatDate(anniversary.date)
                    else -> anniversary.date
                }
                anniversary to effectiveDate
            }
                .filter { it.second < today }
                .sortedByDescending { it.second }
                .take(limit)
                .map { it.first }
        }

    override fun getAnniversariesByType(type: String): Flow<List<Anniversary>> =
        anniversaryDao.getAnniversariesByTypeWithContact(type).map { it.toDomainList() }

    override suspend fun insertAnniversary(anniversary: Anniversary): Long =
        anniversaryDao.insertAnniversary(anniversary.toEntity())

    override suspend fun updateAnniversary(anniversary: Anniversary) =
        anniversaryDao.updateAnniversary(anniversary.toEntity())

    override suspend fun deleteAnniversary(id: Long) =
        anniversaryDao.deleteAnniversaryById(id)

    override fun getAnniversaryCount(): Flow<Int> = anniversaryDao.getAnniversaryCount()
}
