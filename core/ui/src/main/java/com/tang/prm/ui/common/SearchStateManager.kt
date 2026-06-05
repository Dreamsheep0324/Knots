package com.tang.prm.ui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SearchState(
    val query: String = "",
    val isActive: Boolean = false
)

class SearchStateManager {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun toggleSearch() {
        _state.update { it.copy(isActive = !it.isActive, query = if (it.isActive) "" else it.query) }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun clearQuery() {
        _state.update { it.copy(query = "") }
    }

    fun deactivate() {
        _state.update { it.copy(isActive = false, query = "") }
    }
}
