package com.tang.prm.feature.people.contacts

import android.util.Log
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.usecase.DeleteCustomTypeUseCase
import javax.inject.Inject

class ContactFormHelper @Inject constructor(
    private val customTypeRepository: CustomTypeRepository,
    private val deleteCustomTypeUseCase: DeleteCustomTypeUseCase
) {
    companion object {
        private const val TAG = "ContactFormHelper"
    }

    suspend fun addCustomType(category: String, name: String, sortOrder: Int = 0, color: String? = null, icon: String? = null): CustomType? {
        return runCatching {
            val newType = CustomType(
                category = category,
                name = name,
                color = color,
                icon = icon,
                sortOrder = sortOrder
            )
            customTypeRepository.insertType(newType)
            newType
        }.onFailure { Log.e(TAG, "新增自定义类型失败: category=$category, name=$name", it) }
            .getOrNull()
    }

    suspend fun deleteCustomType(typeId: Long, category: String, typeName: String) {
        runCatching {
            val type = CustomType(
                id = typeId,
                category = category,
                name = typeName
            )
            deleteCustomTypeUseCase(type)
        }.onFailure { Log.e(TAG, "删除自定义类型失败: id=$typeId, name=$typeName", it) }
    }
}
