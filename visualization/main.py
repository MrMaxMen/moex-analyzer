from __future__ import annotations

import argparse
import hashlib
import os
import subprocess
import sys
import venv
from pathlib import Path

VENV_DIR = Path.home() / ".maa" / "venv"
STAMP = VENV_DIR / ".requirements.sha256"
REQUIREMENTS = Path(__file__).resolve().with_name("requirements.txt")


def venv_python() -> Path:
    return VENV_DIR / ("Scripts/python.exe" if os.name == "nt" else "bin/python")


def bootstrap() -> None:


    if os.environ.get("MAA_NO_VENV") or Path(sys.prefix).resolve() == VENV_DIR.resolve():
        return
    if not REQUIREMENTS.exists():
        raise SystemExit(f"{REQUIREMENTS} not found")


    py = venv_python()
    current = hashlib.sha256(REQUIREMENTS.read_bytes()).hexdigest()
    up_to_date = py.exists() and STAMP.exists() and STAMP.read_text().strip() == current


    if not up_to_date:
        print("Preparing plotting environment (one-time setup, takes ~1 minute)...")
        try:
            venv.EnvBuilder(with_pip=True).create(VENV_DIR)
            subprocess.check_call([str(py), "-m", "pip", "install",
                                   "--disable-pip-version-check", "-r", str(REQUIREMENTS)])
        except (subprocess.CalledProcessError, OSError) as e:
            raise SystemExit(f"Failed to prepare environment: {e}\n"
                             f"Check internet connection and ensure Python includes the venv module.")
        STAMP.write_text(current)


    code = subprocess.run([str(py), str(Path(__file__).resolve()), *sys.argv[1:]]).returncode
    raise SystemExit(code)


bootstrap()

import numpy as np                  # noqa: E402
import pandas as pd                 # noqa: E402
import plotly.graph_objects as go   # noqa: E402

DATA_DIR = Path(__file__).resolve().parent.parent   # Kotlin project root
HISTORY_DIR = DATA_DIR / "history"
OUT_DIR = DATA_DIR / "charts"

REQUIRED_COLS = ["Asset", "Daily %", "Monthly %", "CAGR %", "Period %",
                 "Max drawdown %", "R^2", "Slope", "Intercept"]


def find_latest_result(data_dir: Path) -> Path:
    files = sorted(data_dir.glob("result_*.csv"))
    if not files:
        raise SystemExit(f"No result_*.csv files found in {data_dir}")
    return files[-1]


def load_results(path: Path) -> pd.DataFrame:
    df = pd.read_csv(path, sep=";", encoding="utf-8-sig", dtype=str)
    missing = [c for c in REQUIRED_COLS if c not in df.columns]
    if missing:
        raise SystemExit(f"Columns missing in {path.name}: {missing}. "
                         f"Re-run maa with --visualize after updating toCSV().")
    for col in REQUIRED_COLS[1:]:
        df[col] = pd.to_numeric(df[col].str.replace(",", ".", regex=False))
    return df


def load_history(data_dir: Path, ticker: str) -> pd.DataFrame | None:
    path = data_dir / "history" / f"{ticker}.csv"
    if not path.exists():
        return None
    df = pd.read_csv(path, sep=";", parse_dates=["date"])
    return df.sort_values("date").reset_index(drop=True)


def build_figure(row: pd.Series, hist: pd.DataFrame) -> go.Figure:
    t = (hist["date"] - hist["date"].min()).dt.days
    trend = np.exp(row["Intercept"] + row["Slope"] * t)
    daily_ret = hist["close"].pct_change().fillna(0) * 100

    fig = go.Figure()
    fig.add_trace(go.Scatter(
        x=hist["date"], y=hist["close"], mode="markers", name="Price",
        marker=dict(size=4), customdata=daily_ret,
        hovertemplate="%{x|%d.%m.%Y}<br>Price: %{y:.4f}<br>Daily: %{customdata:.3f}%<extra></extra>",
    ))
    fig.add_trace(go.Scatter(
        x=hist["date"], y=trend, mode="lines", name="Log trend",
    ))
    fig.add_annotation(
        xref="paper", yref="paper", x=0.01, y=0.99, showarrow=False, align="left",
        text=(f"CAGR: {row['CAGR %']:.2f}%<br>"
              f"Monthly: {row['Monthly %']:.2f}%<br>"
              f"Max drawdown: {row['Max drawdown %']:.2f}%<br>"
              f"R²: {row['R^2']:.4f}"),
    )
    fig.update_layout(title=row["Asset"], yaxis_title="Price, ₽", hovermode="x unified")
    return fig


def write_index(rows: list[pd.Series],out_dir: Path) -> None:
    lines = ["<!doctype html><meta charset='utf-8'><title>MOEX Assets Analyzer</title>",
             "<table border='1' cellpadding='6' style='border-collapse:collapse'>",
             "<tr><th>Asset</th><th>CAGR %</th><th>Max drawdown %</th><th>R²</th></tr>"]
    for r in rows:
        lines.append(f"<tr><td><a href='{r['Asset']}.html'>{r['Asset']}</a></td>"
                     f"<td>{r['CAGR %']:.2f}</td><td>{r['Max drawdown %']:.2f}</td>"
                     f"<td>{r['R^2']:.4f}</td></tr>")
    lines.append("</table>")
    (out_dir / "index.html").write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="Charts for MOEX Assets Analyzer results")
    parser.add_argument("--data-dir", type=Path, default=Path.cwd(),
                        help="Directory with result_*.csv and history/ (default: current directory)")
    parser.add_argument("--out-dir", type=Path, default=None,
                        help="Where to save HTML charts (default: <data-dir>/charts)")
    args = parser.parse_args()

    data_dir = args.data_dir.resolve()
    out_dir = (args.out_dir or data_dir / "charts").resolve()

    result_path = find_latest_result(data_dir)
    results = load_results(result_path)
    out_dir.mkdir(exist_ok=True)

    done = []
    for _, row in results.iterrows():
        ticker = row["Asset"]
        hist = load_history(data_dir, ticker)
        if hist is None:
            print(f"[skip] {ticker}: missing history/{ticker}.csv")
            continue
        fig = build_figure(row, hist)
        fig.write_html(out_dir / f"{ticker}.html", include_plotlyjs="directory")
        done.append(row)
        print(f"[ok] {ticker}")

    write_index(done, out_dir)
    print(f"Done: {len(done)} charts saved to {out_dir}")


if __name__ == "__main__":
    main()