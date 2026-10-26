# MOEX Assets Analyzer

A command-line tool that analyzes funds traded on the Moscow Exchange. It downloads price history from the public MOEX ISS API, fits an exponential trend to each fund, and reports yield, maximum drawdown, and how steady the growth is. Results can be printed to the terminal, saved as CSV, or turned into interactive HTML charts.

It helps find accumulating funds (money market funds, floating-rate bond funds, etc.) with high and stable returns among hundreds of ETFs.

## Install

Requires **Java 25** or newer.

1. Download `maa-<version>.zip` from [Releases](https://github.com/MrMaxMen/moex-analyzer/releases).
2. Unzip it.
3. Run `bin\maa.bat` (Windows) or `bin/maa` (Linux, macOS).

Running without arguments prints the help.

Charts (`--visualize`) additionally need **Python 3.11 or newer**. Everything else works without Python. See [Charts](#charts).

## Usage

| Option | Short | Description |
|---|---|---|
| `--tickers` | `-t` | Comma-separated tickers, e.g. `SBMM,AMFL` |
| `--all` | `-a` | Analyze all funds on the exchange |
| `--names` | `-n` | List tickers with full fund names |
| `--since` | `-s` | Period: `MONTH`, `QUARTER`, `HALF_YEAR`, `YEAR` (default) |
| `--csv` | `-c` | Save to a CSV file instead of printing |
| `--visualize` | `-v` | Save results as CSV and build HTML charts (needs Python) |

Pick one mode: `--tickers`, `--all`, or `--names`. They can't be combined, and `--since` doesn't work with `--names`.
Pick one output: terminal (default), `--csv`, or `--visualize`. `--csv` and `--visualize` can't be combined, and `--visualize` doesn't work with `--names`.

```
maa --tickers SBMM,AMFL
maa --all --since HALF_YEAR
maa --all --csv
maa --tickers SBMM,AMFL --visualize
```

## Output

```
Asset        | Daily %    | Monthly %  | CAGR %    | Period %     | Max drawdown | R^2
AMFL         |    0.0456% |    1.3758% |    18.09% |       17.87% |        0.29% | 0.9987
```

- **Daily / Monthly / CAGR / Period** are the growth rates of the fitted trend.
- **Max drawdown** is the largest drop from a previous price peak.
- **R²** shows how close the price chart is to a smooth exponential (1 means perfectly smooth).

Rows are sorted by max drawdown, smallest first.

With `--csv`, a file such as `result_<date>_<time>.csv` is saved in the current folder and opened (`;` separator, UTF-8 with BOM, so Excel opens it correctly). Skipped tickers are saved to `skipped_<date>_<time>.csv`. Besides the columns above, the CSV contains `Slope` and `Intercept`: the parameters of the fitted line, `ln(price) = Intercept + Slope * days`, where `days` counts calendar days from the first date of the period.

## Charts

`--visualize` saves the results and the price history to the current folder and builds an HTML page for every analyzed fund:

- `result_<date>_<time>.csv`: the results table (same as with `--csv`, but not opened)
- `history/<TICKER>.csv`: daily closing prices of each analyzed fund
- `charts/index.html`: a table of all funds with links, opened in the browser when ready
- `charts/<TICKER>.html`: closing prices, the fitted trend line, and the key metrics

Charts are drawn by a small Python script (`visualization/main.py`, included in the zip) with pandas and Plotly. `maa` finds Python (`python`, `py`, or `python3` on PATH) and runs the script for you.

- **Python 3.11 or newer** is required.
- The first run creates a private virtual environment in `~/.maa/venv` (on Windows `%USERPROFILE%\.maa\venv`) and installs the libraries from `visualization/requirements.txt`. This needs internet and takes about a minute. Later runs reuse the environment. Your system Python is not modified. Delete the folder to start over.
- Built charts work offline: Plotly's JavaScript is saved next to them as `charts/plotly.min.js`.
- If Python is missing or the script fails, `maa` says so and the CSV files are still saved.

## How it works

An accumulating fund reinvests its income, so its price grows roughly exponentially. Instead of comparing the first and last price, the tool takes the logarithm of all daily closing prices over the period and fits a straight line by least squares. The slope gives the daily growth rate, which is converted to monthly and yearly rates. R² measures how well the line fits, and the max drawdown is calculated separately from the raw prices.

## Limitations

- Only accumulating funds are supported. Funds that pay out income show payouts as price drops, which distorts the trend.
- Funds with history shorter than the chosen period, or with no data, are skipped (and have no chart).
- Fees, taxes, and spreads are not taken into account.

## Build from source

Requires JDK 25.

```
git clone https://github.com/MrMaxMen/moex-analyzer.git
cd moex-analyzer
./gradlew installDist
```

The program is built into `build/install/maa/`, together with the `visualization/` folder. Run it from there (`build/install/maa/bin/maa`). `--visualize` can't find the chart script when the program is started with `gradle run` or from the IDE.

### Working on the chart script

Install the dependencies into your own environment and tell the script not to create one:

```
pip install -r visualization/requirements.txt
export MAA_NO_VENV=1          # PowerShell: $env:MAA_NO_VENV = "1"
python visualization/main.py --data-dir <folder with result_*.csv and history/>
```

Built with Kotlin, Ktor, kotlinx.serialization, coroutines, and Clikt. Charts: Python, pandas, and Plotly.

## Disclaimer

For educational and informational purposes only. This is not investment advice. Past performance does not guarantee future results.

## Author

Maxim Menshchikov ([MrMaxMen](https://github.com/MrMaxMen))