package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * 事件表单参考数据。
 *
 * 用于新增/编辑事件界面，统一封装 4 种参考数据的 combine 逻辑，避免 ViewModel
 * 直接依赖 Repository，并消除与 [ObserveEventsAggregateUseCase] 在 contacts + eventTypes
 * 上的重复加载（A-3）。
 *
 * @param contacts 全部联系人（参与者候选）
 * @param eventTypes 事件类型自定义选项
 * @param emotions 情绪自定义选项
 * @param weathers 天气自定义选项
 */
data class EventReferenceData(
    val contacts: List<Contact> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val emotions: List<CustomType> = emptyList(),
    val weathers: List<CustomType> = emptyList()
)

/**
 * 观察事件表单所需的全部参考数据。
 *
 * 4 路 combine：contacts + eventTypes + emotions + weathers，
 * 任一上游变更都会重新发射聚合结果。
 *
 * 参照 feature/people 的 [ObserveContactFormReferenceDataUseCase] 模式。
 */
class ObserveEventReferenceDataUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val customTypeRepository: CustomTypeRepository
) {
    fun invoke(): Flow<EventReferenceData> = combine(
        contactRepository.getAllContacts(),
        customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE),
        customTypeRepository.getTypesByCategory(CustomCategories.EMOTION),
        customTypeRepository.getTypesByCategory(CustomCategories.WEATHER)
    ) { contacts, eventTypes, emotions, weathers ->
        EventReferenceData(contacts, eventTypes, emotions, weathers)
    }
}
