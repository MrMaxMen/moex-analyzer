package com.example.utils

import com.example.entities.HistoryRecord
import com.example.entities.TrendResult
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


fun saveCsv(prefix: String, content: String,open: Boolean = true): File {
    val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
    val target = File("${prefix}_$stamp.csv")
    target.writeText("\uFEFF" + content, Charsets.UTF_8)
    println("saved: ${target.absolutePath}")
    if (open) tryOpenFile(target)
    return target
}

fun Map<String, String>.toCSV(): String = buildString {
    appendLine("Ticker;Reason")
    this@toCSV.toSortedMap().forEach { (ticker, reason) ->
        appendLine("${ticker.csvEscape()};${reason.csvEscape()}")
    }
}


fun List<Fund>.listFundsToCSV(): String = buildString {
    appendLine("ticker;full_name")
    this@listFundsToCSV.sortedBy { it.ticker }.forEach {
        appendLine("${it.ticker.csvEscape()};${it.fullName.csvEscape()}")
    }
}

private fun String.csvEscape(): String =
    if (any { it == ';' || it == '"' || it == '\n' || it == '\r' }) {
        "\"" + replace("\"", "\"\"") + "\""
    } else this


fun saveAssetHistoryToCSV(
    ticker: String,
    records: List<HistoryRecord>,
    dir: File = File("history")
): File {
    dir.mkdirs()
    val target = File(dir, "$ticker.csv")
    val content = buildString {
        appendLine("ticker;date;close")
        records.sortedBy { it.date }.forEach {
            appendLine("${ticker.csvEscape()};${it.date};${it.close}")
        }
    }
    target.writeText(content, Charsets.UTF_8)
    return target
}

fun List<TrendResult>.listTrendResultToCSV(): String = buildString {
    appendLine("Asset;Daily %;Monthly %;CAGR %;Period %;Max drawdown %;R^2;Slope;Intercept")
    this@listTrendResultToCSV.sortedBy { it.maxDrawdownPercent }.forEach {
        appendLine(
            "%s;%.4f;%.4f;%.4f;%.4f;%.4f;%.4f;%.10f;%.10f".format(
                Locale.getDefault(), it.asset, it.dailyRatePercent, it.monthlyRatePercent,
                it.annualRatePercent, it.totalPeriodReturnPercent, it.maxDrawdownPercent,
                it.rSquared, it.slope, it.intercept
            )
        )
    }
}