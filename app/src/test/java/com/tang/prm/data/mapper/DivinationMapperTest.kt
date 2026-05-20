package com.tang.prm.data.mapper

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.entity.DivinationRecordEntity
import com.tang.prm.domain.divination.model.DivinationRecord
import org.junit.jupiter.api.Test

class DivinationMapperTest {

    @Test
    fun divinationMapper_toDomain_mapsAllFields() {
        val entity = DivinationRecordEntity(
            id = 1, method = "liuyao", question = "事业如何",
            resultJson = """{"hexagram":"乾"}""", createdAt = 1000L, aiAnalysis = "大吉"
        )

        val domain = DivinationMapper.toDomain(entity)

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.method).isEqualTo("liuyao")
        assertThat(domain.question).isEqualTo("事业如何")
        assertThat(domain.resultJson).isEqualTo("""{"hexagram":"乾"}""")
        assertThat(domain.createdAt).isEqualTo(1000L)
        assertThat(domain.aiAnalysis).isEqualTo("大吉")
    }

    @Test
    fun divinationMapper_toEntity_mapsAllFields() {
        val domain = DivinationRecord(
            id = 1, method = "meihua", question = "感情如何",
            resultJson = """{"trigram":"坤"}""", createdAt = 1000L, aiAnalysis = "平稳"
        )

        val entity = DivinationMapper.toEntity(domain)

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.method).isEqualTo("meihua")
        assertThat(entity.question).isEqualTo("感情如何")
        assertThat(entity.resultJson).isEqualTo("""{"trigram":"坤"}""")
        assertThat(entity.createdAt).isEqualTo(1000L)
        assertThat(entity.aiAnalysis).isEqualTo("平稳")
    }

    @Test
    fun divinationMapper_toEntity_idZeroPreserved() {
        val domain = DivinationRecord(
            id = 0, method = "liuyao", question = "问题",
            resultJson = "{}", createdAt = 1000L
        )

        val entity = DivinationMapper.toEntity(domain)

        assertThat(entity.id).isEqualTo(0)
    }

    @Test
    fun divinationMapper_toEntity_negativeId_resetsToZero() {
        val domain = DivinationRecord(
            id = -1, method = "liuyao", question = "问题",
            resultJson = "{}", createdAt = 1000L
        )

        val entity = DivinationMapper.toEntity(domain)

        assertThat(entity.id).isEqualTo(0)
    }

    @Test
    fun divinationMapper_roundtrip_preservesAllFields() {
        val original = DivinationRecordEntity(
            id = 1, method = "liuyao", question = "财运如何",
            resultJson = """{"result":"吉"}""", createdAt = 1000L, aiAnalysis = "分析"
        )

        val roundtrip = DivinationMapper.toEntity(DivinationMapper.toDomain(original))

        assertThat(roundtrip).isEqualTo(original)
    }
}
