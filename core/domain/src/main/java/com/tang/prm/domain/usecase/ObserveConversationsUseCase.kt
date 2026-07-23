package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 对话列表项领域模型。
 *
 * 从原 ChatViewModel 中的 ConversationUiModel 下沉到 Domain 层，
 * 使其成为领域聚合模型而非 UI 模型。
 */
data class ConversationItem(
    val eventId: Long,
    val contactId: Long?,
    val contactName: String,
    val avatar: String?,
    val title: String?,
    val lastMessage: String,
    val lastMessageTime: String
)

/**
 * 观察对话列表 UseCase（C-1 修复）。
 *
 * 将 ChatViewModel 中的 EventRepository 数据查询与映射逻辑提取到此 UseCase，
 * 使 ChatViewModel 不再直接依赖 Repository 的观察方法。
 *
 * 遵循 Q-3 约定：上游 Flow 加 [distinctUntilChanged] 去重。
 */
class ObserveConversationsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    /**
     * 观察对话类型事件并映射为 [ConversationItem] 列表。
     *
     * - 按 createdAt 降序排序
     * - 使用 [DateUtils.formatShortDate] 格式化时间
     *
     * @return 对话列表 Flow
     */
    operator fun invoke(): Flow<List<ConversationItem>> {
        return eventRepository.getEventsByType(EventType.CONVERSATION.name)
            .distinctUntilChanged()
            .map { events ->
                events.sortedByDescending { it.createdAt }.map { event ->
                    // M-2 修复：使用 Event.representativeParticipant 富模型属性，与
                    // FootprintAggregationUseCase/PhotoAlbumAggregationUseCase 保持一致。
                    val primaryContact = event.representativeParticipant
                    ConversationItem(
                        eventId = event.id,
                        contactId = primaryContact?.id,
                        contactName = primaryContact?.name ?: event.title,
                        avatar = primaryContact?.avatar,
                        title = event.title,
                        lastMessage = event.conversationSummary ?: event.title,
                        lastMessageTime = DateUtils.formatShortDate(event.createdAt)
                    )
                }
            }
    }
}
