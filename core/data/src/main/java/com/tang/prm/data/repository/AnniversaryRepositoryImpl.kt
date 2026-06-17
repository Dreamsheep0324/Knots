package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.AnniversaryDao
import com.tang.prm.data.local.entity.AnniversaryWithContact
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.util.DateCalcUtils
import com.tang.prm.domain.util.LunarUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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

    /**
     * 计算纪念日的下次生效日期。
     *
     * 旧实现完全忽略 [Anniversary.isLunar] 字段，将农历日期当作公历处理，
     * 导致所有农历生日/纪念日的"下次提醒日期"计算错误。
     *
     * 修复后：当 [Anniversary.isLunar] 为 true 时，先通过 [LunarUtils.lunarToSolar]
     * 将农历月日转换为当年/次年的公历日期，再交给 [DateCalcUtils] 计算下次提醒。
     */
    private fun Anniversary.effectiveDate(): Long = when {
        type == AnniversaryType.BIRTHDAY -> {
            if (isLunar) {
                val solarDate = lunarToNextSolarDate(date, isLeapMonth)
                if (solarDate != null) DateCalcUtils.getNextBirthdayDate(solarDate)
                else DateCalcUtils.getNextBirthdayDate(date)  // 转换失败 fallback
            } else {
                DateCalcUtils.getNextBirthdayDate(date)
            }
        }
        isRepeat -> {
            if (isLunar) {
                val solarDate = lunarToNextSolarDate(date, isLeapMonth)
                if (solarDate != null) DateCalcUtils.getNextRepeatDate(solarDate)
                else DateCalcUtils.getNextRepeatDate(date)
            } else {
                DateCalcUtils.getNextRepeatDate(date)
            }
        }
        else -> date
    }

    /**
     * 将存储的农历日期（epoch millis）转换为当年或次年的公历日期（epoch millis）。
     *
     * 存储的日期中提取农历月、日，分别尝试当年和次年转换，取第一个未过去的公历日期。
     *
     * @param lunarMillis 农历日期的 epoch 毫秒
     * @param isLeapMonth 是否为闰月
     * @return 公历日期的 epoch 毫秒，转换失败返回 null
     */
    private fun lunarToNextSolarDate(lunarMillis: Long, isLeapMonth: Boolean): Long? {
        val zone = ZoneId.systemDefault()
        val lunarInstant = Instant.ofEpochMilli(lunarMillis).atZone(zone).toLocalDate()
        val lunarYear = lunarInstant.year
        val lunarMonth = lunarInstant.monthValue
        val lunarDay = lunarInstant.dayOfMonth
        val today = LocalDate.now()

        // 尝试当年农历转公历
        val solarThisYear = LunarUtils.lunarToSolar(lunarYear, lunarMonth, lunarDay, isLeapMonth)
        if (solarThisYear != null) {
            val solarDate = LocalDate.of(solarThisYear.first, solarThisYear.second, solarThisYear.third)
            if (!solarDate.isBefore(today)) {
                return solarDate.atStartOfDay(zone).toInstant().toEpochMilli()
            }
        }

        // 当年已过或无效，尝试明年
        val solarNextYear = LunarUtils.lunarToSolar(lunarYear + 1, lunarMonth, lunarDay, isLeapMonth)
        return solarNextYear?.let {
            val solarDate = LocalDate.of(it.first, it.second, it.third)
            solarDate.atStartOfDay(zone).toInstant().toEpochMilli()
        }
    }

    override fun getAllAnniversaries(): Flow<List<Anniversary>> =
        anniversaryDao.getAllAnniversariesWithContact().mapList { it.toDomain() }

    override fun getAnniversaryById(id: Long): Flow<Anniversary?> =
        anniversaryDao.getAnniversaryByIdWithContact(id).mapNullable { it.toDomain() }

    override fun getAnniversariesByContact(contactId: Long): Flow<List<Anniversary>> =
        anniversaryDao.getAnniversariesByContactWithContact(contactId).mapList { it.toDomain() }

    override fun getAnniversariesInRange(startDate: Long, endDate: Long): Flow<List<Anniversary>> =
        anniversaryDao.getAnniversariesInRangeWithContact(startDate, endDate).mapList { it.toDomain() }

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

    override fun getPastAnniversaries(limit: Int): Flow<List<Anniversary>> {
        val today = DateCalcUtils.getTodayStart()
        return anniversaryDao.getPastCandidatesWithContact(today).mapList { it.toDomain() }.map { list ->
            list
                .map { it to it.effectiveDate() }
                .filter { it.second < today }
                .sortedByDescending { it.second }
                .take(limit)
                .map { it.first }
        }.flowOn(Dispatchers.Default)
    }

    override fun getAnniversariesByType(type: String): Flow<List<Anniversary>> =
        anniversaryDao.getAnniversariesByTypeWithContact(type).mapList { it.toDomain() }

    override suspend fun insertAnniversary(anniversary: Anniversary): Long =
        anniversaryDao.insertAnniversary(anniversary.toEntity())

    override suspend fun updateAnniversary(anniversary: Anniversary) =
        anniversaryDao.updateAnniversary(anniversary.toEntity())

    override suspend fun deleteAnniversary(id: Long) =
        anniversaryDao.deleteAnniversaryById(id)

    override fun getAnniversaryCount(): Flow<Int> = anniversaryDao.getAnniversaryCount()
}
