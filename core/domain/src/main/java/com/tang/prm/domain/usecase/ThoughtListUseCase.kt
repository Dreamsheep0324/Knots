package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.ThoughtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class ThoughtListState(
    val allThoughts: List<Thought> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val contactThoughts: List<ContactThoughts> = emptyList(),
    val todoThoughts: List<Thought> = emptyList(),
    val filteredThoughts: List<Thought> = emptyList(),
    val gamification: GamificationState? = null
)

class ThoughtListUseCase @Inject constructor(
    private val thoughtRepository: ThoughtRepository,
    private val contactRepository: ContactRepository,
    private val gamificationUseCase: ThoughtGamificationUseCase
) {
    fun getThoughtListState(
        selectedFilter: String,
        selectedContactId: Long?,
        searchQuery: String
    ): Flow<ThoughtListState> {
        return combine(
            thoughtRepository.getAllThoughts(),
            thoughtRepository.getTodoThoughts(),
            contactRepository.getAllContacts()
        ) { thoughts, todos, contacts ->
            val contactMap = contacts.associateBy { it.id }
            val contactThoughts = thoughts
                .filter { it.contactId != null }
                .groupBy { it.contactId }
                .mapNotNull { (contactId, list) ->
                    val contact = contactMap[contactId] ?: return@mapNotNull null
                    ContactThoughts(
                        contact = contact,
                        thoughts = list,
                        latestThought = list.firstOrNull()
                    )
                }

            val filtered = thoughts.filterBy(
                filter = selectedFilter,
                selectedContactId = selectedContactId,
                searchQuery = searchQuery,
                contacts = contacts
            )

            val gamification = gamificationUseCase.getGamificationState(
                allThoughts = thoughts,
                todoThoughts = todos,
                contactThoughts = contactThoughts
            )

            ThoughtListState(
                allThoughts = thoughts,
                contacts = contacts,
                contactThoughts = contactThoughts,
                todoThoughts = todos,
                filteredThoughts = filtered,
                gamification = gamification
            )
        }
    }

    fun thoughtExp(thought: Thought): Int = gamificationUseCase.thoughtExp(thought)
}
