package com.tang.prm.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class ThoughtGamificationUseCaseTest {

    private val useCase = ThoughtGamificationUseCase()
    private val zone = ZoneId.systemDefault()

    /** 构造指定天数前某小时的时间戳 */
    private fun daysAgoAtHour(daysAgo: Long, hour: Long): Long =
        LocalDate.now(zone).minusDays(daysAgo).atStartOfDay(zone).plusHours(hour).toInstant().toEpochMilli()

    @Nested
    @DisplayName("expForLevel")
    inner class ExpForLevelTest {

        @Test
        fun `level1 returns 0`() {
            assertThat(useCase.expForLevel(1)).isEqualTo(0)
        }

        @Test
        fun `level2 returns 15`() {
            assertThat(useCase.expForLevel(2)).isEqualTo(15)
        }

        @Test
        fun `level3 returns 45`() {
            assertThat(useCase.expForLevel(3)).isEqualTo(45)
        }

        @Test
        fun `level0 returns 0`() {
            assertThat(useCase.expForLevel(0)).isEqualTo(0)
        }

        @Test
        fun `negative level returns 0`() {
            assertThat(useCase.expForLevel(-1)).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("calcLevel")
    inner class CalcLevelTest {

        @Test
        fun `exp0 is level1`() {
            assertThat(useCase.calcLevel(0)).isEqualTo(1)
        }

        @Test
        fun `exp14 is level1`() {
            assertThat(useCase.calcLevel(14)).isEqualTo(1)
        }

        @Test
        fun `exp15 is level2`() {
            assertThat(useCase.calcLevel(15)).isEqualTo(2)
        }

        @Test
        fun `exp44 is level2`() {
            assertThat(useCase.calcLevel(44)).isEqualTo(2)
        }

        @Test
        fun `exp45 is level3`() {
            assertThat(useCase.calcLevel(45)).isEqualTo(3)
        }

        @Test
        fun `exp90 is level4`() {
            // expForLevel(4) = 15*4*3/2 = 90
            assertThat(useCase.expForLevel(4)).isEqualTo(90)
            assertThat(useCase.calcLevel(90)).isEqualTo(4)
        }
    }

    @Nested
    @DisplayName("thoughtExp")
    inner class ThoughtExpTest {

        @Test
        fun `base thought gives base XP`() {
            val thought = Thought(content = "test", type = ThoughtType.MURMUR)
            assertThat(useCase.thoughtExp(thought)).isEqualTo(ThoughtGamificationUseCase.XP_BASE)
        }

        @Test
        fun `done todo gives base plus done bonus`() {
            val thought = Thought(content = "test", isTodo = true, isDone = true)
            assertThat(useCase.thoughtExp(thought))
                .isEqualTo(ThoughtGamificationUseCase.XP_BASE + ThoughtGamificationUseCase.XP_DONE_BONUS)
        }

        @Test
        fun `with contact gives base plus contact bonus`() {
            val thought = Thought(content = "test", contactId = 1L)
            assertThat(useCase.thoughtExp(thought))
                .isEqualTo(ThoughtGamificationUseCase.XP_BASE + ThoughtGamificationUseCase.XP_CONTACT_BONUS)
        }

        @Test
        fun `done todo with contact gives all bonuses`() {
            val thought = Thought(content = "test", contactId = 1L, isTodo = true, isDone = true)
            assertThat(useCase.thoughtExp(thought))
                .isEqualTo(
                    ThoughtGamificationUseCase.XP_BASE +
                    ThoughtGamificationUseCase.XP_DONE_BONUS +
                    ThoughtGamificationUseCase.XP_CONTACT_BONUS
                )
        }
    }

    @Nested
    @DisplayName("calcStreak")
    inner class CalcStreakTest {

        @Test
        fun `empty list returns 0`() {
            assertThat(useCase.calcStreak(emptyList())).isEqualTo(0)
        }

        @Test
        fun `today thought returns streak 1`() {
            val thoughts = listOf(Thought(content = "t", createdAt = daysAgoAtHour(0, 14)))
            assertThat(useCase.calcStreak(thoughts)).isEqualTo(1)
        }

        @Test
        fun `today and yesterday returns streak 2`() {
            val thoughts = listOf(
                Thought(content = "t0", createdAt = daysAgoAtHour(0, 10)),
                Thought(content = "t1", createdAt = daysAgoAtHour(1, 20))
            )
            assertThat(useCase.calcStreak(thoughts)).isEqualTo(2)
        }

        @Test
        fun `three day streak returns 3`() {
            val thoughts = (0..2).map {
                Thought(content = "t$it", createdAt = daysAgoAtHour(it.toLong(), 12))
            }
            assertThat(useCase.calcStreak(thoughts)).isEqualTo(3)
        }

        @Test
        fun `gap breaks streak`() {
            // 今天和前天有，昨天没有 → streak = 1
            val thoughts = listOf(
                Thought(content = "today", createdAt = daysAgoAtHour(0, 10)),
                Thought(content = "2daysAgo", createdAt = daysAgoAtHour(2, 10))
            )
            assertThat(useCase.calcStreak(thoughts)).isEqualTo(1)
        }

        @Test
        fun `no thought today returns 0`() {
            // 今天没有 thought，昨天有 → streak = 0
            val thoughts = listOf(Thought(content = "yesterday", createdAt = daysAgoAtHour(1, 10)))
            assertThat(useCase.calcStreak(thoughts)).isEqualTo(0)
        }

        @Test
        fun `multiple thoughts same day returns 1`() {
            val thoughts = listOf(
                Thought(content = "morning", createdAt = daysAgoAtHour(0, 8)),
                Thought(content = "afternoon", createdAt = daysAgoAtHour(0, 15)),
                Thought(content = "evening", createdAt = daysAgoAtHour(0, 22))
            )
            assertThat(useCase.calcStreak(thoughts)).isEqualTo(1)
        }

        @Test
        fun `midnight and afternoon same day`() {
            // 同一天凌晨 00:30 和下午 14:00 → 都算同一天
            val thoughts = listOf(
                Thought(content = "midnight", createdAt = daysAgoAtHour(0, 0) + 30 * 60 * 1000),
                Thought(content = "afternoon", createdAt = daysAgoAtHour(0, 14))
            )
            assertThat(useCase.calcStreak(thoughts)).isEqualTo(1)
        }

        @Test
        fun `unordered thoughts still correct`() {
            // 乱序输入也能正确计算
            val thoughts = listOf(
                Thought(content = "today", createdAt = daysAgoAtHour(0, 10)),
                Thought(content = "2daysAgo", createdAt = daysAgoAtHour(2, 10)),
                Thought(content = "yesterday", createdAt = daysAgoAtHour(1, 10))
            )
            assertThat(useCase.calcStreak(thoughts)).isEqualTo(3)
        }
    }

    @Nested
    @DisplayName("getGamificationState")
    inner class GetGamificationStateTest {

        @Test
        fun `empty inputs returns zero state`() {
            val state = useCase.getGamificationState(
                allThoughts = emptyList(),
                todoThoughts = emptyList(),
                contactThoughts = emptyList()
            )
            assertThat(state.totalCount).isEqualTo(0)
            assertThat(state.currentExp).isEqualTo(0)
            assertThat(state.currentLevel).isEqualTo(1)
            assertThat(state.streak).isEqualTo(0)
            assertThat(state.todoDoneCount).isEqualTo(0)
            assertThat(state.todoTotalCount).isEqualTo(0)
        }

        @Test
        fun `counts thought types`() {
            val allThoughts = listOf(
                Thought(content = "f", type = ThoughtType.FRIEND),
                Thought(content = "p", type = ThoughtType.PLAN),
                Thought(content = "m", type = ThoughtType.MURMUR),
                Thought(content = "m2", type = ThoughtType.MURMUR)
            )
            val state = useCase.getGamificationState(allThoughts, emptyList(), emptyList())
            assertThat(state.friendCount).isEqualTo(1)
            assertThat(state.planCount).isEqualTo(1)
            assertThat(state.murmurCount).isEqualTo(2)
            assertThat(state.totalCount).isEqualTo(4)
        }

        @Test
        fun `exp includes done todo and contact bonuses`() {
            val allThoughts = listOf(
                Thought(content = "a"),
                Thought(content = "b", contactId = 1L)
            )
            val todoThoughts = listOf(
                Thought(content = "t1", isTodo = true, isDone = true),
                Thought(content = "t2", isTodo = true, isDone = false)
            )
            val contactThoughts = listOf(
                ContactThoughts(contact = mockContact(1L), thoughts = emptyList(), latestThought = null)
            )
            val state = useCase.getGamificationState(allThoughts, todoThoughts, contactThoughts)
            // 2 * XP_BASE + 1 * XP_DONE_BONUS + 1 * XP_CONTACT_BONUS + 1 * XP_STREAK_BONUS (两个 thought 都在今天)
            val expectedExp = 2 * 3 + 1 * 2 + 1 * 1 + 1 * 1
            assertThat(state.currentExp).isEqualTo(expectedExp)
            assertThat(state.todoDoneCount).isEqualTo(1)
            assertThat(state.todoTotalCount).isEqualTo(2)
        }

        @Test
        fun `levelProgress is zero at level start`() {
            val state = useCase.getGamificationState(
                allThoughts = emptyList(),
                todoThoughts = emptyList(),
                contactThoughts = emptyList()
            )
            assertThat(state.levelProgress).isEqualTo(0f)
        }
    }

    private fun mockContact(id: Long) = com.tang.prm.domain.model.Contact(id = id, name = "Test")
}
