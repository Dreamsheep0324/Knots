package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.ContactRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class CleanCustomTypeUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend fun removeFromListFieldAll(field: String, value: String) {
        val contacts = contactRepository.getAllContacts().first()
        for (c in contacts) {
            val updatedHobby = c.hobby?.removeFromJsonArray(value)
            val updatedHabit = c.habit?.removeFromJsonArray(value)
            val updatedDiet = c.diet?.removeFromJsonArray(value)
            val updatedSkill = c.skill?.removeFromJsonArray(value)
            val changed = updatedHobby != c.hobby || updatedHabit != c.habit ||
                    updatedDiet != c.diet || updatedSkill != c.skill
            if (changed) {
                contactRepository.updateContact(c.copy(
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
            val arr = kotlinx.serialization.json.Json.parseToJsonElement(this).jsonArray
            val newArr = arr.filter { it.jsonPrimitive.content != value }
            if (newArr.size == arr.size) this
            else if (newArr.isEmpty()) null
            else JsonArray(newArr).toString()
        } catch (e: Exception) {
            val items = split(",").map { it.trim() }.filter { it.isNotEmpty() && it != value }
            if (items.isEmpty()) null
            else JsonArray(items.map { JsonPrimitive(it) }).toString()
        }
    }
}
