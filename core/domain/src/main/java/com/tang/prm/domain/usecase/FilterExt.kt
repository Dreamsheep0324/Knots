package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.FootprintItem
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import java.util.Calendar

// region Intimacy utilities

/** Get intimacy level display name from score */
fun getIntimacyLevel(score: Int): String = when {
    score <= 20 -> AppStrings.Intimacy.NEW
    score <= 40 -> AppStrings.Intimacy.ACQUAINTANCE
    score <= 60 -> AppStrings.Intimacy.FRIEND
    score <= 80 -> AppStrings.Intimacy.CLOSE
    else -> AppStrings.Intimacy.FAMILY
}

/** All intimacy level names in ascending order */
val IntimacyLevels: List<String> = listOf(
    AppStrings.Intimacy.NEW,
    AppStrings.Intimacy.ACQUAINTANCE,
    AppStrings.Intimacy.FRIEND,
    AppStrings.Intimacy.CLOSE,
    AppStrings.Intimacy.FAMILY
)

// endregion

// region Contact filtering

/**
 * Filter contacts by keyword, group, relationship, and/or intimacy level.
 * Result is sorted by intimacyScore descending.
 */
fun List<Contact>.filterBy(
    keyword: String? = null,
    groupId: Long? = null,
    relationship: String? = null,
    intimacy: String? = null
): List<Contact> {
    var filtered = this
    if (!keyword.isNullOrBlank()) {
        filtered = filtered.filter {
            it.name.contains(keyword, ignoreCase = true) ||
                it.phone?.contains(keyword, ignoreCase = true) == true ||
                it.nickname?.contains(keyword, ignoreCase = true) == true
        }
    }
    if (groupId != null) {
        filtered = filtered.filter { it.groupId == groupId }
    }
    if (relationship != null) {
        filtered = filtered.filter { it.relationship == relationship }
    }
    if (intimacy != null) {
        filtered = filtered.filter { getIntimacyLevel(it.intimacyScore) == intimacy }
    }
    return filtered.sortedByDescending { it.intimacyScore }
}

// endregion

// region Event filtering

/**
 * Filter events by contact participant, event type, and/or search query.
 * Result is sorted by time descending.
 */
fun List<Event>.filterBy(
    contact: Contact? = null,
    eventType: String? = null,
    searchQuery: String = ""
): List<Event> {
    var filtered = this
    if (contact != null) {
        filtered = filtered.filter { event ->
            event.participants.any { it.id == contact.id }
        }
    }
    if (eventType != null && eventType != "all") {
        filtered = filtered.filter { it.type.name == eventType || it.customTypeName == eventType }
    }
    if (searchQuery.isNotBlank()) {
        filtered = filtered.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.description?.contains(searchQuery, ignoreCase = true) == true ||
                it.location?.contains(searchQuery, ignoreCase = true) == true
        }
    }
    return filtered.sortedByDescending { it.time }
}

// endregion

// region Thought filtering

/**
 * Filter thoughts by type filter, contact, and/or search query.
 * The [contacts] list is used for contact name matching in search.
 */
fun List<Thought>.filterBy(
    filter: String = "all",
    selectedContactId: Long? = null,
    searchQuery: String = "",
    contacts: List<Contact> = emptyList()
): List<Thought> {
    val contactMap = contacts.associateBy { it.id }
    val byType = when (filter) {
        "friend" -> filter { it.type == ThoughtType.FRIEND }
        "plan" -> filter { it.type == ThoughtType.PLAN }
        "murmur" -> filter { it.type == ThoughtType.MURMUR }
        "todo" -> filter { it.isTodo }
        else -> this
    }
    val byContact = if (selectedContactId != null) {
        byType.filter { it.contactId == selectedContactId }
    } else {
        byType
    }
    return if (searchQuery.isBlank()) byContact else byContact.filter {
        it.content.contains(searchQuery, ignoreCase = true) ||
            contactMap[it.contactId]?.name?.contains(searchQuery, ignoreCase = true) == true
    }
}

// endregion

// region Footprint filtering

/**
 * Filter footprints by contact, event type, and/or year.
 */
fun List<FootprintItem>.filterBy(
    selectedContactId: Long? = null,
    filterEventType: String? = null,
    selectedYear: Int? = null
): List<FootprintItem> {
    var filtered = this
    if (selectedContactId != null) {
        filtered = filtered.filter { it.contactId == selectedContactId }
    }
    if (filterEventType != null) {
        filtered = filtered.filter { it.eventType == filterEventType }
    }
    if (selectedYear != null) {
        filtered = filtered.filter {
            val calendar = Calendar.getInstance().apply { timeInMillis = it.date }
            calendar.get(Calendar.YEAR) == selectedYear
        }
    }
    return filtered
}

// endregion

// region Photo filtering

/**
 * Filter album photos by contact and/or source type.
 */
fun List<AlbumPhoto>.filterBy(
    selectedContactId: Long? = null,
    filterSourceType: String? = null
): List<AlbumPhoto> {
    var filtered = this
    if (selectedContactId != null) {
        filtered = filtered.filter { it.contactId == selectedContactId }
    }
    if (filterSourceType != null) {
        filtered = filtered.filter { it.sourceType == filterSourceType }
    }
    return filtered
}

// endregion
