package com.tang.prm.feature.people.contacts

import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.usecase.DeleteCustomTypeUseCase
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import javax.inject.Inject

class ContactFormHelper @Inject constructor(
    private val customTypeRepository: CustomTypeRepository,
    private val deleteCustomTypeUseCase: DeleteCustomTypeUseCase,
    private val anniversaryRepository: AnniversaryRepository
) {
    suspend fun addCustomType(category: String, name: String, sortOrder: Int = 0, color: String? = null, icon: String? = null): CustomType? {
        val newType = CustomType(
            category = category,
            name = name,
            color = color,
            icon = icon,
            sortOrder = sortOrder
        )
        customTypeRepository.insertType(newType)
        return newType
    }

    suspend fun deleteCustomType(typeId: Long, category: String, typeName: String) {
        val type = CustomType(
            id = typeId,
            category = category,
            name = typeName
        )
        deleteCustomTypeUseCase(type)
    }

    fun parseListField(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(value)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    fun serializeListField(list: List<String>): String? {
        if (list.isEmpty()) return null
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    suspend fun syncBirthdayAnniversary(contactId: Long, birthdayLong: Long, isLunar: Boolean, contactName: String, contactAvatar: String?) {
        anniversaryRepository.getAnniversariesByContact(contactId).first().filter { it.type == AnniversaryType.BIRTHDAY }.forEach { anniversary ->
            anniversaryRepository.updateAnniversary(
                anniversary.copy(
                    date = birthdayLong,
                    isLunar = isLunar,
                    contactName = contactName,
                    contactAvatar = contactAvatar
                )
            )
        }
    }
}
