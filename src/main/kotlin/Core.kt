package com.example

import com.example.config_and_utils.Period
import com.example.config_and_utils.configureHttpClient
import com.example.entities.HistoryRecord
import com.example.entities.TrendResult
import com.example.utils.*
import io.ktor.client.*
import java.io.File


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
        if (toCSV) saveCsv("asset_names", assets.listFundsToCSV())
        else Display.fundsFullNames(assets)
    }

    suspend fun getAssetsAnalyticsResults(since: Period, toCSV: Boolean,visualize : Boolean, tickers: List<String>?) {
        val skipped = mutableMapOf<String, String>()

        val history = mutableMapOf<String, List<HistoryRecord>>()

        val assetsHistoryFetchResults = if (tickers == null) {
            dataService.fetchAssetsNames()
            dataService.fetchAssetsHistory(onProgress = ProgressBar::draw, since = since.since)
        } else {
            dataService.fetchAssetsHistory(assets = tickers, onProgress = ProgressBar::draw,since = since.since)
        }


        for ((ticker, result) in assetsHistoryFetchResults) {
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
            saveCsv("result", res.results.listTrendResultToCSV())
            if (res.skipped.isNotEmpty()) {
                saveCsv("skipped", res.skipped.toCSV(), open = false)
                println("Skipped tickers: ${res.skipped.size}")
            }
        } else if(visualize){
            history.forEach { (ticker, records) -> saveAssetHistoryToCSV(ticker, records) }
            saveCsv("result", res.results.listTrendResultToCSV(), open = false)
            if (res.skipped.isNotEmpty()) {
                saveCsv("skipped", res.skipped.toCSV(), open = false)
                println("Skipped tickers: ${res.skipped.size}")
            }
            val dataDir = File("").absoluteFile
            if (runVisualization(dataDir)) {
                tryOpenFile(File(dataDir, "charts/index.html"))
            }

        }else {
            Display.calculationResults(res.results)
            if (res.skipped.isNotEmpty()) Display.skipped(res.skipped)
        }

    }

}

