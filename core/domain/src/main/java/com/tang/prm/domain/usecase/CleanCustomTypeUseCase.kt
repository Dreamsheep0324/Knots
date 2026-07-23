package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
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
    /**
     * 从所有联系人的指定列表字段中移除某个自定义类型值。
     *
     * [field] 必须是 [CustomCategories.HOBBY]/[CustomCategories.HABIT]/
     * [CustomCategories.DIET]/[CustomCategories.SKILL] 之一，仅清理该字段，不污染其他字段。
     *
     * P-5 修复：先收集所有需要变更的联系人，最后调用 [ContactRepository.updateContacts]
     * 批量事务化提交，替代 N 次独立 updateContact。
     */
    suspend fun removeFromListFieldAll(field: String, value: String) {
        val targetAccessor: (Contact) -> String? = when (field) {
            CustomCategories.HOBBY -> Contact::hobby
            CustomCategories.HABIT -> Contact::habit
            CustomCategories.DIET -> Contact::diet
            CustomCategories.SKILL -> Contact::skill
            else -> return
        }
        val now = System.currentTimeMillis()
        val toUpdate = mutableListOf<Contact>()
        val contacts = contactRepository.getAllContacts().first()
        for (c in contacts) {
            val original = targetAccessor(c)
            if (original == null) continue
            val updated = original.removeFromJsonArray(value)
            if (updated != original) {
                val updatedContact = when (field) {
                    CustomCategories.HOBBY -> c.copy(hobby = updated, updatedAt = now)
                    CustomCategories.HABIT -> c.copy(habit = updated, updatedAt = now)
                    CustomCategories.DIET -> c.copy(diet = updated, updatedAt = now)
                    CustomCategories.SKILL -> c.copy(skill = updated, updatedAt = now)
                    else -> c
                }
                toUpdate.add(updatedContact)
            }
        }
        if (toUpdate.isNotEmpty()) {
            contactRepository.updateContacts(toUpdate)
        }
    }

    private fun String.removeFromJsonArray(value: String): String? {
        return try {
            val arr = kotlinx.serialization.json.Json.parseToJsonElement(this).jsonArray
            val newArr = arr.filter { it.jsonPrimitive.content != value }
            if (newArr.size == arr.size) this
            else if (newArr.isEmpty()) null
            else JsonArray(newArr).toString()
        } catch (e: kotlinx.serialization.SerializationException) {
            // L-3 修复：原捕获过宽 Exception，现仅捕获 SerializationException，
            // 降级到逗号分隔解析路径（兼容历史脏数据）。
            val items = split(",").map { it.trim() }.filter { it.isNotEmpty() && it != value }
            if (items.isEmpty()) null
            else JsonArray(items.map { JsonPrimitive(it) }).toString()
        } catch (e: IllegalArgumentException) {
            // parseToJsonElement 对非法 JSON 抛 IllegalArgumentException，同样降级。
            val items = split(",").map { it.trim() }.filter { it.isNotEmpty() && it != value }
            if (items.isEmpty()) null
            else JsonArray(items.map { JsonPrimitive(it) }).toString()
        }
    }
}
