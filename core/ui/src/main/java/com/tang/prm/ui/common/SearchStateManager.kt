package com.tang.prm.ui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

data class SearchState(
    val query: String = "",
    val isActive: Boolean = false
)

class SearchStateManager {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    /** 防抖后的查询 Flow，消费者应 collect 此 Flow 触发搜索，而非直接读 state.query */
    val debouncedQuery: kotlinx.coroutines.flow.Flow<String> = _state
        .map { it.query }
        .debounce(300)
        .distinctUntilChanged()

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
