package com.example

import com.example.entities.HistoryRecord
import com.example.entities.TrendResult
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow


    fun calculateTrend(asset : String ,records: List<HistoryRecord>): TrendResult {
        if (records.size < 2) return TrendResult(asset, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        val sortedRecords = records.sortedBy { it.date }
        val startDate = sortedRecords.first().date

        // t - количество календарных дней от даты первого рекорда
        val tValues = sortedRecords.map { ChronoUnit.DAYS.between(startDate, it.date).toDouble() }
        val yValues = sortedRecords.map { ln(it.close) }

        val tBar = tValues.average()
        val yBar = yValues.average()

        var up = 0.0
        var down = 0.0

        for (i in sortedRecords.indices) {
            val dt = tValues[i] - tBar
            val dy = yValues[i] - yBar
            up += dt * dy
            down += dt * dt
        }

        if (down == 0.0) return TrendResult(asset, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        val b = up / down // Дневная экспоненциальная ставка роста
        val totalDays = tValues.last()

        // Расчет R^2 (точности аппроксимации тренда)
        val a = yBar - b * tBar
        var ssTot = 0.0
        var ssRes = 0.0
        for (i in sortedRecords.indices) {
            val yPred = a + b * tValues[i]
            ssRes += (yValues[i] - yPred).pow(2)
            ssTot += (yValues[i] - yBar).pow(2)
        }
        val rSquared = if (ssTot != 0.0) 1.0 - (ssRes / ssTot) else 1.0

        val dailyRate = (exp(b) - 1.0) * 100.0
        val monthlyRate = (exp(b * 30) - 1.0) * 100.0
        val annualRate = (exp(b * 365.0) - 1.0) * 100.0
        val totalPeriodReturn = (exp(b * totalDays) - 1.0) * 100.0

        var peak = 0.0
        var worst = 0.0
        for ((_, close) in records.sortedBy { it.date }) {
            if (close > peak) peak = close
            val drop = (peak - close) / peak * 100.0
            if (drop > worst) worst = drop
        }


        return TrendResult(
            asset = asset,
            dailyRatePercent = dailyRate,
            monthlyRatePercent = monthlyRate,
            annualRatePercent = annualRate,
            totalPeriodReturnPercent = totalPeriodReturn,
            maxDrawdownPercent = worst,
            rSquared = rSquared
        )
    }


