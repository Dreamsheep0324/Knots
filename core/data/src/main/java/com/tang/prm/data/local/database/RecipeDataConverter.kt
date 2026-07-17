package com.tang.prm.data.local.database

import androidx.room.TypeConverter
import com.tang.prm.domain.model.CookingStep
import com.tang.prm.domain.model.Ingredient
import com.tang.prm.domain.model.IngredientGroupType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RecipeDataConverter {
    @TypeConverter
    fun fromIngredientList(list: List<Ingredient>): String {
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(
                JSONObject().apply {
                    put("name", item.name)
                    put("amount", item.amount)
                    put("unit", item.unit)
                    put("groupType", item.groupType.name)
                    put("isScalable", item.isScalable)
                }
            )
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toIngredientList(value: String?): List<Ingredient> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(value)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Ingredient(
                    name = obj.optString("name"),
                    amount = obj.optString("amount"),
                    unit = obj.optString("unit"),
                    groupType = IngredientGroupType.entries
                        .find { it.name == obj.optString("groupType") }
                        ?: IngredientGroupType.MAIN,
                    isScalable = obj.optBoolean("isScalable", true)
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStepList(list: List<CookingStep>): String {
        val jsonArray = JSONArray()
        for (step in list) {
            jsonArray.put(
                JSONObject().apply {
                    put("order", step.order)
                    put("description", step.description)
                    step.image?.let { put("image", it) }
                    step.timerSeconds?.let { put("timerSeconds", it) }
                }
            )
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStepList(value: String?): List<CookingStep> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(value)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                CookingStep(
                    order = obj.optInt("order", i + 1),
                    description = obj.optString("description"),
                    image = obj.optString("image").ifBlank { null },
                    timerSeconds = if (obj.has("timerSeconds")) obj.getInt("timerSeconds") else null
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }
}
