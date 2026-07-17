package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 联系人表单所需的自定义类型参照数据（关系/学校/爱好/习惯/饮食/技能）。
 *
 * 用于新增/编辑联系人界面，避免 ViewModel 直接依赖 Repository。
 */
data class ContactFormReferenceData(
    val relationships: List<CustomType> = emptyList(),
    val educations: List<CustomType> = emptyList(),
    val hobbies: List<CustomType> = emptyList(),
    val habits: List<CustomType> = emptyList(),
    val diets: List<CustomType> = emptyList(),
    val skills: List<CustomType> = emptyList()
)

/**
 * 观察联系人表单所需的全部自定义类型选项。
 *
 * 6 种类型通过 combine 合并为单一 Flow，任一类型变更会自动刷新 UI。
 */
class ObserveContactFormReferenceDataUseCase @Inject constructor(
    private val customTypeRepository: CustomTypeRepository
) {
    fun invoke(): Flow<ContactFormReferenceData> {
        return combine(
            combine(
                customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP),
                customTypeRepository.getTypesByCategory(CustomCategories.EDUCATION),
                customTypeRepository.getTypesByCategory(CustomCategories.HOBBY)
            ) { relationships, educations, hobbies ->
                Triple(relationships, educations, hobbies)
            },
            combine(
                customTypeRepository.getTypesByCategory(CustomCategories.HABIT),
                customTypeRepository.getTypesByCategory(CustomCategories.DIET),
                customTypeRepository.getTypesByCategory(CustomCategories.SKILL)
            ) { habits, diets, skills ->
                Triple(habits, diets, skills)
            }
        ) { ref1, ref2 ->
            ContactFormReferenceData(
                relationships = ref1.first,
                educations = ref1.second,
                hobbies = ref1.third,
                habits = ref2.first,
                diets = ref2.second,
                skills = ref2.third
            )
        }
    }
}

/**
 * 获取指定联系人用于编辑（一次性读取）。
 *
 * 返回 null 表示联系人不存在或已删除。
 */
class GetContactForEditUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contactId: Long): Contact? {
        return contactRepository.getContactById(contactId).first()
    }
}
