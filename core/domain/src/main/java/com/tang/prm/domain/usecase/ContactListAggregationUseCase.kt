package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.CircleRepository
import com.tang.prm.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

data class CircleWithMembers(
    val circle: Circle,
    val members: List<Contact>
)

data class ContactListAggregate(
    val circles: List<CircleWithMembers>,
    val contacts: List<Contact>
)

enum class CircleSortMode {
    DEFAULT, MEMBER_COUNT_ASC, MEMBER_COUNT_DESC, INTIMACY_ASC, INTIMACY_DESC
}

/**
 * 联系人列表聚合用例（读操作）。
 *
 * 聚合 [ContactRepository] 与 [CircleRepository] 的数据，提供：
 * - 联系人 + 朋友圈聚合流
 * - 朋友圈可用联系人筛选
 * - 朋友圈排序
 */
class ContactListAggregationUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val circleRepository: CircleRepository
) {

    operator fun invoke(): Flow<ContactListAggregate> {
        return combine(
            contactRepository.getAllContacts().distinctUntilChanged(),
            circleRepository.getAllCircles().distinctUntilChanged()
        ) { contacts, circles ->
            // Q-4 修复：原 O(n*m) 过滤改为 O(n+m) 查找表
            val contactMap = contacts.associateBy { it.id }
            val circleWithMembers = circles.map { circle ->
                val members = circle.memberIds.mapNotNull { contactMap[it] }
                CircleWithMembers(circle = circle, members = members)
            }
            ContactListAggregate(circles = circleWithMembers, contacts = contacts)
        }
    }

    fun getAvailableContacts(allContacts: List<Contact>, circle: Circle): List<Contact> {
        return allContacts.filter { it.id !in circle.memberIds }
    }

    fun getSortedCircles(circles: List<CircleWithMembers>, sortMode: CircleSortMode): List<CircleWithMembers> {
        return when (sortMode) {
            CircleSortMode.DEFAULT -> circles
            CircleSortMode.MEMBER_COUNT_ASC -> circles.sortedBy { it.members.size }
            CircleSortMode.MEMBER_COUNT_DESC -> circles.sortedByDescending { it.members.size }
            CircleSortMode.INTIMACY_ASC -> circles.sortedBy { it.members.map { m -> m.intimacyScore }.averageOrZero() }
            CircleSortMode.INTIMACY_DESC -> circles.sortedByDescending { it.members.map { m -> m.intimacyScore }.averageOrZero() }
        }
    }

    private fun List<Int>.averageOrZero(): Double {
        return if (isEmpty()) 0.0 else average()
    }
}
