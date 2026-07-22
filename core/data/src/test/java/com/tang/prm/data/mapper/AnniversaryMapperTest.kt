package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.AnniversaryEntity
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnniversaryMapperTest {

    @BeforeEach
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.w(any(), any<String>()) } returns 0
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun anniversaryEntity_toDomain_mapsAllFields() {
        val entity = AnniversaryEntity(
            id = 1, contactId = 10, name = "生日", type = "BIRTHDAY",
            date = 1000L, isRepeat = true, reminderDays = 3,
            remarks = "备注", icon = "cake", createdAt = 2000L, updatedAt = 3000L
        )

        val domain = entity.toDomain(contactName = "张三", contactAvatar = "avatar.png")

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.contactId).isEqualTo(10)
        assertThat(domain.name).isEqualTo("生日")
        assertThat(domain.type).isEqualTo(AnniversaryType.BIRTHDAY)
        assertThat(domain.date).isEqualTo(1000L)
        assertThat(domain.isRepeat).isTrue()
        assertThat(domain.reminderDays).isEqualTo(3)
        assertThat(domain.remarks).isEqualTo("备注")
        assertThat(domain.contactName).isEqualTo("张三")
        assertThat(domain.contactAvatar).isEqualTo("avatar.png")
        assertThat(domain.icon).isEqualTo("cake")
        assertThat(domain.createdAt).isEqualTo(2000L)
        assertThat(domain.updatedAt).isEqualTo(3000L)
    }

    @Test
    fun anniversaryEntity_toDomain_typeAnniversary() {
        val entity = AnniversaryEntity(id = 1, contactId = 10, name = "纪念日", type = "ANNIVERSARY", date = 1000L)

        val domain = entity.toDomain(contactName = null, contactAvatar = null)

        assertThat(domain.type).isEqualTo(AnniversaryType.ANNIVERSARY)
    }

    @Test
    fun anniversaryEntity_toDomain_typeHoliday() {
        val entity = AnniversaryEntity(id = 1, contactId = 10, name = "节日", type = "HOLIDAY", date = 1000L)

        val domain = entity.toDomain(contactName = null, contactAvatar = null)

        assertThat(domain.type).isEqualTo(AnniversaryType.HOLIDAY)
    }

    @Test
    fun anniversaryEntity_toDomain_unknownType_fallsBackToBirthday() {
        val entity = AnniversaryEntity(id = 1, contactId = 10, name = "未知", type = "UNKNOWN", date = 1000L)

        val domain = entity.toDomain(contactName = null, contactAvatar = null)

        assertThat(domain.type).isEqualTo(AnniversaryType.BIRTHDAY)
    }

    @Test
    fun anniversary_toEntity_mapsAllFields() {
        val domain = Anniversary(
            id = 1, contactId = 10, name = "生日", type = AnniversaryType.BIRTHDAY,
            date = 1000L, isRepeat = true, reminderDays = 3,
            remarks = "备注", contactName = "张三", contactAvatar = "avatar.png",
            icon = "cake", createdAt = 2000L, updatedAt = 3000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.contactId).isEqualTo(10)
        assertThat(entity.name).isEqualTo("生日")
        assertThat(entity.type).isEqualTo("BIRTHDAY")
        assertThat(entity.date).isEqualTo(1000L)
        assertThat(entity.isRepeat).isTrue()
        assertThat(entity.reminderDays).isEqualTo(3)
        assertThat(entity.remarks).isEqualTo("备注")
        assertThat(entity.icon).isEqualTo("cake")
        assertThat(entity.createdAt).isEqualTo(2000L)
        assertThat(entity.updatedAt).isEqualTo(3000L)
    }

    @Test
    fun anniversary_toEntity_nullContactId_mapsToZero() {
        val domain = Anniversary(
            id = 1, contactId = null, name = "节日", type = AnniversaryType.HOLIDAY,
            date = 1000L
        )

        val entity = domain.toEntity()

        assertThat(entity.contactId).isEqualTo(0L)
    }

    @Test
    fun anniversary_toEntity_typeAnniversary_nameIsAnniversary() {
        val domain = Anniversary(id = 1, contactId = 10, name = "纪念日", type = AnniversaryType.ANNIVERSARY, date = 1000L)

        val entity = domain.toEntity()

        assertThat(entity.type).isEqualTo("ANNIVERSARY")
    }

    @Test
    fun anniversaryEntity_roundtrip_preservesEntityFields() {
        val original = AnniversaryEntity(
            id = 1, contactId = 10, name = "生日", type = "BIRTHDAY",
            date = 1000L, isRepeat = false, reminderDays = 7,
            remarks = "备注", icon = "gift", createdAt = 2000L, updatedAt = 3000L
        )

        val roundtrip = original.toDomain(contactName = null, contactAvatar = null).toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }
}
