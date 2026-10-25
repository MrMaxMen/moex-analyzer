package com.example.entities

import java.util.Locale

data class TrendResult(
    val asset : String,
    val dailyRatePercent: Double,         // Дневной темп роста (%)
    val monthlyRatePercent : Double,      // Месячная доходность (%)
    val annualRatePercent: Double,        // Годовая доходность (% годовых, CAGR)
    val totalPeriodReturnPercent: Double, // Рост тренда за весь период (%)
    val maxDrawdownPercent : Double,
    val rSquared: Double,                  // Коэффициент детерминации R^2
    val slope: Double = 0.0,      // b: рост ln(цены) за календарный день
    val intercept: Double = 0.0   // a: ln(цены) в t = 0 (первая дата ряда)

)


