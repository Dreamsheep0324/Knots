package com.tang.prm.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SqlUtilsTest {

    @Test
    fun escapeSqlWildcards_percent_returnsEscaped() {
        val result = "test%name".escapeSqlWildcards()
        assertThat(result).isEqualTo("test\\%name")
    }

    @Test
    fun escapeSqlWildcards_underscore_returnsEscaped() {
        val result = "test_name".escapeSqlWildcards()
        assertThat(result).isEqualTo("test\\_name")
    }

    @Test
    fun escapeSqlWildcards_backslash_returnsEscaped() {
        val result = "test\\name".escapeSqlWildcards()
        assertThat(result).isEqualTo("test\\\\name")
    }

    @Test
    fun escapeSqlWildcards_noSpecialChars_returnsSame() {
        val result = "normal".escapeSqlWildcards()
        assertThat(result).isEqualTo("normal")
    }

    @Test
    fun escapeSqlWildcards_emptyString_returnsEmpty() {
        val result = "".escapeSqlWildcards()
        assertThat(result).isEmpty()
    }

    @Test
    fun escapeSqlWildcards_allSpecialChars_returnsAllEscaped() {
        val result = "\\%_".escapeSqlWildcards()
        assertThat(result).isEqualTo("\\\\\\%\\_")
    }
}
