from pathlib import Path
import pandas as pd
import numpy as np


BASE_DIR = Path(__file__).resolve().parent

INPUT_PATH = BASE_DIR / "data" / "processed" / "istanbul_ilce_gunluk_trafik_2024_clean.csv"

OUTPUT_PATH = BASE_DIR / "data" / "processed" / "istanbul_ilce_gunluk_trafik_model_ready.csv"


def main():
    print("Model-ready veri seti hızlı hazırlanıyor...")

    if not INPUT_PATH.exists():
        raise FileNotFoundError(
            f"Giriş dosyası bulunamadı:\n{INPUT_PATH}\n\n"
            "Önce clean_district_names.py çalışmış olmalı."
        )

    df = pd.read_csv(INPUT_PATH)

    print("Okunan veri:")
    print(f"Satır sayısı: {len(df):,}")
    print(f"Sütunlar: {list(df.columns)}")

    target_col = "avg_congestion_score"

    if target_col not in df.columns:
        raise RuntimeError(f"{target_col} kolonu bulunamadı.")

    if "date" not in df.columns:
        raise RuntimeError("date kolonu bulunamadı.")

    if "district" not in df.columns:
        raise RuntimeError("district kolonu bulunamadı.")

    df["date"] = pd.to_datetime(df["date"], errors="coerce")
    df[target_col] = pd.to_numeric(df[target_col], errors="coerce")

    df = df.dropna(subset=["date", "district", target_col]).copy()

    # Tarih özellikleri
    df["year"] = df["date"].dt.year
    df["month"] = df["date"].dt.month
    df["day"] = df["date"].dt.day
    df["day_of_week"] = df["date"].dt.dayofweek
    df["is_weekend"] = df["day_of_week"].isin([5, 6]).astype(int)
    df["week_of_year"] = df["date"].dt.isocalendar().week.astype(int)
    df["quarter"] = df["date"].dt.quarter

    # Döngüsel tarih özellikleri
    df["month_sin"] = np.sin(2 * np.pi * df["month"] / 12)
    df["month_cos"] = np.cos(2 * np.pi * df["month"] / 12)

    df["day_of_week_sin"] = np.sin(2 * np.pi * df["day_of_week"] / 7)
    df["day_of_week_cos"] = np.cos(2 * np.pi * df["day_of_week"] / 7)

    # Resmi tatil bilgisi
    try:
        import holidays

        years = sorted(df["date"].dt.year.unique().tolist())
        tr_holidays = holidays.Turkey(years=years)

        df["is_holiday"] = df["date"].dt.date.apply(
            lambda d: 1 if d in tr_holidays else 0
        )

        print("Resmi tatil bilgisi eklendi.")

    except Exception as e:
        print("holidays paketi yok veya çalışmadı. is_holiday = 0 atanıyor.")
        print("Detay:", e)
        df["is_holiday"] = 0

    # Veri sızıntısı oluşturabilecek kolonları çıkar
    leakage_cols = [
        "max_traffic_value",
        "min_traffic_value",
        "std_traffic_value",
        "record_count",
        "AVERAGE_SPEED_mean",
        "AVERAGE_SPEED_max",
        "AVERAGE_SPEED_min",
        "NUMBER_OF_VEHICLES_sum",
        "NUMBER_OF_VEHICLES_mean"
    ]

    existing_leakage_cols = [
        col for col in leakage_cols
        if col in df.columns
    ]

    if existing_leakage_cols:
        print("Çıkarılan sızıntı kolonları:")
        for col in existing_leakage_cols:
            print("-", col)

        df = df.drop(columns=existing_leakage_cols)

    # İlçe bazlı lag feature üretimi
    df = df.sort_values(["district", "date"]).reset_index(drop=True)

    group = df.groupby("district", group_keys=False)

    df["lag_1"] = group[target_col].shift(1)
    df["lag_2"] = group[target_col].shift(2)
    df["lag_3"] = group[target_col].shift(3)
    df["lag_7"] = group[target_col].shift(7)

    df["rolling_3_mean"] = group[target_col].apply(
        lambda s: s.shift(1).rolling(window=3, min_periods=1).mean()
    )

    df["rolling_7_mean"] = group[target_col].apply(
        lambda s: s.shift(1).rolling(window=7, min_periods=1).mean()
    )

    df["rolling_3_std"] = group[target_col].apply(
        lambda s: s.shift(1).rolling(window=3, min_periods=2).std()
    )

    df["rolling_7_std"] = group[target_col].apply(
        lambda s: s.shift(1).rolling(window=7, min_periods=2).std()
    )

    # Eksikleri doldur
    district_mean = group[target_col].transform("mean")
    global_mean = df[target_col].mean()

    lag_cols = [
        "lag_1",
        "lag_2",
        "lag_3",
        "lag_7",
        "rolling_3_mean",
        "rolling_7_mean",
        "rolling_3_std",
        "rolling_7_std"
    ]

    for col in lag_cols:
        df[col] = df[col].fillna(district_mean)
        df[col] = df[col].fillna(global_mean)
        df[col] = df[col].fillna(0)

    # Tek değerli kolonları çıkar
    single_value_cols = []

    for col in df.columns:
        if col in ["date", "district", target_col]:
            continue

        if df[col].nunique(dropna=True) <= 1:
            single_value_cols.append(col)

    if single_value_cols:
        print("Tek değerli olduğu için çıkarılan kolonlar:")
        for col in single_value_cols:
            print("-", col)

        df = df.drop(columns=single_value_cols)

    df = df.sort_values(["date", "district"]).reset_index(drop=True)

    df.to_csv(
        OUTPUT_PATH,
        index=False,
        encoding="utf-8-sig"
    )

    print("\nModel-ready veri seti kaydedildi:")
    print(OUTPUT_PATH)

    print("\nSon veri özeti:")
    print(f"Satır sayısı: {len(df):,}")
    print(f"Sütun sayısı: {df.shape[1]}")
    print("Sütunlar:")
    for col in df.columns:
        print("-", col)


if __name__ == "__main__":
    main()