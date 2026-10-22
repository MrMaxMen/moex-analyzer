# MOEX Assets Analyzer

A command-line tool that analyzes funds traded on the Moscow Exchange. It downloads price history from the public MOEX ISS API, fits an exponential trend to each fund, and reports yield, maximum drawdown, and how steady the growth is. Results can be printed to the terminal or saved as CSV.

It helps find accumulating funds (money market funds, floating-rate bond funds, etc.) with high and stable returns among hundreds of ETFs.

## Install

Requires **Java 25** or newer.

1. Download `maa-<version>.zip` from [Releases](https://github.com/MrMaxMen/moex-analyzer/releases).
2. Unzip it.
3. Run `bin\maa.bat` (Windows) or `bin/maa` (Linux, macOS).

Running without arguments prints the help.

## Usage

| Option | Short | Description |
|---|---|---|
| `--tickers` | `-t` | Comma-separated tickers, e.g. `SBMM,AMFL` |
| `--all` | `-a` | Analyze all funds on the exchange |
| `--names` | `-n` | List tickers with full fund names |
| `--since` | `-s` | Period: `MONTH`, `QUARTER`, `HALF_YEAR`, `YEAR` (default) |
| `--csv` | `-c` | Save to a CSV file instead of printing |

Pick one mode: `--tickers`, `--all`, or `--names`. They can't be combined, and `--since` doesn't work with `--names`.

```
maa --tickers SBMM,AMFL
maa --all --since HALF_YEAR
maa --all --csv
```

## Output

```
Asset        | Daily %    | Monthly %  | CAGR           | Period %     | Max drawdown          | R^2
AMFL         |    0.0456% |    1.3758% |         18.09% |       17.87% |                 0.29% | 0.9987
```

- **Daily / Monthly / CAGR / Period** are the growth rates of the fitted trend.
- **Max drawdown** is the largest drop from a previous price peak.
- **R²** shows how close the price chart is to a smooth exponential (1 means perfectly smooth).

Rows are sorted by max drawdown, smallest first. With `--csv`, a file such as `result_<date>_<time>.csv` is saved in the current folder (`;` separator, UTF-8 with BOM, so Excel opens it correctly).

## How it works

An accumulating fund reinvests its income, so its price grows roughly exponentially. Instead of comparing the first and last price, the tool takes the logarithm of all daily closing prices over the period and fits a straight line by least squares. The slope gives the daily growth rate, which is converted to monthly and yearly rates. R² measures how well the line fits, and the max drawdown is calculated separately from the raw prices.

## Limitations

- Only accumulating funds are supported. Funds that pay out income show payouts as price drops, which distorts the trend.
- Funds with history shorter than the chosen period, or with no data, are skipped.
- Fees, taxes, and spreads are not taken into account.

## Build from source

Requires JDK 25.

```
git clone https://github.com/MrMaxMen/moex-analyzer.git
cd moex-analyzer
./gradlew installDist
```

The program is built into `build/install/maa/`.

Built with Kotlin, Ktor, kotlinx.serialization, coroutines, and Clikt.

## Disclaimer

For educational and informational purposes only. This is not investment advice. Past performance does not guarantee future results.

## Author

Maxim Menshchikov ([MrMaxMen](https://github.com/MrMaxMen))
