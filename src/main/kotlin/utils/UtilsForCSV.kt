package com.example.utils

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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

