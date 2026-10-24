package com.example.utils

import com.example.entities.TrendResult

object Display  {

    fun calculationResults(trends: List<TrendResult>) {

        println("\n" + "=".repeat(90))
        println(
            "%-12s | %-10s | %-10s | %-9s | %-12s | %-12s | %-6s".format(
                "Asset",
                "Daily %",
                "Monthly %",
                "CAGR %",
                "Period %",
                "Max drawdown",
                "R^2"
            )
        )
        println("=".repeat(90))

        trends.sortedBy { it.maxDrawdownPercent }.forEach {
            println(
                "%-12s | %9.4f%% | %9.4f%% | %8.2f%% | %11.2f%% | %11.2f%% | %6.4f".format(
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

    fun skipped(skipped : Map<String, String>){
        print("\n" + "skipped results :")
        println("\n" + "=".repeat(50))
        print("%-12s | %-25s ".format("ticker", "skip reason"))
        println("\n" + "=".repeat(50))
        skipped.forEach {
            println(
                "%-12s | %-25s".format(
                    it.key,
                    it.value
                )
            )
        }

    }

    fun fundsFullNames(funds: List<Fund>) {
        println("\n" + "=".repeat(50))
        print("%-12s | %-25s ".format("ticker", "full name"))
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
