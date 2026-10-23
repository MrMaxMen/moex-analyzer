package com.example.config_and_utils

import com.example.Fund
import com.example.entities.HistoryRecord
import java.time.LocalDate
import java.awt.Desktop
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun historyUrlFor(asset: String, ago : LocalDate, start: Int = 0) =
    "http://iss.moex.com/iss/history/engines/stock/markets/shares/securities/$asset.json?from=$ago&start=$start&history.columns=TRADEDATE,CLOSE"


fun hasFullPeriod(records: List<HistoryRecord>, period: LocalDate, toleranceDays: Long = 10): Boolean {
    val first = records.minOfOrNull { it.date } ?: return false
    return !first.isAfter(period.plusDays(toleranceDays))
}



fun List<Fund>.toCSV(): String = buildString {
    appendLine("ticker;full_name")
    this@toCSV.sortedBy { it.ticker }.forEach {
        appendLine("${it.ticker.csvEscape()};${it.fullName.csvEscape()}")
    }
}

private fun String.csvEscape(): String =
    if (any { it == ';' || it == '"' || it == '\n' || it == '\r' }) {
        "\"" + replace("\"", "\"\"") + "\""
    } else this



fun tryOpenFile(file: File) {
    try {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(file)
        } else {
            System.err.println("Can't open the file automatically, open it manually")
        }
    } catch (e: Exception) {
        System.err.println("Can't open the file: ${e.message}")
    }
}

fun saveCsv(prefix: String, content: String,open: Boolean = true): File {
    val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
    val target = File("${prefix}_$stamp.csv")
    target.writeText("\uFEFF" + content, Charsets.UTF_8)
    println("Сохранено: ${target.absolutePath}")
    if (open) tryOpenFile(target)
    return target
}

fun Map<String, String>.toCSV(): String = buildString {
    appendLine("Ticker;Reason")
    this@toCSV.toSortedMap().forEach { (ticker, reason) ->
        appendLine("${ticker.csvEscape()};${reason.csvEscape()}")
    }
}