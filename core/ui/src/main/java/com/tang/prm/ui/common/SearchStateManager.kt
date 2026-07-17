package com.tang.prm.ui.common

import kotlinx.coroutines.FlowPreview
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

@OptIn(FlowPreview::class)
class SearchStateManager {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    /** 防抖后的查询 Flow，消费者应 collect 此 Flow 触发搜索，而非直接读 state.query。
     *  空查询立即发射（清空即时响应），非空查询防抖 300ms（减少中间态搜索）。 */
    val debouncedQuery: kotlinx.coroutines.flow.Flow<String> = _state
        .map { it.query }
        .debounce { query -> if (query.isEmpty()) 0L else DEBOUNCE_MS }
        .distinctUntilChanged()

    private companion object {
        const val DEBOUNCE_MS = 300L
    }

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
