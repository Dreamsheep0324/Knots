package com.tang.prm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions", indices = [Index("nextBillingDate"), Index("status")])
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String? = null,
    val category: String? = null,
    val price: Double,
    val currency: String = "CNY",
    val cycle: String,
    val startDate: Long,
    val nextBillingDate: Long,
    val status: String = "ACTIVE",
    val reminderDays: Int = 3,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "timezone", defaultValue = "UTC")
    val timezone: String = "UTC"
)
