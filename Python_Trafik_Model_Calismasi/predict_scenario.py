from pathlib import Path
from datetime import timedelta

import pandas as pd
import numpy as np
import joblib


# ============================================================
# 1) DOSYA YOLLARI
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

MODEL_PATH = BASE_DIR / "models" / "best_traffic_model.joblib"

DATA_PATH = BASE_DIR / "data" / "processed" / "istanbul_ilce_gunluk_trafik_model_ready.csv"

OUTPUT_DIR = BASE_DIR / "outputs"
OUTPUT_DIR.mkdir(exist_ok=True)

SCENARIO_OUTPUT_PATH = OUTPUT_DIR / "scenario_predictions.csv"


# ============================================================
# 2) MODEL VE GEÇMİŞ VERİYİ OKUMA
# ============================================================

def load_model_package():
    """
    train_model.py tarafından kaydedilen model paketini yükler.
    """

    if not MODEL_PATH.exists():
        raise FileNotFoundError(
            f"Model dosyası bulunamadı:\n{MODEL_PATH}\n\n"
            "Önce modeli eğitmelisin:\n"
            "python train_model.py"
        )

    package = joblib.load(MODEL_PATH)

    print("\n[1] Model yüklendi.")
    print(f"Model adı : {package['model_name']}")
    print(f"Hedef     : {package['target_col']}")

    return package


def load_history_data():
    """
    Gelecek gün tahmini için geçmiş veri gerekir.
    Çünkü model lag_1, lag_7, rolling_7_mean gibi kolonlar kullanıyor.
    """

    if not DATA_PATH.exists():
        raise FileNotFoundError(
            f"Model-ready veri seti bulunamadı:\n{DATA_PATH}"
        )

    df = pd.read_csv(DATA_PATH)

    df["date"] = pd.to_datetime(df["date"], errors="coerce")
    df = df.dropna(subset=["date"]).copy()

    print("\n[2] Geçmiş veri yüklendi.")
    print(f"Satır sayısı : {len(df):,}")
    print(f"İlçe sayısı  : {df['district'].nunique()}")
    print(f"Tarih aralığı: {df['date'].min().date()} - {df['date'].max().date()}")

    return df


# ============================================================
# 3) TARİH FEATURE'LARI
# ============================================================

def create_date_features(date_value):
    """
    Tek bir tarih için modelin beklediği tarih feature'larını üretir.
    """

    date_value = pd.to_datetime(date_value)

    month = date_value.month
    day = date_value.day
    day_of_week = date_value.dayofweek
    is_weekend = 1 if day_of_week in [5, 6] else 0
    week_of_year = int(date_value.isocalendar().week)
    quarter = date_value.quarter

    month_sin = np.sin(2 * np.pi * month / 12)
    month_cos = np.cos(2 * np.pi * month / 12)

    day_of_week_sin = np.sin(2 * np.pi * day_of_week / 7)
    day_of_week_cos = np.cos(2 * np.pi * day_of_week / 7)

    return {
        "month": month,
        "day": day,
        "day_of_week": day_of_week,
        "is_weekend": is_weekend,
        "week_of_year": week_of_year,
        "quarter": quarter,
        "month_sin": month_sin,
        "month_cos": month_cos,
        "day_of_week_sin": day_of_week_sin,
        "day_of_week_cos": day_of_week_cos
    }


def is_turkey_holiday(date_value):
    """
    Türkiye resmi tatil bilgisini üretir.
    holidays paketi yoksa 0 döndürür.
    """

    try:
        import holidays

        year = pd.to_datetime(date_value).year
        tr_holidays = holidays.Turkey(years=[year])

        return 1 if pd.to_datetime(date_value).date() in tr_holidays else 0

    except Exception:
        return 0


# ============================================================
# 4) GEÇMİŞ TRAFİK FEATURE'LARI
# ============================================================

def get_district_history(history_df, district, target_col):
    """
    Seçilen ilçenin geçmiş trafik kayıtlarını döndürür.
    """

    district_df = history_df[
        history_df["district"].astype(str).str.lower().str.strip()
        ==
        district.lower().strip()
    ].copy()

    if district_df.empty:
        available = sorted(history_df["district"].dropna().astype(str).unique().tolist())

        raise RuntimeError(
            f"'{district}' ilçesi eğitim verisinde bulunamadı.\n\n"
            "Mevcut ilçelerden bazıları:\n"
            + "\n".join(f"- {x}" for x in available[:30])
        )

    district_df = district_df.sort_values("date").reset_index(drop=True)

    return district_df


