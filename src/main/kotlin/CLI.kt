package com.example

import com.example.config_and_utils.Period
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.enum
import kotlinx.coroutines.runBlocking

class CLI : CliktCommand(){
    override val printHelpOnEmptyArgs = true
    private val tickers by option("--tickers","-t",help = "use comma to separate tickers").split(",")
    private val all by option("--all","-a",help = "all to analyze all funds of MOEX").flag()
    private val toCSV by option("--csv" ,"-c",help = "store results in csv file").flag()
    private val names by option("--names" , "-n",help = "get all funds full names").flag()
    private val since by option("--since", "-s", help = "Analysis period: MONTH, QUARTER, HALF_YEAR, YEAR (default: YEAR)").enum<Period>()

    override fun run() {
        if (!all && tickers == null && !names)
            throw UsageError("choose mode: --all, --tickers или --names")
        if (names && since != null)
            throw UsageError("--since doesn't work with  --names")
        if (all && tickers != null)
            throw UsageError("Нельзя использовать --all и --tickers вместе")
        if ( names && (all || tickers != null) )
            throw UsageError("Нельзя использовать --all или --tickers вместе с --names")

        val period = since ?: Period.YEAR

        val core = Core()
        try {
            runBlocking {

                if (names) core.getAssetsFullNames(toCSV)

                if (all || tickers != null) core.getAssetsAnalyticsResults(period, toCSV, tickers)

            }
        }finally {
            core.close()
        }
    }

}

