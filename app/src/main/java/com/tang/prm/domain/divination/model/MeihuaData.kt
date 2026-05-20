package com.tang.prm.domain.divination.model

data class MeihuaData(
    val originalName: String,
    val changedName: String,
    val interName: String,
    val ganzhi: GanZhiInfo,
    val timestamp: Long,
    val tiGua: TiYongGuaInfo,
    val yongGua: TiYongGuaInfo,
    val changedTiGua: TiYongGuaInfo?,
    val changedYongGua: TiYongGuaInfo?,
    val movingYao: MovingYaoInfo,
    val analysis: MeihuaAnalysis,
    val mainHexagram: HexagramDetail,
    val interHexagram: HexagramDetail?,
    val changedHexagram: HexagramDetail?,
    val yaosDetail: List<MeihuaYaoDetail>,
    val calculation: MeihuaCalculation?
)

data class TiYongGuaInfo(
    val name: String,
    val element: String,
    val nature: String
)

data class MovingYaoInfo(
    val position: Int,
    val description: String,
    val yaoName: String
)

data class MeihuaAnalysis(
    val season: String,
    val tiYongRelation: String,
    val tiSeasonState: String,
    val yongSeasonState: String,
    val inter1Relation: String,
    val inter2Relation: String,
    val changedRelation: String,
    val changedTiYongRelation: String
)

data class HexagramDetail(
    val name: String,
    val symbol: String,
    val upper: String,
    val lower: String,
    val description: String,
    val guaciMeaning: String = "",
    val hexagramMeaning: String = ""
)

data class MeihuaYaoDetail(
    val position: Int,
    val yaoType: String,
    val isChanging: Boolean,
    val tiYong: String
)

data class MeihuaCalculation(
    val method: String,
    val methodKey: String,
    val yearZhi: String? = null,
    val yearZhiIndex: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val timeZhi: String? = null,
    val timeZhiIndex: Int? = null,
    val upperTrigramIndex: Int? = null,
    val lowerTrigramIndex: Int? = null,
    val movingYaoIndex: Int? = null,
    val number: Int? = null,
    val numberB: Int? = null,
    val externalSummary: String? = null
)
