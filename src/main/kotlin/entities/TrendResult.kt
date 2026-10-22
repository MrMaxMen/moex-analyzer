package com.example.entities

import java.util.Locale

data class TrendResult(
    val asset : String,
    val dailyRatePercent: Double,         // Дневной темп роста (%)
    val monthlyRatePercent : Double,      // Месячная доходность (%)
    val annualRatePercent: Double,        // Годовая доходность (% годовых, CAGR)
    val totalPeriodReturnPercent: Double, // Рост тренда за весь период (%)
    val maxDrawdownPercent : Double,
    val rSquared: Double                  // Коэффициент детерминации R^2
)


fun List<TrendResult>.toCSV(): String = buildString {
    appendLine("asset;daily_pct;monthly_pct;cagr_pct;period_pct;max_drawdown_pct;r_squared")
    this@toCSV.sortedBy { it.maxDrawdownPercent }.forEach {
        appendLine(
            "%s;%.4f;%.4f;%.4f;%.4f;%.4f;%.4f".format(
                Locale.ROOT, it.asset, it.dailyRatePercent, it.monthlyRatePercent,
                it.annualRatePercent, it.totalPeriodReturnPercent, it.maxDrawdownPercent, it.rSquared
            )
        )
    }
}