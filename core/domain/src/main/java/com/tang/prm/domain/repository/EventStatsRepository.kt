package com.tang.prm.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 事件统计 Repository（R-2 ISP 拆分）。
 *
 * 从 [EventRepository] 抽出纯统计方法，[HomeStatsUseCase] 改依赖此接口，
 * 避免统计职责与事件 CRUD 职责耦合在单一胖接口中。
 *
 * [EventRepository] 继承此接口以保持向后兼容（Hilt 绑定不变）。
 */
interface EventStatsRepository {
    fun getEventCount(): Flow<Int>
    fun getEventCountByType(type: String): Flow<Int>
    fun getEventCountWithLocation(): Flow<Int>

    /**
     * 事件照片总数。
     *
     * R-2 修复：保留此方法在 Repository 层（O(1) SQL count），
     * 不下沉到 UseCase 层聚合（会导致 O(n) 内存扫描性能退化）。
     */
    fun getPhotoCount(): Flow<Int>
}
