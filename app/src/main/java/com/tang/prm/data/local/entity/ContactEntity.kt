package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatar: String? = null,
    val nickname: String? = null,
    val gender: Int = 0, // 0=未知, 1=男, 2=女
    val birthday: Long? = null,
    val isLunarBirthday: Boolean = false,
    val knowingDate: Long? = null,
    val phone: String? = null,
    val email: String? = null,
    val city: String? = null,
    val address: String? = null,
    val education: String? = null,
    val company: String? = null,
    val jobTitle: String? = null,
    val industry: String? = null,
    val hobby: String? = null,
    val habit: String? = null,
    val diet: String? = null,
    val skill: String? = null,
    val mbti: String? = null,
    val spouseName: String? = null,
    val childrenCount: Int = 0,
    val childrenNames: String? = null,
    val introducer: String? = null,
    val relationshipLevel: Int = 0,
    val relationship: String? = null,
    val groupId: Long? = null,
    val intimacyScore: Int = 50,
    val lastInteractionTime: Long? = null,
    val customFields: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contact_groups")
data class ContactGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contact_tags")
data class ContactTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "contact_tag_cross_ref",
    primaryKeys = ["contactId", "tagId"]
)
data class ContactTagCrossRef(
    val contactId: Long,
    val tagId: Long
)