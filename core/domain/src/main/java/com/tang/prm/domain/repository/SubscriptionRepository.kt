package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAllSubscriptions(): Flow<List<Subscription>>
    fun getActiveSubscriptions(): Flow<List<Subscription>>
    fun searchSubscriptions(keyword: String?): Flow<List<Subscription>>
    fun getSubscriptionById(id: Long): Flow<Subscription?>
    suspend fun getSubscriptionByIdOnce(id: Long): Subscription?
    fun getAllCategories(): Flow<List<String>>
    suspend fun insertSubscription(subscription: Subscription): Long
    suspend fun updateSubscription(subscription: Subscription)
    suspend fun deleteSubscription(id: Long)
    fun getSubscriptionCount(): Flow<Int>
}