def build_lag_features_from_values(values, global_mean):
    """
    Bir ilçenin geçmiş trafik skorları üzerinden lag ve rolling feature üretir.

    Model şu kolonları bekliyor:
    - lag_1
    - lag_2
    - lag_3
    - lag_7
    - rolling_3_mean
    - rolling_7_mean
    - rolling_3_std
    - rolling_7_std
    """

    values = list(values)

    if len(values) == 0:
        values = [global_mean]

    def lag(n):
        if len(values) >= n:
            return values[-n]
        return global_mean

    last_3 = values[-3:] if len(values) >= 3 else values
    last_7 = values[-7:] if len(values) >= 7 else values

    return {
        "lag_1": lag(1),
        "lag_2": lag(2),
        "lag_3": lag(3),
        "lag_7": lag(7),
        "rolling_3_mean": float(np.mean(last_3)),
        "rolling_7_mean": float(np.mean(last_7)),
        "rolling_3_std": float(np.std(last_3)) if len(last_3) >= 2 else 0,
        "rolling_7_std": float(np.std(last_7)) if len(last_7) >= 2 else 0
    }


# ============================================================
# 5) SENARYO KATSAYISI
# ============================================================

def apply_scenario_adjustment(
    base_prediction,
    road_work,
    closed_lane_count,
    rain_level,
    event_intensity,
    school_day,
    work_time_type,
    accident_risk,
    public_transport_disruption
):
    """
    Modelin baz tahminini gerçek hayat parametrelerine göre düzeltir.

    Önemli:
    Bu parametreler tarihsel veri içinde olmadığı için model tarafından öğrenilmiş değildir.
    Bu yüzden burada uzman kuralı / senaryo katsayısı olarak uygulanır.
    """

    multiplier = 1.0
    reasons = []

    # Yol çalışması
    if road_work:
        multiplier += 0.12
        reasons.append("Yol çalışması: +%12")

    # Kapalı şerit etkisi
    if closed_lane_count > 0:
        lane_effect = min(closed_lane_count * 0.10, 0.35)
        multiplier += lane_effect
        reasons.append(f"{closed_lane_count} kapalı şerit: +%{lane_effect * 100:.0f}")

    # Yağmur seviyesi
    if rain_level == 1:
        multiplier += 0.05
        reasons.append("Hafif yağmur: +%5")
    elif rain_level == 2:
        multiplier += 0.10
        reasons.append("Orta yağmur: +%10")
    elif rain_level >= 3:
        multiplier += 0.18
        reasons.append("Yoğun yağmur: +%18")

    # Etkinlik yoğunluğu
    if event_intensity == 1:
        multiplier += 0.05
        reasons.append("Düşük etkinlik yoğunluğu: +%5")
    elif event_intensity == 2:
        multiplier += 0.12
        reasons.append("Orta etkinlik yoğunluğu: +%12")
    elif event_intensity >= 3:
        multiplier += 0.22
        reasons.append("Yüksek etkinlik yoğunluğu: +%22")

    # Okul günü
    if school_day:
        multiplier += 0.06
        reasons.append("Okul günü: +%6")
    else:
        multiplier -= 0.03
        reasons.append("Okul günü değil: -%3")

    # Çalışma zamanı
    if work_time_type == "night":
        multiplier -= 0.18
        reasons.append("Gece çalışması: -%18")
    elif work_time_type == "day":
        multiplier += 0.05
        reasons.append("Gündüz çalışması: +%5")

    # Kaza riski
    if accident_risk == 1:
        multiplier += 0.05
        reasons.append("Düşük kaza riski: +%5")
    elif accident_risk == 2:
        multiplier += 0.12
        reasons.append("Orta kaza riski: +%12")
    elif accident_risk >= 3:
        multiplier += 0.20
        reasons.append("Yüksek kaza riski: +%20")

    # Toplu taşıma aksaması
    if public_transport_disruption == 1:
        multiplier += 0.06
        reasons.append("Düşük toplu taşıma aksaması: +%6")
    elif public_transport_disruption == 2:
        multiplier += 0.13
        reasons.append("Orta toplu taşıma aksaması: +%13")
    elif public_transport_disruption >= 3:
        multiplier += 0.22
        reasons.append("Yüksek toplu taşıma aksaması: +%22")

    adjusted_prediction = base_prediction * multiplier

    return adjusted_prediction, multiplier, reasons


# ============================================================
# 6) RİSK VE UYGUNLUK SKORU
# ============================================================

def calculate_risk_level(value):
    """
    Trafik skorunu basit risk seviyesine çevirir.

    Bu eşikler veri dağılımına göre daha sonra kalibre edilebilir.
    """

    if value < 90:
        return "Düşük"
    elif value < 115:
        return "Orta"
    elif value < 140:
        return "Yüksek"
    else:
        return "Çok yüksek"


