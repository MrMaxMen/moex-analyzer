package com.example


import com.example.entities.HistoryRecord
import kotlinx.serialization.json.*
import java.time.LocalDate
import kotlin.collections.mapNotNull

fun parseHistory(jsonString: String): List<HistoryRecord> {
    val root = Json.parseToJsonElement(jsonString)

    val dataArray = root.jsonObject["history"]
        ?.jsonObject
        ?.get("data")
        ?.jsonArray ?: return emptyList()

    return dataArray.mapNotNull { element ->
        val row = element.jsonArray
        val dateStr = row.getOrNull(0)?.jsonPrimitive?.contentOrNull
        val closeVal = row.getOrNull(1)?.jsonPrimitive?.doubleOrNull

        if (dateStr != null && closeVal != null) {

            HistoryRecord(
                date = LocalDate.parse(dateStr),
                close = closeVal
            )

        } else {
            null
        }
    }
}


fun parseTickers(jsonString: String) : List<String>{
    val data = Json.parseToJsonElement(jsonString) .jsonObject["securities"]!! .jsonObject["data"]!! .jsonArray
    return data.mapNotNull { row -> val ticker = row.jsonArray.getOrNull(0)?.jsonPrimitive?.content
        val name = row.jsonArray.getOrNull(1)?.jsonPrimitive?.content
        val fullName = row.jsonArray.getOrNull(2)?.jsonPrimitive?.content
        if (name?.contains("ETF", ignoreCase = true) == true || name?.contains("БПИФ", ignoreCase = true) == true || name?.contains("ПИФ", ignoreCase = true) == true || fullName?.contains("БПИФ", ignoreCase = true) == true || fullName?.contains("ПИФ", ignoreCase = true) == true ) ticker else null }.distinct()
}


data class Fund(val ticker: String, val fullName: String)

fun parseFunds(jsonString: String): List<Fund> {
    val data = Json.parseToJsonElement(jsonString).jsonObject["securities"]!!.jsonObject["data"]!!.jsonArray
    return data.mapNotNull { row ->
        val ticker = row.jsonArray.getOrNull(0)?.jsonPrimitive?.content
        val name = row.jsonArray.getOrNull(1)?.jsonPrimitive?.content
        val fullName = row.jsonArray.getOrNull(2)?.jsonPrimitive?.content
        if (name?.contains("ETF", ignoreCase = true) == true || name?.contains("БПИФ", ignoreCase = true) == true || name?.contains("ПИФ", ignoreCase = true) == true || fullName?.contains("БПИФ", ignoreCase = true) == true || fullName?.contains("ПИФ", ignoreCase = true) == true) {
            if (ticker != null && fullName != null) Fund(ticker, fullName) else null
        } else null
    }.distinctBy { it.ticker }
}