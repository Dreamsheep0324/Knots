package com.tang.prm.engine.divination.data

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DataIntegrityTest {

    @Nested
    @DisplayName("HexagramData 64卦完整性")
    inner class HexagramDataTest {

        @Test
        fun has64Hexagrams() {
            assertThat(HexagramData.all).hasSize(64)
        }

        @Test
        fun allNamesAreUnique() {
            val names = HexagramData.all.map { it.name }
            assertThat(names.toSet()).hasSize(64)
        }

        @Test
        fun allBinarySymbolsAreUnique() {
            val binaries = HexagramData.all.map { it.binarySymbol }
            assertThat(binaries.toSet()).hasSize(64)
        }

        @Test
        fun allSymbolsAreUnique() {
            val symbols = HexagramData.all.map { it.symbol }
            assertThat(symbols.toSet()).hasSize(64)
        }

        @Test
        fun allIdsAreUnique() {
            val ids = HexagramData.all.map { it.id }
            assertThat(ids.toSet()).hasSize(64)
        }

        @Test
        fun findByName_worksForAll() {
            HexagramData.all.forEach { hexagram ->
                val found = HexagramData.findByName(hexagram.name)
                assertThat(found).isEqualTo(hexagram)
            }
        }

        @Test
        fun findByBinary_worksForAll() {
            HexagramData.all.forEach { hexagram ->
                val found = HexagramData.findByBinary(hexagram.binarySymbol)
                assertThat(found).isEqualTo(hexagram)
            }
        }

        @Test
        fun findBySymbol_worksForAll() {
            HexagramData.all.forEach { hexagram ->
                val found = HexagramData.findBySymbol(hexagram.symbol)
                assertThat(found).isEqualTo(hexagram)
            }
        }

        @Test
        fun findByName_returnsNullForUnknown() {
            assertThat(HexagramData.findByName("不存在")).isNull()
        }

        @Test
        fun allBinarySymbolsAre6Chars() {
            HexagramData.all.forEach { hexagram ->
                assertThat(hexagram.binarySymbol).hasLength(6)
            }
        }

        @Test
        fun allBinarySymbolsContainOnly01() {
            HexagramData.all.forEach { hexagram ->
                hexagram.binarySymbol.forEach { char ->
                    assertThat(char.toString()).isAnyOf("0", "1")
                }
            }
        }
    }

    @Nested
    @DisplayName("TrigramData 八卦完整性")
    inner class TrigramDataTest {

        @Test
        fun has8Trigrams() {
            assertThat(TrigramData.byIndex).hasSize(8)
        }

        @Test
        fun indicesAre1To8() {
            val keys = TrigramData.byIndex.keys
            assertThat(keys).containsExactly(1, 2, 3, 4, 5, 6, 7, 8).inOrder()
        }

        @Test
        fun allLinesHave3Elements() {
            TrigramData.byIndex.values.forEach { trigram ->
                assertThat(trigram.lines).hasSize(3)
            }
        }

        @Test
        fun allLinesContainOnly01() {
            TrigramData.byIndex.values.forEach { trigram ->
                trigram.lines.forEach { line ->
                    assertThat(line).isAnyOf(0, 1)
                }
            }
        }

        @Test
        fun findBySymbol_worksForAll() {
            TrigramData.byIndex.values.forEach { trigram ->
                val found = TrigramData.findBySymbol(trigram.symbol)
                assertThat(found).isEqualTo(trigram)
            }
        }

        @Test
        fun findByLines_worksForAll() {
            TrigramData.byIndex.entries.forEach { (index, trigram) ->
                val found = TrigramData.findByLines(trigram.lines)
                assertThat(found).isNotNull()
                assertThat(found!!.first).isEqualTo(index)
            }
        }

        @Test
        fun findBySymbol_returnsNullForUnknown() {
            assertThat(TrigramData.findBySymbol("X")).isNull()
        }

        @Test
        fun findByLines_returnsNullForUnknown() {
            assertThat(TrigramData.findByLines(listOf(9, 9, 9))).isNull()
        }
    }

    @Nested
    @DisplayName("PalaceData 八宫完整性")
    inner class PalaceDataTest {

        @Test
        fun has8Palaces() {
            assertThat(PalaceData.palaces).hasSize(8)
        }

        @Test
        fun palaceNamesMatchKeys() {
            PalaceData.palaces.forEach { (name, palace) ->
                assertThat(palace.name).isEqualTo(name)
            }
        }

        @Test
        fun palaceHexagrams_has8Lists() {
            assertThat(PalaceData.palaceHexagrams).hasSize(8)
        }

        @Test
        fun eachPalaceHas8Hexagrams() {
            PalaceData.palaceHexagrams.forEach { (palace, hexagrams) ->
                assertThat(hexagrams).hasSize(8)
            }
        }

        @Test
        fun totalHexagramsInPalaces_is64() {
            val total = PalaceData.palaceHexagrams.values.sumOf { it.size }
            assertThat(total).isEqualTo(64)
        }

        @Test
        fun allPalaceHexagramsAreUnique() {
            val allHexagrams = PalaceData.palaceHexagrams.values.flatten()
            assertThat(allHexagrams.toSet()).hasSize(64)
        }

        @Test
        fun hexagramPalaceMap_coversAll64() {
            assertThat(PalaceData.hexagramPalaceMap).hasSize(64)
        }

        @Test
        fun hexagramPalaceMap_isConsistentWithPalaceHexagrams() {
            PalaceData.palaceHexagrams.forEach { (palace, hexagrams) ->
                hexagrams.forEach { hexagram ->
                    assertThat(PalaceData.hexagramPalaceMap[hexagram]).isEqualTo(palace)
                }
            }
        }

        @Test
        fun eachPalaceHasCorrectWuxing() {
            assertThat(PalaceData.palaces["乾"]!!.wuxing).isEqualTo("金")
            assertThat(PalaceData.palaces["兑"]!!.wuxing).isEqualTo("金")
            assertThat(PalaceData.palaces["离"]!!.wuxing).isEqualTo("火")
            assertThat(PalaceData.palaces["震"]!!.wuxing).isEqualTo("木")
            assertThat(PalaceData.palaces["巽"]!!.wuxing).isEqualTo("木")
            assertThat(PalaceData.palaces["坎"]!!.wuxing).isEqualTo("水")
            assertThat(PalaceData.palaces["艮"]!!.wuxing).isEqualTo("土")
            assertThat(PalaceData.palaces["坤"]!!.wuxing).isEqualTo("土")
        }
    }

    @Nested
    @DisplayName("NaJiaData 纳甲完整性")
    inner class NaJiaDataTest {

        @Test
        fun has64Entries() {
            assertThat(NaJiaData.hexagramNaJia).hasSize(64)
        }

        @Test
        fun eachEntryHas6Dizhi() {
            NaJiaData.hexagramNaJia.forEach { (hexagram, dizhiList) ->
                assertThat(dizhiList).hasSize(6)
            }
        }

        @Test
        fun allDizhiAreValid() {
            val validDizhi = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
            NaJiaData.hexagramNaJia.forEach { (_, dizhiList) ->
                dizhiList.forEach { dizhi ->
                    assertThat(validDizhi).contains(dizhi)
                }
            }
        }

        @Test
        fun allHexagramNamesExistInHexagramData() {
            NaJiaData.hexagramNaJia.keys.forEach { name ->
                assertThat(HexagramData.findByName(name)).isNotNull()
            }
        }
    }

    @Nested
    @DisplayName("ExternalOmenData 外应数据完整性")
    inner class ExternalOmenDataTest {

        @Test
        fun has6Categories() {
            assertThat(ExternalOmenData.categories).hasSize(6)
        }

        @Test
        fun eachCategoryHas8Options() {
            ExternalOmenData.categories.forEach { category ->
                assertThat(category.options).hasSize(8)
            }
        }

        @Test
        fun priorityHas6Items() {
            assertThat(ExternalOmenData.priority).hasSize(6)
        }

        @Test
        fun priorityMatchesCategoryKeys() {
            val categoryKeys = ExternalOmenData.categories.map { it.key }
            assertThat(ExternalOmenData.priority).containsExactlyElementsIn(categoryKeys).inOrder()
        }

        @Test
        fun allTrigramIndicesAreInRange1To8() {
            ExternalOmenData.categories.forEach { category ->
                category.options.forEach { option ->
                    assertThat(option.trigramIndex).isIn(1..8)
                }
            }
        }

        @Test
        fun resolveOmens_returnsCorrectIndices() {
            val selections = mapOf(
                "direction" to ExternalOmenData.directionOptions[0], // 东 → 震(4)
                "person" to ExternalOmenData.personOptions[0]        // 老父 → 乾(1)
            )
            val (upper, lower, moving) = ExternalOmenData.resolveOmens(selections, 3)
            assertThat(upper).isEqualTo(4)
            assertThat(lower).isEqualTo(1)
            assertThat(moving).isEqualTo(3)
        }

        @Test
        fun resolveOmens_insufficientSelections_throws() {
            assertThrows<IllegalArgumentException> {
                ExternalOmenData.resolveOmens(emptyMap(), 3)
            }
        }

        @Test
        fun resolveOmens_zeroCount_throws() {
            val selections = mapOf(
                "direction" to ExternalOmenData.directionOptions[0],
                "person" to ExternalOmenData.personOptions[0]
            )
            assertThrows<IllegalArgumentException> {
                ExternalOmenData.resolveOmens(selections, 0)
            }
        }

        @Test
        fun buildSummary_containsAllSelections() {
            val selections = mapOf(
                "direction" to ExternalOmenData.directionOptions[0],
                "person" to ExternalOmenData.personOptions[0]
            )
            val summary = ExternalOmenData.buildSummary(selections, 5)
            assertThat(summary).contains("方位")
            assertThat(summary).contains("人物")
            assertThat(summary).contains("数量：5")
        }

        @Test
        fun buildSummary_usesPriorityOrder() {
            val selections = mapOf(
                "color" to ExternalOmenData.colorOptions[0],
                "direction" to ExternalOmenData.directionOptions[0],
                "person" to ExternalOmenData.personOptions[0]
            )
            val summary = ExternalOmenData.buildSummary(selections, 1)
            // direction should come before person, person before color
            val dirIdx = summary.indexOf("方位")
            val personIdx = summary.indexOf("人物")
            val colorIdx = summary.indexOf("颜色")
            assertThat(dirIdx).isLessThan(personIdx)
            assertThat(personIdx).isLessThan(colorIdx)
        }
    }
}