def calculate_work_suitability_score(
    scenario_prediction,
    is_weekend,
    work_time_type,
    road_work,
    closed_lane_count
):
    """
    Yol çalışması için uygunluk skoru hesaplar.

    100'e yakınsa daha uygun.
    0'a yakınsa daha riskli.

    Mantık:
    - Trafik skoru arttıkça uygunluk düşer.
    - Gece çalışması daha uygundur.
    - Hafta sonu biraz daha uygundur.
    - Kapalı şerit sayısı arttıkça uygunluk düşer.
    """

    score = 100.0

    # Trafik skorunun etkisi
    score -= scenario_prediction * 0.35

    # Hafta sonu avantajı
    if is_weekend:
        score += 8

    # Gece çalışması avantajı
    if work_time_type == "night":
        score += 15
    else:
        score -= 5

    # Yol çalışması ve kapalı şerit cezaları
    if road_work:
        score -= 5

    score -= closed_lane_count * 5

    score = max(0, min(100, score))

    return round(score, 2)


# ============================================================
# 7) GELECEK GÜN TAHMİNİ
# ============================================================

def predict_future_days(
    package,
    history_df,
    district,
    start_date,
    days,
    road_work,
    closed_lane_count,
    rain_level,
    event_intensity,
    school_day,
    work_time_type,
    accident_risk,
    public_transport_disruption
):
    """
    Seçilen ilçe için gelecek günleri tahmin eder.

    Recursive yaklaşım:
    - İlk gün için lag değerleri gerçek geçmiş veriden gelir.
    - Sonraki gün için bir önceki tahmin geçmişe eklenir.
    """

    pipeline = package["pipeline"]
    target_col = package["target_col"]
    categorical_features = package["categorical_features"]
    numeric_features = package["numeric_features"]

    model_columns = categorical_features + numeric_features

    start_date = pd.to_datetime(start_date)

    history_df = history_df.copy()
    global_mean = history_df[target_col].mean()

    district_history = get_district_history(
        history_df=history_df,
        district=district,
        target_col=target_col
    )

    # Geleceği tahmin ederken sadece son bilinen tarihe kadar olan değerleri kullanıyoruz.
    district_values = district_history[target_col].dropna().tolist()

    results = []

    for i in range(days):
        current_date = start_date + timedelta(days=i)

        date_features = create_date_features(current_date)
        date_features["is_holiday"] = is_turkey_holiday(current_date)

        lag_features = build_lag_features_from_values(
            values=district_values,
            global_mean=global_mean
        )

        row = {
            "district": district,
            **date_features,
            **lag_features
        }

        input_df = pd.DataFrame([row])

        # Modelin beklediği kolonlar eksikse dolduralım.
        for col in model_columns:
            if col not in input_df.columns:
                input_df[col] = 0

        input_df = input_df[model_columns]

        base_prediction = pipeline.predict(input_df)[0]

        scenario_prediction, multiplier, reasons = apply_scenario_adjustment(
            base_prediction=base_prediction,
            road_work=road_work,
            closed_lane_count=closed_lane_count,
            rain_level=rain_level,
            event_intensity=event_intensity,
            school_day=school_day,
            work_time_type=work_time_type,
            accident_risk=accident_risk,
            public_transport_disruption=public_transport_disruption
        )

        risk_level = calculate_risk_level(scenario_prediction)

        suitability_score = calculate_work_suitability_score(
            scenario_prediction=scenario_prediction,
            is_weekend=date_features["is_weekend"],
            work_time_type=work_time_type,
            road_work=road_work,
            closed_lane_count=closed_lane_count
        )

        results.append({
            "date": current_date.date(),
            "district": district,
            "base_prediction": round(base_prediction, 4),
            "scenario_prediction": round(scenario_prediction, 4),
            "scenario_multiplier": round(multiplier, 4),
            "risk_level": risk_level,
            "work_suitability_score": suitability_score,
            "day_of_week": date_features["day_of_week"],
            "is_weekend": date_features["is_weekend"],
            "is_holiday": date_features["is_holiday"],
            "road_work": road_work,
            "closed_lane_count": closed_lane_count,
            "rain_level": rain_level,
            "event_intensity": event_intensity,
            "school_day": school_day,
            "work_time_type": work_time_type,
            "accident_risk": accident_risk,
            "public_transport_disruption": public_transport_disruption,
            "scenario_reasons": " | ".join(reasons)
        })

        # Recursive tahmin:
        # Sonraki günün lag değerleri için senaryolu tahmini geçmişe ekliyoruz.
        district_values.append(scenario_prediction)

    return pd.DataFrame(results)


