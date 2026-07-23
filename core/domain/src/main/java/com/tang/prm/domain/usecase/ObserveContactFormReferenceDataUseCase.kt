package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.CustomTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 联系人表单所需的自定义类型参照数据（关系/学校/爱好/习惯/饮食/技能/人物关系类型）。
 *
 * 用于新增/编辑联系人界面，避免 ViewModel 直接依赖 Repository。
 */
data class ContactFormReferenceData(
    val relationships: List<CustomType> = emptyList(),
    val educations: List<CustomType> = emptyList(),
    val hobbies: List<CustomType> = emptyList(),
    val habits: List<CustomType> = emptyList(),
    val diets: List<CustomType> = emptyList(),
    val skills: List<CustomType> = emptyList(),
    val personRelationTypes: List<CustomType> = emptyList()
)

/**
 * 观察联系人表单所需的全部自定义类型选项。
 *
 * 7 种类型通过 combine 合并为单一 Flow，任一类型变更会自动刷新 UI。
 */
class ObserveContactFormReferenceDataUseCase @Inject constructor(
    private val customTypeRepository: CustomTypeRepository
) {
    operator fun invoke(): Flow<ContactFormReferenceData> {
        return combine(
            combine(
                customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP).distinctUntilChanged(),
                customTypeRepository.getTypesByCategory(CustomCategories.EDUCATION).distinctUntilChanged(),
                customTypeRepository.getTypesByCategory(CustomCategories.HOBBY).distinctUntilChanged()
            ) { relationships, educations, hobbies ->
                Triple(relationships, educations, hobbies)
            },
            combine(
                customTypeRepository.getTypesByCategory(CustomCategories.HABIT).distinctUntilChanged(),
                customTypeRepository.getTypesByCategory(CustomCategories.DIET).distinctUntilChanged(),
                customTypeRepository.getTypesByCategory(CustomCategories.SKILL).distinctUntilChanged()
            ) { habits, diets, skills ->
                Triple(habits, diets, skills)
            },
            customTypeRepository.getTypesByCategory(CustomCategories.PERSON_RELATION).distinctUntilChanged()
        ) { ref1, ref2, personRelationTypes ->
            ContactFormReferenceData(
                relationships = ref1.first,
                educations = ref1.second,
                hobbies = ref1.third,
                habits = ref2.first,
                diets = ref2.second,
                skills = ref2.third,
                personRelationTypes = personRelationTypes
            )
        }
    }
}
