package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.FootprintItem
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.domain.model.SubscriptionStatus
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.domain.model.computedStatus
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class FilterExtTest {

    private fun testSub(
        name: String = "Test",
        price: Double = 10.0,
        cycle: SubscriptionCycle = SubscriptionCycle.MONTHLY,
        category: String? = null,
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
        nextBillingDate: Long = System.currentTimeMillis() + 30 * 86_400_000L
    ) = Subscription(
        name = name, price = price, cycle = cycle,
        startDate = System.currentTimeMillis(), nextBillingDate = nextBillingDate,
        status = status, category = category
    )

    // region IntimacyTier

    @Test
    fun `IntimacyTier of returns correct tier for each score`() {
        assertThat(IntimacyTier.of(10).label).isEqualTo("初识")
        assertThat(IntimacyTier.of(25).label).isEqualTo("泛交")
        assertThat(IntimacyTier.of(50).label).isEqualTo("朋友")
        assertThat(IntimacyTier.of(80).label).isEqualTo("密友")
        assertThat(IntimacyTier.of(95).label).isEqualTo("至亲")
    }

    // endregion

    // region Contact filtering

    @Test
    fun `returns all contacts sorted by intimacy descending when no filter`() {
        val contacts = listOf(
            Contact(id = 1, name = "A", intimacyScore = 30),
            Contact(id = 2, name = "B", intimacyScore = 80),
            Contact(id = 3, name = "C", intimacyScore = 50)
        )
        val result = contacts.filterBy()
        assertThat(result.map { it.id }).containsExactly(2L, 3L, 1L).inOrder()
    }

    @Test
    fun `filters contacts by intimacy level`() {
        val contacts = listOf(
            Contact(id = 1, name = "A", intimacyScore = 10),
            Contact(id = 2, name = "B", intimacyScore = 70),
            Contact(id = 3, name = "C", intimacyScore = 50)
        )
        val result = contacts.filterBy(intimacy = "朋友")
        assertThat(result.map { it.id }).containsExactly(2L, 3L)
    }

    @Test
    fun `filters contacts by keyword matching name`() {
        val contacts = listOf(
            Contact(id = 1, name = "张三", intimacyScore = 50),
            Contact(id = 2, name = "李四", intimacyScore = 60),
            Contact(id = 3, name = "张伟", intimacyScore = 70)
        )
        val result = contacts.filterBy(keyword = "张")
        assertThat(result.map { it.id }).containsExactly(3L, 1L).inOrder()
    }

    @Test
    fun `filters contacts by keyword matching phone`() {
        val contacts = listOf(
            Contact(id = 1, name = "A", phone = "13800001111", intimacyScore = 50),
            Contact(id = 2, name = "B", phone = "13900002222", intimacyScore = 60),
            Contact(id = 3, name = "C", phone = "13800003333", intimacyScore = 70)
        )
        val result = contacts.filterBy(keyword = "138")
        assertThat(result.map { it.id }).containsExactly(3L, 1L).inOrder()
    }

    @Test
    fun `filters contacts by groupId`() {
        val contacts = listOf(
            Contact(id = 1, name = "A", groupId = 10, intimacyScore = 50),
            Contact(id = 2, name = "B", groupId = 20, intimacyScore = 60),
            Contact(id = 3, name = "C", groupId = 10, intimacyScore = 70)
        )
        val result = contacts.filterBy(groupId = 10L)
        assertThat(result.map { it.id }).containsExactly(3L, 1L).inOrder()
    }

    @Test
    fun `combines multiple contact filters`() {
        val contacts = listOf(
            Contact(id = 1, name = "张三", groupId = 10, relationship = "同事", intimacyScore = 50),
            Contact(id = 2, name = "张伟", groupId = 20, relationship = "同事", intimacyScore = 60),
            Contact(id = 3, name = "张丽", groupId = 10, relationship = "同学", intimacyScore = 70),
            Contact(id = 4, name = "李四", groupId = 10, relationship = "同事", intimacyScore = 80)
        )
        val result = contacts.filterBy(keyword = "张", groupId = 10L, relationship = "同事")
        assertThat(result.map { it.id }).containsExactly(1L)
    }

    // endregion

    // region Event filtering

    @Test
    fun `returns all events sorted by time descending when no filters`() {
        val events = listOf(
            Event(id = 1, title = "A", time = 1000L, participants = emptyList()),
            Event(id = 2, title = "B", time = 3000L, participants = emptyList()),
            Event(id = 3, title = "C", time = 2000L, participants = emptyList())
        )
        val result = events.filterBy()
        assertThat(result.map { it.id }).containsExactly(2L, 3L, 1L).inOrder()
    }

    @Test
    fun `filters events by contact`() {
        val contact = Contact(id = 10L, name = "Alice")
        val events = listOf(
            Event(id = 1, title = "A", time = 1000L, participants = listOf(Contact(id = 10L, name = "Alice"))),
            Event(id = 2, title = "B", time = 2000L, participants = listOf(Contact(id = 20L, name = "Bob"))),
            Event(id = 3, title = "C", time = 3000L, participants = listOf(Contact(id = 10L, name = "Alice")))
        )
        val result = events.filterBy(contact = contact)
        assertThat(result.map { it.id }).containsExactly(3L, 1L).inOrder()
    }

    @Test
    fun `filters events by event type`() {
        val events = listOf(
            Event(id = 1, title = "A", time = 1000L, type = EventType.MEETUP, participants = emptyList()),
            Event(id = 2, title = "B", time = 2000L, type = EventType.DINING, participants = emptyList()),
            Event(id = 3, title = "C", time = 3000L, type = EventType.MEETUP, participants = emptyList())
        )
        val result = events.filterBy(eventType = "MEETUP")
        assertThat(result.map { it.id }).containsExactly(3L, 1L).inOrder()
    }

    @Test
    fun `filters events by search query`() {
        val events = listOf(
            Event(id = 1, title = "团队聚餐", time = 1000L, participants = emptyList()),
            Event(id = 2, title = "周末旅行", time = 2000L, participants = emptyList()),
            Event(id = 3, title = "聚餐聚会", time = 3000L, participants = emptyList())
        )
        val result = events.filterBy(searchQuery = "聚餐")
        assertThat(result.map { it.id }).containsExactly(3L, 1L).inOrder()
    }

    @Test
    fun `eventType all returns all events`() {
        val events = listOf(
            Event(id = 1, title = "A", time = 1000L, type = EventType.MEETUP, participants = emptyList()),
            Event(id = 2, title = "B", time = 2000L, type = EventType.DINING, participants = emptyList())
        )
        val result = events.filterBy(eventType = "all")
        assertThat(result.map { it.id }).containsExactly(2L, 1L).inOrder()
    }

    // endregion

    // region Thought filtering

    @Test
    fun `filters thoughts by type`() {
        val thoughts = listOf(
            Thought(id = 1, content = "A", type = ThoughtType.FRIEND),
            Thought(id = 2, content = "B", type = ThoughtType.PLAN),
            Thought(id = 3, content = "C", type = ThoughtType.FRIEND)
        )
        val result = thoughts.filterBy(filter = "friend")
        assertThat(result.map { it.id }).containsExactly(1L, 3L).inOrder()
    }

    @Test
    fun `filters thoughts by contact`() {
        val thoughts = listOf(
            Thought(id = 1, content = "A", contactId = 10L),
            Thought(id = 2, content = "B", contactId = 20L),
            Thought(id = 3, content = "C", contactId = 10L)
        )
        val result = thoughts.filterBy(selectedContactId = 10L)
        assertThat(result.map { it.id }).containsExactly(1L, 3L).inOrder()
    }

    @Test
    fun `filters thoughts by search query matching content`() {
        val thoughts = listOf(
            Thought(id = 1, content = "周末计划"),
            Thought(id = 2, content = "工作安排"),
            Thought(id = 3, content = "周末出游")
        )
        val result = thoughts.filterBy(searchQuery = "周末")
        assertThat(result.map { it.id }).containsExactly(1L, 3L).inOrder()
    }

    // endregion

    // region Footprint filtering

    @Test
    fun `filters footprints by contact`() {
        val footprints = listOf(
            FootprintItem(id = 1, location = "北京", date = 1000L, eventType = "MEETUP", eventTitle = "A", contactId = 10L, contactName = "A", contactAvatar = null, description = null, weather = null, emotion = null, photoCount = 0),
            FootprintItem(id = 2, location = "上海", date = 2000L, eventType = "DINING", eventTitle = "B", contactId = 20L, contactName = "B", contactAvatar = null, description = null, weather = null, emotion = null, photoCount = 0),
            FootprintItem(id = 3, location = "北京", date = 3000L, eventType = "MEETUP", eventTitle = "C", contactId = 10L, contactName = "C", contactAvatar = null, description = null, weather = null, emotion = null, photoCount = 0)
        )
        val result = footprints.filterBy(selectedContactId = 10L)
        assertThat(result.map { it.id }).containsExactly(1L, 3L).inOrder()
    }

    @Test
    fun `filters footprints by event type`() {
        val footprints = listOf(
            FootprintItem(id = 1, location = "北京", date = 1000L, eventType = "MEETUP", eventTitle = "A", contactId = null, contactName = null, contactAvatar = null, description = null, weather = null, emotion = null, photoCount = 0),
            FootprintItem(id = 2, location = "上海", date = 2000L, eventType = "DINING", eventTitle = "B", contactId = null, contactName = null, contactAvatar = null, description = null, weather = null, emotion = null, photoCount = 0),
            FootprintItem(id = 3, location = "北京", date = 3000L, eventType = "MEETUP", eventTitle = "C", contactId = null, contactName = null, contactAvatar = null, description = null, weather = null, emotion = null, photoCount = 0)
        )
        val result = footprints.filterBy(filterEventType = "MEETUP")
        assertThat(result.map { it.id }).containsExactly(1L, 3L).inOrder()
    }

    // endregion

    // region Photo filtering

    @Test
    fun `filters photos by contact`() {
        val photos = listOf(
            AlbumPhoto(id = "p1", uri = "", sourceType = "event", sourceId = 1, sourceTitle = "A", contactId = 10L, contactName = "A", contactAvatar = null, date = 1000L, location = null),
            AlbumPhoto(id = "p2", uri = "", sourceType = "event", sourceId = 2, sourceTitle = "B", contactId = 20L, contactName = "B", contactAvatar = null, date = 2000L, location = null),
            AlbumPhoto(id = "p3", uri = "", sourceType = "gift", sourceId = 3, sourceTitle = "C", contactId = 10L, contactName = "C", contactAvatar = null, date = 3000L, location = null)
        )
        val result = photos.filterBy(selectedContactId = 10L)
        assertThat(result.map { it.id }).containsExactly("p1", "p3").inOrder()
    }

    @Test
    fun `filters photos by source type`() {
        val photos = listOf(
            AlbumPhoto(id = "p1", uri = "", sourceType = "event", sourceId = 1, sourceTitle = "A", contactId = 10L, contactName = "A", contactAvatar = null, date = 1000L, location = null),
            AlbumPhoto(id = "p2", uri = "", sourceType = "gift", sourceId = 2, sourceTitle = "B", contactId = 20L, contactName = "B", contactAvatar = null, date = 2000L, location = null),
            AlbumPhoto(id = "p3", uri = "", sourceType = "event", sourceId = 3, sourceTitle = "C", contactId = 10L, contactName = "C", contactAvatar = null, date = 3000L, location = null)
        )
        val result = photos.filterBy(filterSourceType = "event")
        assertThat(result.map { it.id }).containsExactly("p1", "p3").inOrder()
    }

    // endregion

    // region Subscription filtering

    @Test
    fun `filters subscriptions by status`() {
        val subs = listOf(
            testSub(name = "A", status = SubscriptionStatus.ACTIVE),
            testSub(name = "B", status = SubscriptionStatus.EXPIRED),
            testSub(name = "C", status = SubscriptionStatus.ACTIVE)
        )
        val result = subs.filterBy(status = SubscriptionStatus.ACTIVE)
        assertThat(result.map { it.name }).containsExactly("A", "C")
    }

    @Test
    fun `filters subscriptions by category`() {
        val subs = listOf(
            testSub(name = "A", category = "娱乐"),
            testSub(name = "B", category = "工具"),
            testSub(name = "C", category = "娱乐")
        )
        val result = subs.filterBy(category = "娱乐")
        assertThat(result.map { it.name }).containsExactly("A", "C")
    }

    @Test
    fun `filters subscriptions by keyword`() {
        val subs = listOf(
            testSub(name = "Netflix"),
            testSub(name = "Spotify"),
            testSub(name = "Netflix Premium")
        )
        val result = subs.filterBy(keyword = "netflix")
        assertThat(result.map { it.name }).containsExactly("Netflix", "Netflix Premium")
    }

    @Test
    fun `combines multiple subscription filters`() {
        val subs = listOf(
            testSub(name = "Netflix", status = SubscriptionStatus.ACTIVE, category = "娱乐"),
            testSub(name = "Spotify", status = SubscriptionStatus.ACTIVE, category = "工具"),
            testSub(name = "Netflix Premium", status = SubscriptionStatus.EXPIRED, category = "娱乐")
        )
        val result = subs.filterBy(status = SubscriptionStatus.ACTIVE, category = "娱乐")
        assertThat(result.map { it.name }).containsExactly("Netflix")
    }

    @Test
    fun `subscription filter with no matches returns empty`() {
        val subs = listOf(testSub(name = "A", status = SubscriptionStatus.ACTIVE))
        val result = subs.filterBy(status = SubscriptionStatus.EXPIRED)
        assertThat(result).isEmpty()
    }

    // endregion

    // region IntimacyLevels

    @Test
    fun `IntimacyLevels contains all levels in order`() {
        assertThat(IntimacyLevels).containsExactly(
            "初识", "泛交", "朋友", "密友", "至亲"
        ).inOrder()
    }

    // endregion
}
