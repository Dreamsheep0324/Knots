package com.tang.prm.ui.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactDetailUiState(
    val contact: Contact? = null,
    val events: List<Event> = emptyList(),
    val anniversaries: List<Anniversary> = emptyList(),
    val conversations: List<Event> = emptyList(),
    val gifts: List<Gift> = emptyList(),
    val thoughts: List<Thought> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val hobbyOptions: List<CustomType> = emptyList(),
    val habitOptions: List<CustomType> = emptyList(),
    val dietOptions: List<CustomType> = emptyList(),
    val skillOptions: List<CustomType> = emptyList(),
    val eventTypes: List<CustomType> = emptyList()
)

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val eventRepository: EventRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val giftRepository: GiftRepository,
    private val thoughtRepository: ThoughtRepository,
    private val favoriteRepository: FavoriteRepository,
    private val customTypeRepository: CustomTypeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    init {
        loadContactDetail()
    }

    private fun loadContactDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val contactFlow = contactRepository.getContactById(contactId)
            val eventsFlow = eventRepository.getEventsByContact(contactId)
            val anniversariesFlow = anniversaryRepository.getAnniversariesByContact(contactId)
            val giftsFlow = giftRepository.getGiftsByContactId(contactId)
            val thoughtsFlow = thoughtRepository.getThoughtsByContact(contactId)
            val hobbyFlow = customTypeRepository.getTypesByCategory(CustomCategories.HOBBY)
            val habitFlow = customTypeRepository.getTypesByCategory(CustomCategories.HABIT)
            val dietFlow = customTypeRepository.getTypesByCategory(CustomCategories.DIET)
            val skillFlow = customTypeRepository.getTypesByCategory(CustomCategories.SKILL)
            val eventTypeFlow = customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE)
            val thoughtFavoritesFlow = favoriteRepository.getFavoritesByType(SourceTypes.THOUGHT)

            combine(
                combine(contactFlow, eventsFlow, anniversariesFlow, giftsFlow, thoughtsFlow) { contact, events, anniversaries, gifts, thoughts ->
                    ContactData(contact, events, anniversaries, gifts, thoughts)
                },
                combine(hobbyFlow, habitFlow, dietFlow, skillFlow, eventTypeFlow) { hobby, habit, diet, skill, eventTypes ->
                    CustomTypeData(hobby, habit, diet, skill, eventTypes)
                },
                thoughtFavoritesFlow
            ) { contactData, customTypeData, thoughtFavorites ->
                val conversations = contactData.events.filter { it.type == EventTypes.CONVERSATION }
                val nonConversationEvents = contactData.events.filter { it.type != EventTypes.CONVERSATION }
                val favIds = thoughtFavorites.map { it.sourceId }.toSet()

                _uiState.update { it.copy(
                    contact = contactData.contact,
                    events = nonConversationEvents,
                    anniversaries = contactData.anniversaries,
                    conversations = conversations,
                    gifts = contactData.gifts,
                    thoughts = contactData.thoughts,
                    favoriteIds = favIds,
                    hobbyOptions = customTypeData.hobby,
                    habitOptions = customTypeData.habit,
                    dietOptions = customTypeData.diet,
                    skillOptions = customTypeData.skill,
                    eventTypes = customTypeData.eventTypes,
                    isLoading = false
                )}
            }.collect {}
        }
    }

    private data class ContactData(
        val contact: Contact?,
        val events: List<Event>,
        val anniversaries: List<Anniversary>,
        val gifts: List<Gift>,
        val thoughts: List<Thought>
    )

    private data class CustomTypeData(
        val hobby: List<CustomType>,
        val habit: List<CustomType>,
        val diet: List<CustomType>,
        val skill: List<CustomType>,
        val eventTypes: List<CustomType>
    )

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteContact(onDeleted: () -> Unit) {
        viewModelScope.launch {
            contactRepository.deleteContact(contactId)
            onDeleted()
        }
    }

    fun toggleFavorite(thoughtId: Long, content: String) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(
                type = SourceTypes.THOUGHT,
                sourceId = thoughtId,
                title = content.take(50),
                description = content
            )
        }
    }

    fun toggleTodoDone(thought: Thought) {
        viewModelScope.launch {
            thoughtRepository.updateThought(thought.copy(
                isDone = !thought.isDone,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun deleteThought(id: Long) {
        viewModelScope.launch {
            thoughtRepository.deleteThought(id)
        }
    }
}
