package com.tang.prm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tang.prm.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY nextBillingDate ASC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionByIdOnce(id: Long): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE (:keyword IS NULL OR name LIKE '%' || :keyword || '%' ESCAPE '\\') ORDER BY nextBillingDate ASC")
    fun searchSubscriptions(keyword: String?): Flow<List<SubscriptionEntity>>

    @Query("SELECT DISTINCT category FROM subscriptions WHERE category IS NOT NULL ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    // DAO-Q-1 修复：updateSubscription 改为 @Update，语义清晰（仅更新已存在行，不触发 DELETE+INSERT 级联副作用）。
    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: Long)

    @Query("SELECT COUNT(*) FROM subscriptions")
    fun getSubscriptionCount(): Flow<Int>
}
