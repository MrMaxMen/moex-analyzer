package com.example.config_and_utils

import com.example.Fund
import com.example.entities.TrendResult

object Display  {

    fun calculationResults(trends: List<TrendResult>) {

        println("\n" + "=".repeat(104))
        println(
            "%-12s | %-10s | %-10s | %-14s | %-12s | %-15s | %-6s".format(
                "Asset",
                "Дневной %",
                "Месячный %",
                "Годовых (CAGR)",
                "За период %",
                "Максимальная просадка",
                "R^2"
            )
        )
        println("=".repeat(104))

        trends.sortedBy { it.maxDrawdownPercent }.forEach {
            println(
                "%-12s | %9.4f%% | %9.4f%% | %13.2f%% | %11.2f%% | %20.2f%% | %6.4f".format(
                    it.asset,
                    it.dailyRatePercent,
                    it.monthlyRatePercent,
                    it.annualRatePercent,
                    it.totalPeriodReturnPercent,
                    it.maxDrawdownPercent,
                    it.rSquared
                )
            )
        }
    }

    fun fundsFullNames(funds: List<Fund>) {
        println("\n" + "=".repeat(50))
        println("%-12s | %-25s ".format("ticker", "full name"))
        println("\n" + "=".repeat(50))
        funds.forEach {
            println(
                "%-12s | %-25s".format(
                    it.ticker,
                    it.fullName
                )
            )
        }
    }
}
