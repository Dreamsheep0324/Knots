package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.entity.ContactGroupEntity
import com.tang.prm.data.local.entity.ContactTagEntity
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.ContactTag
import org.junit.jupiter.api.Test

class ContactMapperTest {

    @Test
    fun contactEntity_toDomain_mapsAllFields() {
        val entity = ContactEntity(
            id = 1, name = "张三", avatar = "avatar.png", nickname = "小三",
            gender = 1, birthday = 1000L, isLunarBirthday = true, knowingDate = 2000L,
            phone = "13800138000", email = "zhangsan@test.com", city = "北京",
            address = "朝阳区", education = "本科", company = "科技公司",
            jobTitle = "工程师", industry = "互联网", hobby = "篮球",
            habit = "早起", diet = "素食", skill = "编程", mbti = "INTJ",
            spouseName = "李四", childrenCount = 2, childrenNames = "张小一;张小二",
            introducer = "王五", relationshipLevel = 5, relationship = "朋友",
            groupId = 10L, intimacyScore = 80, lastInteractionTime = 3000L,
            customFields = """{"key":"value"}""", notes = "重要客户",
            createdAt = 4000L, updatedAt = 5000L
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.name).isEqualTo("张三")
        assertThat(domain.avatar).isEqualTo("avatar.png")
        assertThat(domain.nickname).isEqualTo("小三")
        assertThat(domain.gender).isEqualTo(1)
        assertThat(domain.birthday).isEqualTo(1000L)
        assertThat(domain.isLunarBirthday).isTrue()
        assertThat(domain.knowingDate).isEqualTo(2000L)
        assertThat(domain.phone).isEqualTo("13800138000")
        assertThat(domain.email).isEqualTo("zhangsan@test.com")
        assertThat(domain.city).isEqualTo("北京")
        assertThat(domain.address).isEqualTo("朝阳区")
        assertThat(domain.education).isEqualTo("本科")
        assertThat(domain.company).isEqualTo("科技公司")
        assertThat(domain.jobTitle).isEqualTo("工程师")
        assertThat(domain.industry).isEqualTo("互联网")
        assertThat(domain.hobby).isEqualTo("篮球")
        assertThat(domain.habit).isEqualTo("早起")
        assertThat(domain.diet).isEqualTo("素食")
        assertThat(domain.skill).isEqualTo("编程")
        assertThat(domain.mbti).isEqualTo("INTJ")
        assertThat(domain.spouseName).isEqualTo("李四")
        assertThat(domain.childrenCount).isEqualTo(2)
        assertThat(domain.childrenNames).isEqualTo("张小一;张小二")
        assertThat(domain.introducer).isEqualTo("王五")
        assertThat(domain.relationshipLevel).isEqualTo(5)
        assertThat(domain.relationship).isEqualTo("朋友")
        assertThat(domain.groupId).isEqualTo(10L)
        assertThat(domain.intimacyScore).isEqualTo(80)
        assertThat(domain.lastInteractionTime).isEqualTo(3000L)
        assertThat(domain.customFields).isEqualTo("""{"key":"value"}""")
        assertThat(domain.notes).isEqualTo("重要客户")
        assertThat(domain.createdAt).isEqualTo(4000L)
        assertThat(domain.updatedAt).isEqualTo(5000L)
    }

    @Test
    fun contact_toEntity_mapsAllFields() {
        val domain = Contact(
            id = 1, name = "张三", avatar = "avatar.png", nickname = "小三",
            gender = 1, birthday = 1000L, isLunarBirthday = true, knowingDate = 2000L,
            phone = "13800138000", email = "zhangsan@test.com", city = "北京",
            address = "朝阳区", education = "本科", company = "科技公司",
            jobTitle = "工程师", industry = "互联网", hobby = "篮球",
            habit = "早起", diet = "素食", skill = "编程", mbti = "INTJ",
            spouseName = "李四", childrenCount = 2, childrenNames = "张小一;张小二",
            introducer = "王五", relationshipLevel = 5, relationship = "朋友",
            groupId = 10L, intimacyScore = 80, lastInteractionTime = 3000L,
            customFields = """{"key":"value"}""", notes = "重要客户",
            createdAt = 4000L, updatedAt = 5000L
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.name).isEqualTo("张三")
        assertThat(entity.avatar).isEqualTo("avatar.png")
        assertThat(entity.nickname).isEqualTo("小三")
        assertThat(entity.gender).isEqualTo(1)
        assertThat(entity.birthday).isEqualTo(1000L)
        assertThat(entity.isLunarBirthday).isTrue()
        assertThat(entity.knowingDate).isEqualTo(2000L)
        assertThat(entity.phone).isEqualTo("13800138000")
        assertThat(entity.email).isEqualTo("zhangsan@test.com")
        assertThat(entity.city).isEqualTo("北京")
        assertThat(entity.address).isEqualTo("朝阳区")
        assertThat(entity.education).isEqualTo("本科")
        assertThat(entity.company).isEqualTo("科技公司")
        assertThat(entity.jobTitle).isEqualTo("工程师")
        assertThat(entity.industry).isEqualTo("互联网")
        assertThat(entity.hobby).isEqualTo("篮球")
        assertThat(entity.habit).isEqualTo("早起")
        assertThat(entity.diet).isEqualTo("素食")
        assertThat(entity.skill).isEqualTo("编程")
        assertThat(entity.mbti).isEqualTo("INTJ")
        assertThat(entity.spouseName).isEqualTo("李四")
        assertThat(entity.childrenCount).isEqualTo(2)
        assertThat(entity.childrenNames).isEqualTo("张小一;张小二")
        assertThat(entity.introducer).isEqualTo("王五")
        assertThat(entity.relationshipLevel).isEqualTo(5)
        assertThat(entity.relationship).isEqualTo("朋友")
        assertThat(entity.groupId).isEqualTo(10L)
        assertThat(entity.intimacyScore).isEqualTo(80)
        assertThat(entity.lastInteractionTime).isEqualTo(3000L)
        assertThat(entity.customFields).isEqualTo("""{"key":"value"}""")
        assertThat(entity.notes).isEqualTo("重要客户")
        assertThat(entity.createdAt).isEqualTo(4000L)
        assertThat(entity.updatedAt).isEqualTo(5000L)
    }

