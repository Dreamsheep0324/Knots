package com.tang.prm.engine.divination.model

import kotlinx.serialization.Serializable

@Serializable
data class LiuyaoData(
    val originalName: String,
    val changedName: String,
    val interName: String,
    val ganzhi: GanZhiInfo,
    val timestamp: Long,
    val yaoArray: List<Int>,
    val changingYaos: List<ChangingYao>,
    val sixGods: List<String>,
    val sixRelatives: List<String>,
    val najiaDizhi: List<String>,
    val wuxing: List<String>,
    val worldAndResponse: List<String>,
    val voidBranches: List<String>,
    val palace: PalaceInfo,
    val yaosDetail: List<LiuyaoYaoDetail>,
    val specialPattern: String?,
    val specialAdvice: String?,
    val isChaotic: Boolean,
    val chaoticReason: String?
)

@Serializable
data class ChangingYao(
    val position: Int,
    val isChanging: Boolean,
    val type: String
)

@Serializable
data class PalaceInfo(
    val name: String,
    val wuxing: String
)

@Serializable
data class LiuyaoYaoDetail(
    val position: Int,
    val rawValue: Int,
    val yaoType: String,
    val isChanging: Boolean,
    val changeType: String,
    val sixGod: String,
    val sixRelative: String,
    val najiaDizhi: String,
    val wuxing: String,
    val isWorld: Boolean,
    val isResponse: Boolean,
    val isVoid: Boolean,
    val changedYao: ChangedYaoInfo?
)

@Serializable
data class ChangedYaoInfo(
    val dizhi: String,
    val wuxing: String,
    val liuqin: String,
    val isVoid: Boolean
)
