package com.tang.prm.ui.common

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchStateManagerTest {

    private lateinit var manager: SearchStateManager

    @BeforeEach
    fun setUp() {
        manager = SearchStateManager()
    }

    @Nested
    @DisplayName("初始状态")
    inner class InitialStateTest {

        @Test
        fun initialState_isInactiveWithEmptyQuery() = runTest {
            manager.state.test {
                val state = awaitItem()
                assertThat(state.isActive).isFalse()
                assertThat(state.query).isEmpty()
            }
        }
    }

    @Nested
    @DisplayName("toggleSearch")
    inner class ToggleSearchTest {

        @Test
        fun toggleSearch_activatesSearch() = runTest {
            manager.toggleSearch()
            assertThat(manager.state.value.isActive).isTrue()
        }

        @Test
        fun toggleSearchTwice_deactivatesAndClearsQuery() = runTest {
            manager.onQueryChange("test")
            manager.toggleSearch()  // activate
            manager.toggleSearch()  // deactivate
            assertThat(manager.state.value.isActive).isFalse()
            assertThat(manager.state.value.query).isEmpty()
        }

        @Test
        fun toggleSearchThreeTimes_reactivates() = runTest {
            manager.toggleSearch()  // activate
            manager.toggleSearch()  // deactivate
            manager.toggleSearch()  // activate
            assertThat(manager.state.value.isActive).isTrue()
        }
    }

    @Nested
    @DisplayName("onQueryChange")
    inner class OnQueryChangeTest {

        @Test
        fun updatesQuery() {
            manager.onQueryChange("hello")
            assertThat(manager.state.value.query).isEqualTo("hello")
        }

        @Test
        fun updatesQueryMultipleTimes() {
            manager.onQueryChange("a")
            manager.onQueryChange("ab")
            manager.onQueryChange("abc")
            assertThat(manager.state.value.query).isEqualTo("abc")
        }
    }

    @Nested
    @DisplayName("clearQuery")
    inner class ClearQueryTest {

        @Test
        fun clearsQuery() {
            manager.onQueryChange("test")
            manager.clearQuery()
            assertThat(manager.state.value.query).isEmpty()
        }

        @Test
        fun keepsSearchActive() {
            manager.toggleSearch()
            manager.onQueryChange("test")
            manager.clearQuery()
            assertThat(manager.state.value.isActive).isTrue()
            assertThat(manager.state.value.query).isEmpty()
        }
    }

    @Nested
    @DisplayName("deactivate")
    inner class DeactivateTest {

        @Test
        fun deactivatesAndClearsQuery() {
            manager.toggleSearch()
            manager.onQueryChange("test")
            manager.deactivate()
            assertThat(manager.state.value.isActive).isFalse()
            assertThat(manager.state.value.query).isEmpty()
        }
    }

    @Nested
    @DisplayName("debouncedQuery")
    inner class DebouncedQueryTest {

        @Test
        fun emitsQueryAfterDebounce() = runTest {
            manager.debouncedQuery.test {
                manager.onQueryChange("hello")
                // Wait for debounce (300ms)
                val query = awaitItem()
                assertThat(query).isEqualTo("hello")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
