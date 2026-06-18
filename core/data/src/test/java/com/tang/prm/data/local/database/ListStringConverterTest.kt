package com.tang.prm.data.local.database

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListStringConverterTest {

    private val converter = ListStringConverter()

    @Nested
    @DisplayName("fromList: List<String> -> JSON String")
    inner class FromListTest {

        @Test
        fun emptyList_returnsEmptyJsonArray() {
            assertThat(converter.fromList(emptyList())).isEqualTo("[]")
        }

        @Test
        fun singleItem_returnsJsonArray() {
            val result = converter.fromList(listOf("hello"))
            assertThat(result).isEqualTo("""["hello"]""")
        }

        @Test
        fun multipleItems_returnsJsonArray() {
            val result = converter.fromList(listOf("a", "b", "c"))
            assertThat(result).isEqualTo("""["a","b","c"]""")
        }

        @Test
        fun itemWithComma_preservedInJson() {
            val result = converter.fromList(listOf("a,b", "c"))
            assertThat(result).contains("a,b")
        }

        @Test
        fun emptyStringItem_preservedInJson() {
            val result = converter.fromList(listOf("", "b"))
            assertThat(result).isEqualTo("""["","b"]""")
        }
    }

    @Nested
    @DisplayName("fromString: String -> List<String>")
    inner class FromStringTest {

        @Test
        fun null_returnsEmptyList() {
            assertThat(converter.fromString(null)).isEmpty()
        }

        @Test
        fun blankString_returnsEmptyList() {
            assertThat(converter.fromString("")).isEmpty()
            assertThat(converter.fromString("   ")).isEmpty()
        }

        @Test
        fun jsonArray_returnsList() {
            val result = converter.fromString("""["a","b","c"]""")
            assertThat(result).containsExactly("a", "b", "c").inOrder()
        }

        @Test
        fun singleItemJsonArray_returnsSingleItemList() {
            val result = converter.fromString("""["hello"]""")
            assertThat(result).containsExactly("hello")
        }

        @Test
        fun emptyJsonArray_returnsEmptyList() {
            assertThat(converter.fromString("[]")).isEmpty()
        }

        @Test
        fun invalidJson_fallsBackToCommaSplit() {
            val result = converter.fromString("a, b, c")
            assertThat(result).containsExactly("a", "b", "c").inOrder()
        }

        @Test
        fun invalidJson_trimsAndFiltersEmpty() {
            val result = converter.fromString("a, , b,")
            assertThat(result).containsExactly("a", "b").inOrder()
        }

        @Test
        fun plainString_fallsBackToSingleItem() {
            val result = converter.fromString("hello")
            assertThat(result).containsExactly("hello")
        }
    }

    @Nested
    @DisplayName("往返一致性")
    inner class RoundTripTest {

        @Test
        fun roundTrip_normalList() {
            val original = listOf("alpha", "beta", "gamma")
            val serialized = converter.fromList(original)
            val deserialized = converter.fromString(serialized)
            assertThat(deserialized).containsExactlyElementsIn(original).inOrder()
        }

        @Test
        fun roundTrip_emptyList() {
            val serialized = converter.fromList(emptyList())
            val deserialized = converter.fromString(serialized)
            assertThat(deserialized).isEmpty()
        }

        @Test
        fun roundTrip_listWithCommaInItem() {
            val original = listOf("a,b", "c")
            val serialized = converter.fromList(original)
            val deserialized = converter.fromString(serialized)
            assertThat(deserialized).containsExactlyElementsIn(original).inOrder()
        }
    }
}