# ============================================================
# 8) KULLANICI GİRİŞLERİ
# ============================================================

def ask_user_inputs(history_df):
    """
    Terminal üzerinden kullanıcıdan senaryo bilgilerini alır.
    """

    print("\nMevcut ilçeler:")
    districts = sorted(history_df["district"].dropna().astype(str).unique().tolist())

    for d in districts:
        print("-", d)

    print("\nTahmin parametrelerini gir.")

    district = input("İlçe adı: ").strip()

    start_date = input("Başlangıç tarihi (YYYY-MM-DD): ").strip()

    days = int(input("Kaç gün tahmin yapılsın?: ").strip())

    road_work_text = input("Yol çalışması var mı? (e/h): ").strip().lower()
    road_work = road_work_text == "e"

    closed_lane_count = int(input("Kapalı şerit sayısı (0/1/2/3): ").strip())

    rain_level = int(input("Yağmur seviyesi (0 yok, 1 hafif, 2 orta, 3 yoğun): ").strip())

    event_intensity = int(input("Etkinlik yoğunluğu (0 yok, 1 düşük, 2 orta, 3 yüksek): ").strip())

    school_day_text = input("Okul günü mü? (e/h): ").strip().lower()
    school_day = school_day_text == "e"

    work_time_type = input("Çalışma zamanı (day/night): ").strip().lower()

    if work_time_type not in ["day", "night"]:
        work_time_type = "day"

    accident_risk = int(input("Kaza riski (0 yok, 1 düşük, 2 orta, 3 yüksek): ").strip())

    public_transport_disruption = int(
        input("Toplu taşıma aksaması (0 yok, 1 düşük, 2 orta, 3 yüksek): ").strip()
    )

    return {
        "district": district,
        "start_date": start_date,
        "days": days,
        "road_work": road_work,
        "closed_lane_count": closed_lane_count,
        "rain_level": rain_level,
        "event_intensity": event_intensity,
        "school_day": school_day,
        "work_time_type": work_time_type,
        "accident_risk": accident_risk,
        "public_transport_disruption": public_transport_disruption
    }


# ============================================================
# 9) SONUÇ YAZDIRMA
# ============================================================

def print_results(predictions_df):
    """
    Tahmin sonuçlarını terminale düzgün şekilde yazdırır.
    """

    print("\nTahmin sonuçları:")
    print(
        predictions_df[
            [
                "date",
                "district",
                "base_prediction",
                "scenario_prediction",
                "risk_level",
                "work_suitability_score"
            ]
        ].to_string(index=False)
    )

    sorted_df = predictions_df.sort_values(
        "work_suitability_score",
        ascending=False
    ).reset_index(drop=True)

    best_day = sorted_df.iloc[0]

    print("\nYol çalışması için en uygun gün:")
    print(f"Tarih: {best_day['date']}")
    print(f"İlçe : {best_day['district']}")
    print(f"Normal trafik tahmini   : {best_day['base_prediction']}")
    print(f"Senaryolu trafik tahmini: {best_day['scenario_prediction']}")
    print(f"Risk seviyesi           : {best_day['risk_level']}")
    print(f"Uygunluk skoru          : {best_day['work_suitability_score']} / 100")

    print("\nBu günün senaryo etkileri:")
    print(best_day["scenario_reasons"])


# ============================================================
# 10) ANA AKIŞ
# ============================================================

def main():
    print("Kişiselleştirilebilir Trafik Senaryo Tahmini")
    print("=" * 70)

    package = load_model_package()

    history_df = load_history_data()

    inputs = ask_user_inputs(history_df)

    predictions_df = predict_future_days(
        package=package,
        history_df=history_df,
        district=inputs["district"],
        start_date=inputs["start_date"],
        days=inputs["days"],
        road_work=inputs["road_work"],
        closed_lane_count=inputs["closed_lane_count"],
        rain_level=inputs["rain_level"],
        event_intensity=inputs["event_intensity"],
        school_day=inputs["school_day"],
        work_time_type=inputs["work_time_type"],
        accident_risk=inputs["accident_risk"],
        public_transport_disruption=inputs["public_transport_disruption"]
    )

    predictions_df.to_csv(
        SCENARIO_OUTPUT_PATH,
        index=False,
        encoding="utf-8-sig"
    )

    print_results(predictions_df)

    print("\nSonuç dosyası kaydedildi:")
    print(SCENARIO_OUTPUT_PATH)


if __name__ == "__main__":
    main()