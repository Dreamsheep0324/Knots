package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteCustomTypeUseCase @Inject constructor(
    private val customTypeRepository: CustomTypeRepository,
    private val contactRepository: ContactRepository,
    private val cleanCustomTypeUseCase: CleanCustomTypeUseCase
) {
    suspend operator fun invoke(type: CustomType) {
        customTypeRepository.deleteTypeById(type.id)
        when (type.category) {
            CustomCategories.RELATIONSHIP -> clearSingleStringField(Contact::relationship, type.name)
            CustomCategories.EDUCATION -> clearSingleStringField(Contact::education, type.name)
            CustomCategories.HOBBY, CustomCategories.HABIT, CustomCategories.DIET, CustomCategories.SKILL ->
                cleanCustomTypeUseCase.removeFromListFieldAll(type.category, type.name)
        }
    }

    /**
     * B-9 修复：统一单值字段（RELATIONSHIP / EDUCATION）的清理逻辑。
     *
     * 原实现：RELATIONSHIP 走 [ContactRepository.getFilteredContacts] DB 过滤，
     * EDUCATION 走 [ContactRepository.getAllContacts] + 内存过滤，两套不一致。
     *
     * 统一为内存过滤 + [ContactRepository.updateContacts] 批量事务提交，
     * 与 [CleanCustomTypeUseCase.removeFromListFieldAll] 的写法对齐。
     * 选择内存过滤而非 DB 过滤的原因：
     * 1. [ContactRepository.getFilteredContacts] 是为关键词搜索设计的（支持 LIKE 模糊匹配），
     *    用于精确等值清理语义不对口；
     * 2. EDUCATION 无对应 DB 过滤变体，强行加会进一步膨胀 [ContactRepository] 接口；
     * 3. 联系人表通常规模可控（百级），内存过滤的性能开销可忽略。
     */
    private suspend fun clearSingleStringField(
        fieldAccessor: (Contact) -> String?,
        value: String
    ) {
        val now = System.currentTimeMillis()
        val toUpdate = contactRepository.getAllContacts().first()
            .asSequence()
            .filter { fieldAccessor(it) == value }
            .map { contact ->
                when (fieldAccessor) {
                    Contact::relationship -> contact.copy(relationship = null, updatedAt = now)
                    Contact::education -> contact.copy(education = null, updatedAt = now)
                    else -> contact
                }
            }
            .toList()
        if (toUpdate.isNotEmpty()) {
            contactRepository.updateContacts(toUpdate)
        }
    }
}
