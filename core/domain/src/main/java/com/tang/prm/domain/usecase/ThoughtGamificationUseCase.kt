package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class GamificationState(
    val totalCount: Int,
    val friendCount: Int,
    val planCount: Int,
    val murmurCount: Int,
    val currentExp: Int,
    val currentLevel: Int,
    val nextLevel: Int,
    val expInLevel: Int,
    val expNeeded: Int,
    val expForNextLevel: Int,
    val expToNextLevel: Int,
    val levelProgress: Float,
    val streak: Int,
    val todoDoneCount: Int,
    val todoTotalCount: Int
)

data class ContactThoughts(
    val contact: Contact,
    val thoughts: List<Thought>,
    val latestThought: Thought?
)

class ThoughtGamificationUseCase @Inject constructor() {

    companion object {
        const val XP_BASE = 3
        const val XP_DONE_BONUS = 2
        const val XP_CONTACT_BONUS = 1
        const val XP_STREAK_BONUS = 1
        const val BASE_XP_PER_LEVEL = 15
    }

    fun calcLevel(exp: Int): Int {
        var level = 1
        while (expForLevel(level + 1) <= exp) level++
        return level
    }

    fun expForLevel(level: Int): Int {
        if (level <= 1) return 0
        return BASE_XP_PER_LEVEL * level * (level - 1) / 2
    }

    fun calcStreak(thoughts: List<Thought>): Int {
        if (thoughts.isEmpty()) return 0
        val zone = ZoneId.systemDefault()
        val daysWithThoughts = thoughts.map {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate().toEpochDay()
        }.toSet()
        val today = LocalDate.now(zone).toEpochDay()
        var streak = 0
        var checkDay = today
        if (daysWithThoughts.contains(checkDay)) {
            streak = 1
            checkDay--
            while (daysWithThoughts.contains(checkDay)) { streak++; checkDay-- }
        }
        return streak
    }

    fun thoughtExp(thought: Thought): Int =
        XP_BASE + (if (thought.isDone) XP_DONE_BONUS else 0) + (if (thought.contactId != null) XP_CONTACT_BONUS else 0)

    fun getGamificationState(
        allThoughts: List<Thought>,
        todoThoughts: List<Thought>,
        contactThoughts: List<ContactThoughts>
    ): GamificationState {
        val totalCount = allThoughts.size
        val friendCount = allThoughts.count { it.type == ThoughtType.FRIEND }
        val planCount = allThoughts.count { it.type == ThoughtType.PLAN }
        val murmurCount = allThoughts.count { it.type == ThoughtType.MURMUR }
        val streak = calcStreak(allThoughts)
        val currentExp = allThoughts.size * XP_BASE +
                todoThoughts.count { it.isDone } * XP_DONE_BONUS +
                contactThoughts.size * XP_CONTACT_BONUS +
                streak * XP_STREAK_BONUS
        val currentLevel = calcLevel(currentExp)
        val nextLevel = currentLevel + 1
        val expInLevel = currentExp - expForLevel(currentLevel)
        val expNeeded = expForLevel(nextLevel) - expForLevel(currentLevel)
        val levelProgress = if (expNeeded > 0) expInLevel.toFloat() / expNeeded else 1f
        val expForNextLevel = expForLevel(nextLevel)
        val expToNextLevel = expForNextLevel - currentExp

        return GamificationState(
            totalCount = totalCount,
            friendCount = friendCount,
            planCount = planCount,
            murmurCount = murmurCount,
            currentExp = currentExp,
            currentLevel = currentLevel,
            nextLevel = nextLevel,
            expInLevel = expInLevel,
            expNeeded = expNeeded,
            expForNextLevel = expForNextLevel,
            expToNextLevel = expToNextLevel,
            levelProgress = levelProgress,
            streak = streak,
            todoDoneCount = todoThoughts.count { it.isDone },
            todoTotalCount = todoThoughts.size
        )
    }
}
