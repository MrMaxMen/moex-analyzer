package com.example

import com.example.config_and_utils.historyUrlFor
import com.example.entities.HistoryRecord
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.time.LocalDate


suspend fun getAssetsList(client: HttpClient) =
    client.get("https://iss.moex.com/iss/engines/stock/markets/shares/securities.json?iss.meta=off&iss.only=securities&securities.columns=SECID%2CSHORTNAME%2CSECNAME")
        .bodyAsText(Charsets.UTF_8)


suspend fun getAssetHistoryFor(client: HttpClient, asset: String,since : LocalDate) : List<HistoryRecord> {
    val history = mutableListOf<HistoryRecord>()
    while (true) {
        val page = parseHistory(client.get(historyUrlFor(asset, since, start = history.size)).bodyAsText())
        history += page
        if (page.size != 100) break
    }
    return history
}