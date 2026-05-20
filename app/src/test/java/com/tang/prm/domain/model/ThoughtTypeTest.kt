package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ThoughtTypeTest {

    @Test
    fun thoughtType_values_has3Entries() {
        assertThat(ThoughtType.entries).hasSize(3)
    }

    @Test
    fun thoughtType_friend_keyIsFriend() {
        assertThat(ThoughtType.FRIEND.key).isEqualTo("friend")
    }

    @Test
    fun thoughtType_plan_keyIsPlan() {
        assertThat(ThoughtType.PLAN.key).isEqualTo("plan")
    }

    @Test
    fun thoughtType_murmur_keyIsMurmur() {
        assertThat(ThoughtType.MURMUR.key).isEqualTo("murmur")
    }

    @Test
    fun thoughtType_fromKey_friend_returnsFRIEND() {
        assertThat(ThoughtType.fromKey("friend")).isEqualTo(ThoughtType.FRIEND)
    }

    @Test
    fun thoughtType_fromKey_unknown_returnsMURMUR() {
        assertThat(ThoughtType.fromKey("unknown")).isEqualTo(ThoughtType.MURMUR)
    }

    @Test
    fun thoughtType_fromKey_empty_returnsMURMUR() {
        assertThat(ThoughtType.fromKey("")).isEqualTo(ThoughtType.MURMUR)
    }
}
