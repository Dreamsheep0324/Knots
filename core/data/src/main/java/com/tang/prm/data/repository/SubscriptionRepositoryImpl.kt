package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.SubscriptionDao
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.repository.SubscriptionRepository
import com.tang.prm.util.escapeSqlWildcards
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) : SubscriptionRepository {
    override fun getAllSubscriptions(): Flow<List<Subscription>> =
        subscriptionDao.getAllSubscriptions().mapList { it.toDomain() }

    override fun searchSubscriptions(keyword: String?): Flow<List<Subscription>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return subscriptionDao.searchSubscriptions(escapedKeyword).mapList { it.toDomain() }
    }

    override fun getSubscriptionById(id: Long): Flow<Subscription?> =
        subscriptionDao.getSubscriptionById(id).mapNullable { it.toDomain() }

    override suspend fun getSubscriptionByIdOnce(id: Long): Subscription? =
        subscriptionDao.getSubscriptionByIdOnce(id)?.toDomain()

    override fun getAllCategories(): Flow<List<String>> =
        subscriptionDao.getAllCategories()

    override suspend fun insertSubscription(subscription: Subscription): Long =
        subscriptionDao.insertSubscription(subscription.toEntity())

    override suspend fun updateSubscription(subscription: Subscription) =
        subscriptionDao.updateSubscription(subscription.toEntity())

    override suspend fun deleteSubscription(id: Long) =
        subscriptionDao.deleteSubscriptionById(id)

    override fun getSubscriptionCount(): Flow<Int> =
        subscriptionDao.getSubscriptionCount()
}
