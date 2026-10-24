package com.example.utils

import com.example.entities.HistoryRecord
import java.time.LocalDate
import java.awt.Desktop
import java.io.File


fun historyUrlFor(asset: String, ago : LocalDate, start: Int = 0) =
    "http://iss.moex.com/iss/history/engines/stock/markets/shares/securities/$asset.json?from=$ago&start=$start&history.columns=TRADEDATE,CLOSE"


fun hasFullPeriod(records: List<HistoryRecord>, period: LocalDate, toleranceDays: Long = 10): Boolean {
    val first = records.minOfOrNull { it.date } ?: return false
    return !first.isAfter(period.plusDays(toleranceDays))
}




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
