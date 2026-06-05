package com.tang.prm.domain.usecase

import com.tang.prm.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * 更新联系人互动信息 UseCase
 *
 * 将亲密度更新的 coerceIn 逻辑和调用路径统一到 Domain 层，
 * 消除 AddEventVM / AddChatVM 中的重复代码。
 */
class UpdateInteractionUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contactId: Long, intimacyScore: Int, interactionTime: Long) {
        contactRepository.updateContactInteraction(
            id = contactId,
            score = intimacyScore.coerceIn(0, 100),
            interactionTime = interactionTime
        )
    }
}
