package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.util.escapeSqlWildcards
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val anniversaryDao: AnniversaryDao,
    private val giftDao: GiftDao,
    private val thoughtDao: ThoughtDao,
    private val circleDao: CircleDao,
    private val todoDao: TodoDao,
    private val reminderDao: ReminderDao,
    private val database: TangDatabase
) : ContactRepository {
    override fun getAllContacts(): Flow<List<Contact>> =
        contactDao.getAllContacts().map { entities -> entities.map { it.toDomain() } }

    override fun getContactById(id: Long): Flow<Contact?> =
        contactDao.getContactById(id).map { it?.toDomain() }

    override fun searchContacts(keyword: String): Flow<List<Contact>> =
        contactDao.searchContacts(keyword.escapeSqlWildcards()).map { entities -> entities.map { it.toDomain() } }

    override fun getContactsByGroup(groupId: Long): Flow<List<Contact>> =
        contactDao.getContactsByGroup(groupId).map { entities -> entities.map { it.toDomain() } }

    override fun getFilteredContacts(keyword: String?, groupId: Long?, relationship: String?): Flow<List<Contact>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return contactDao.getFilteredContacts(escapedKeyword, groupId, relationship).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTopContactsByIntimacy(limit: Int): Flow<List<Contact>> =
        contactDao.getTopContactsByIntimacy(limit).map { entities -> entities.map { it.toDomain() } }

    override fun getRecentContacts(limit: Int): Flow<List<Contact>> =
        contactDao.getRecentContacts(limit).map { entities -> entities.map { it.toDomain() } }

    override fun getContactCount(): Flow<Int> = contactDao.getContactCount()

    override suspend fun insertContact(contact: Contact): Long =
        contactDao.insertContact(contact.toEntity())

    override suspend fun updateContact(contact: Contact) =
        contactDao.updateContact(contact.toEntity())

    override suspend fun deleteContact(id: Long) = database.withTransaction {
        anniversaryDao.deleteAnniversariesByContact(id)
        giftDao.deleteGiftsByContactId(id)
        thoughtDao.deleteThoughtsByContact(id)
        todoDao.deleteTodosByContact(id)
        reminderDao.deleteRemindersByContact(id)
        contactDao.deleteCrossRefsByContact(id)
        circleDao.deleteMemberRefsByContact(id)
        contactDao.deleteContactById(id)
    }

    override suspend fun updateContactInteraction(id: Long, score: Int, interactionTime: Long) =
        contactDao.updateContactInteraction(id, score, interactionTime)

    override suspend fun removeRelationshipFromAll(relationshipName: String) {
        contactDao.clearRelationship(relationshipName)
    }

    override suspend fun removeEducationFromAll(educationName: String) {
        contactDao.clearEducation(educationName)
    }

    override suspend fun removeFromListFieldAll(field: String, value: String) {
        val contacts = contactDao.getContactsWithListFieldValue(value)
        for (c in contacts) {
            val updatedHobby = c.hobby?.removeFromJsonArray(value)
            val updatedHabit = c.habit?.removeFromJsonArray(value)
            val updatedDiet = c.diet?.removeFromJsonArray(value)
            val updatedSkill = c.skill?.removeFromJsonArray(value)
            val changed = updatedHobby != c.hobby || updatedHabit != c.habit || updatedDiet != c.diet || updatedSkill != c.skill
            if (changed) {
                val full = contactDao.getContactByIdOnce(c.id) ?: continue
                contactDao.updateContact(full.copy(
                    hobby = updatedHobby,
                    habit = updatedHabit,
                    diet = updatedDiet,
                    skill = updatedSkill,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }

    private fun String.removeFromJsonArray(value: String): String? {
        return try {
            val arr = org.json.JSONArray(this)
            val newArr = org.json.JSONArray()
            var changed = false
            for (i in 0 until arr.length()) {
                val item = arr.getString(i)
                if (item == value) { changed = true } else { newArr.put(item) }
            }
            if (!changed) this
            else if (newArr.length() == 0) null
            else newArr.toString()
        } catch (e: Exception) {
            val items = split(",").map { it.trim() }.filter { it.isNotEmpty() && it != value }
            if (items.isEmpty()) null else org.json.JSONArray(items).toString()
        }
    }
}