    @Test
    fun contactEntity_roundtrip_preservesAllFields() {
        val original = ContactEntity(
            id = 1, name = "张三", avatar = "a", nickname = "b",
            gender = 2, birthday = 100L, isLunarBirthday = true, knowingDate = 200L,
            phone = "13800138000", email = "e", city = "c", address = "a",
            education = "e", company = "c", jobTitle = "j", industry = "i",
            hobby = "h", habit = "h", diet = "d", skill = "s", mbti = "M",
            spouseName = "s", childrenCount = 1, childrenNames = "cn",
            introducer = "i", relationshipLevel = 3, relationship = "r",
            groupId = 5L, intimacyScore = 60, lastInteractionTime = 300L,
            customFields = "cf", notes = "n", createdAt = 400L, updatedAt = 500L
        )

        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip).isEqualTo(original)
    }

    @Test
    fun contactEntity_nullFields_toDomain_preservesNulls() {
        val entity = ContactEntity(
            id = 1, name = "张三", phone = null, email = null,
            avatar = null, nickname = null, birthday = null, knowingDate = null,
            city = null, address = null, education = null, company = null,
            jobTitle = null, industry = null, hobby = null, habit = null,
            diet = null, skill = null, mbti = null, spouseName = null,
            childrenNames = null, introducer = null, relationship = null,
            groupId = null, lastInteractionTime = null, customFields = null,
            notes = null, createdAt = 1000L, updatedAt = 1000L
        )

        val domain = entity.toDomain()

        assertThat(domain.phone).isNull()
        assertThat(domain.email).isNull()
        assertThat(domain.avatar).isNull()
        assertThat(domain.nickname).isNull()
        assertThat(domain.birthday).isNull()
        assertThat(domain.knowingDate).isNull()
        assertThat(domain.city).isNull()
        assertThat(domain.address).isNull()
        assertThat(domain.education).isNull()
        assertThat(domain.company).isNull()
        assertThat(domain.jobTitle).isNull()
        assertThat(domain.industry).isNull()
        assertThat(domain.hobby).isNull()
        assertThat(domain.habit).isNull()
        assertThat(domain.diet).isNull()
        assertThat(domain.skill).isNull()
        assertThat(domain.mbti).isNull()
        assertThat(domain.spouseName).isNull()
        assertThat(domain.childrenNames).isNull()
        assertThat(domain.introducer).isNull()
        assertThat(domain.relationship).isNull()
        assertThat(domain.groupId).isNull()
        assertThat(domain.lastInteractionTime).isNull()
        assertThat(domain.customFields).isNull()
        assertThat(domain.notes).isNull()
    }

    @Test
    fun contactGroupEntity_toDomain_mapsAllFields() {
        val entity = ContactGroupEntity(id = 1, name = "朋友", color = "#FF0000", sortOrder = 2, createdAt = 1000L)

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.name).isEqualTo("朋友")
        assertThat(domain.color).isEqualTo("#FF0000")
        assertThat(domain.sortOrder).isEqualTo(2)
    }

    @Test
    fun contactGroup_toEntity_mapsAllFields() {
        val domain = ContactGroup(id = 1, name = "朋友", color = "#FF0000", sortOrder = 2)

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.name).isEqualTo("朋友")
        assertThat(entity.color).isEqualTo("#FF0000")
        assertThat(entity.sortOrder).isEqualTo(2)
    }

    @Test
    fun contactGroupEntity_roundtrip_preservesFields() {
        val original = ContactGroupEntity(id = 1, name = "同事", color = "#00FF00", sortOrder = 5, createdAt = 1000L)
        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip.id).isEqualTo(original.id)
        assertThat(roundtrip.name).isEqualTo(original.name)
        assertThat(roundtrip.color).isEqualTo(original.color)
        assertThat(roundtrip.sortOrder).isEqualTo(original.sortOrder)
    }

    @Test
    fun contactTagEntity_toDomain_mapsAllFields() {
        val entity = ContactTagEntity(id = 1, name = "VIP", color = "#0000FF", createdAt = 1000L)

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.name).isEqualTo("VIP")
        assertThat(domain.color).isEqualTo("#0000FF")
    }

    @Test
    fun contactTag_toEntity_mapsAllFields() {
        val domain = ContactTag(id = 1, name = "VIP", color = "#0000FF")

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.name).isEqualTo("VIP")
        assertThat(entity.color).isEqualTo("#0000FF")
    }

    @Test
    fun contactTagEntity_roundtrip_preservesFields() {
        val original = ContactTagEntity(id = 1, name = "重要", color = "#FF00FF", createdAt = 1000L)
        val roundtrip = original.toDomain().toEntity()

        assertThat(roundtrip.id).isEqualTo(original.id)
        assertThat(roundtrip.name).isEqualTo(original.name)
        assertThat(roundtrip.color).isEqualTo(original.color)
    }
}
