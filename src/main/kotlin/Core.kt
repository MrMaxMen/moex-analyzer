package com.example

import com.example.config_and_utils.*
import com.example.entities.HistoryRecord
import com.example.entities.TrendResult
import com.example.entities.toCSV
import io.ktor.client.*


data class AnalysisReport(
    val results: List<TrendResult>,
    val skipped: Map<String, String>
)
class Core (
    private val client : HttpClient = configureHttpClient()
) {
    val dataService = DataService(client)
    fun close() = client.close()

    suspend fun getAssetsFullNames(toCSV: Boolean) {
        val assets = dataService.getFundsFullNames()
        if (toCSV) saveCsv("asset_names", assets.toCSV())
        else Display.fundsFullNames(assets)
    }

    suspend fun getAssetsAnalyticsResults(since: Period, toCSV: Boolean, tickers: List<String>?) {
        val skipped = mutableMapOf<String, String>()

        val history = mutableMapOf<String, List<HistoryRecord>>()

        val fetchAssetsHistoryResults = if (tickers == null) {
            dataService.fetchAssetsNames()
            dataService.fetchAssetsHistory(onProgress = ProgressBar::draw, since = since.since)
        } else {
            dataService.fetchAssetsHistory(assets = tickers, onProgress = ProgressBar::draw,since = since.since)
        }


        for ((ticker, result) in fetchAssetsHistoryResults) {
            val records = result.getOrNull()
            when {
                records == null -> skipped[ticker] = "request failed: ${result.exceptionOrNull()?.message}"
                records.isEmpty() -> skipped[ticker] = "no data"
                !hasFullPeriod(records, since.since) -> skipped[ticker] = "history shorter than period"
                else -> history[ticker] = records
            }
        }


        val results = history
            .map { (ticker, records) -> calculateTrend(ticker, records) }

        val res = AnalysisReport(results, skipped)
        if (toCSV) {
            saveCsv("result", res.results.toCSV())
            if (res.skipped.isNotEmpty()) {
                saveCsv("skipped", res.skipped.toCSV(), open = false)
                println("Skipped tickers: ${res.skipped.size}")
            }
        } else {
            Display.calculationResults(res.results)
            if (res.skipped.isNotEmpty()) Display.skipped(res.skipped)
        }

    }
}

