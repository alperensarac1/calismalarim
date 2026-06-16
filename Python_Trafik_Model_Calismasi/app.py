from pathlib import Path
from datetime import timedelta

import pandas as pd
import numpy as np
import joblib
import streamlit as st
import matplotlib.pyplot as plt


# ============================================================
# 1) SAYFA AYARLARI
# ============================================================

st.set_page_config(
    page_title="İlçe Bazlı Trafik Tahmin Sistemi",
    page_icon="🚦",
    layout="wide"
)


# ============================================================
# 2) DOSYA YOLLARI
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

MODEL_PATH = BASE_DIR / "models" / "best_traffic_model.joblib"

DATA_PATH = BASE_DIR / "data" / "processed" / "istanbul_ilce_gunluk_trafik_model_ready.csv"


# ============================================================
# 3) YARDIMCI FONKSİYONLAR
# ============================================================

@st.cache_resource
def load_model_package():
    """
    Eğitilmiş modeli yükler.

    st.cache_resource:
    Model her etkileşimde tekrar yüklenmesin diye kullanıyoruz.
    """

    if not MODEL_PATH.exists():
        st.error(f"Model dosyası bulunamadı:\n{MODEL_PATH}")
        st.stop()

    package = joblib.load(MODEL_PATH)

    return package


@st.cache_data
def load_history_data():
    """
    Model-ready geçmiş veri setini yükler.

    Bu veri lag_1, rolling_7_mean gibi geçmiş trafik bilgileri için kullanılır.
    """

    if not DATA_PATH.exists():
        st.error(f"Veri dosyası bulunamadı:\n{DATA_PATH}")
        st.stop()

    df = pd.read_csv(DATA_PATH)

    df["date"] = pd.to_datetime(df["date"], errors="coerce")
    df = df.dropna(subset=["date"]).copy()

    return df


def create_date_features(date_value):
    """
    Bir tarih için modelin beklediği tarihsel özellikleri üretir.
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
    Türkiye resmi tatil bilgisini döndürür.
    holidays paketi yoksa 0 döner.
    """

    try:
        import holidays

        date_value = pd.to_datetime(date_value)
        tr_holidays = holidays.Turkey(years=[date_value.year])

        return 1 if date_value.date() in tr_holidays else 0

    except Exception:
        return 0


def get_district_history(history_df, district, target_col):
    """
    Seçilen ilçenin geçmiş trafik kayıtlarını getirir.
    """

    district_df = history_df[
        history_df["district"].astype(str).str.lower().str.strip()
        ==
        district.lower().strip()
    ].copy()

    if district_df.empty:
        raise RuntimeError(f"{district} ilçesi veri setinde bulunamadı.")

    district_df = district_df.sort_values("date").reset_index(drop=True)

    return district_df


def build_lag_features_from_values(values, global_mean):
    """
    Geçmiş trafik değerlerinden lag ve rolling feature üretir.
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
    Model tahminini kullanıcı senaryosuna göre düzeltir.

    Bu katman makine öğrenmesi modeli değildir.
    Kullanıcı parametreleri geçmiş veride olmadığı için uzman kuralı gibi uygulanır.
    """

    multiplier = 1.0
    reasons = []

    if road_work:
        multiplier += 0.12
        reasons.append("Yol çalışması +%12")

    if closed_lane_count > 0:
        lane_effect = min(closed_lane_count * 0.10, 0.35)
        multiplier += lane_effect
        reasons.append(f"{closed_lane_count} kapalı şerit +%{lane_effect * 100:.0f}")

    if rain_level == 1:
        multiplier += 0.05
        reasons.append("Hafif yağmur +%5")
    elif rain_level == 2:
        multiplier += 0.10
        reasons.append("Orta yağmur +%10")
    elif rain_level >= 3:
        multiplier += 0.18
        reasons.append("Yoğun yağmur +%18")

    if event_intensity == 1:
        multiplier += 0.05
        reasons.append("Düşük etkinlik +%5")
    elif event_intensity == 2:
        multiplier += 0.12
        reasons.append("Orta etkinlik +%12")
    elif event_intensity >= 3:
        multiplier += 0.22
        reasons.append("Yüksek etkinlik +%22")

    if school_day:
        multiplier += 0.06
        reasons.append("Okul günü +%6")
    else:
        multiplier -= 0.03
        reasons.append("Okul günü değil -%3")

    if work_time_type == "Gece":
        multiplier -= 0.18
        reasons.append("Gece çalışması -%18")
    else:
        multiplier += 0.05
        reasons.append("Gündüz çalışması +%5")

    if accident_risk == 1:
        multiplier += 0.05
        reasons.append("Düşük kaza riski +%5")
    elif accident_risk == 2:
        multiplier += 0.12
        reasons.append("Orta kaza riski +%12")
    elif accident_risk >= 3:
        multiplier += 0.20
        reasons.append("Yüksek kaza riski +%20")

    if public_transport_disruption == 1:
        multiplier += 0.06
        reasons.append("Düşük toplu taşıma aksaması +%6")
    elif public_transport_disruption == 2:
        multiplier += 0.13
        reasons.append("Orta toplu taşıma aksaması +%13")
    elif public_transport_disruption >= 3:
        multiplier += 0.22
        reasons.append("Yüksek toplu taşıma aksaması +%22")

    # Aşırı senaryolarda katsayının negatif veya mantıksız değerlere gitmesini engeller.
    multiplier = max(0.40, multiplier)

    adjusted_prediction = base_prediction * multiplier

    return adjusted_prediction, multiplier, reasons


def calculate_risk_level(value):
    """
    Trafik skorunu risk seviyesine çevirir.
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
    Yol çalışması uygunluk skoru üretir.

    Not:
    Bu skor kullanıcıya gösterilmeyecek.
    Sadece günleri sıralamak ve metinsel uygunluk üretmek için kullanılacak.
    """

    score = 100.0

    score -= scenario_prediction * 0.35

    if is_weekend:
        score += 8

    if work_time_type == "Gece":
        score += 15
    else:
        score -= 5

    if road_work:
        score -= 5

    score -= closed_lane_count * 5

    score = max(0, min(100, score))

    return round(score, 2)


def suitability_label_from_score(score, risk_level):
    """
    Sayısal uygunluk skorunu ve risk seviyesini birlikte değerlendirerek
    kullanıcıya gösterilecek metinsel kararı üretir.

    Böylece yüksek riskli bir günün yanlışlıkla 'Çok uygun'
    görünmesi engellenir.
    """

    # Çok yüksek risk varsa çalışma önerilmez.
    if risk_level == "Çok yüksek":
        return "Uygun değil"

    # Yüksek risk varsa skor çok iyi olsa bile en fazla 'Uygun' diyelim.
    if risk_level == "Yüksek":
        if score >= 55:
            return "Uygun"
        return "Uygun değil"

    # Orta risk varsa çok uygun demek yerine en fazla 'Uygun' diyelim.
    if risk_level == "Orta":
        if score >= 45:
            return "Uygun"
        return "Uygun değil"

    # Risk düşükse skor durumuna göre karar verelim.
    if risk_level == "Düşük":
        if score >= 70:
            return "Çok uygun"
        elif score >= 45:
            return "Uygun"
        else:
            return "Uygun değil"

    return "Uygun değil"


def create_suitability_reasons(row):
    """
    Tahmin sonucuna göre kullanıcıya anlaşılır gerekçeler üretir.
    """

    reasons = []

    if row["risk_level"] == "Düşük":
        reasons.append("Trafik risk seviyesi düşük görünüyor.")
    elif row["risk_level"] == "Orta":
        reasons.append("Trafik risk seviyesi yönetilebilir seviyede.")
    elif row["risk_level"] == "Yüksek":
        reasons.append("Trafik risk seviyesi yüksek olduğu için dikkatli planlama gerekir.")
    else:
        reasons.append("Trafik risk seviyesi çok yüksek olduğu için çalışma önerilmez.")

    if row["work_time_type"] == "Gece":
        reasons.append("Çalışmanın gece yapılması trafik etkisini azaltıyor.")
    else:
        reasons.append("Çalışmanın gündüz yapılması trafik etkisini artırıyor.")

    if bool(row["road_work"]):
        reasons.append("Yol çalışması trafik üzerinde ek baskı oluşturuyor.")

    if int(row["closed_lane_count"]) > 0:
        reasons.append(f"{int(row['closed_lane_count'])} şerit kapalı olduğu için trafik etkisi artıyor.")

    if int(row["rain_level"]) > 0:
        reasons.append("Yağmur seviyesi trafik akışını olumsuz etkileyebilir.")

    if int(row["event_level"]) > 0:
        reasons.append("Etkinlik yoğunluğu bölgede trafik artışına neden olabilir.")

    if int(row["accident_risk_level"]) > 0:
        reasons.append("Kaza riski trafik akışını bozabilecek bir faktör olarak görülüyor.")

    if int(row["public_transport_disruption_level"]) > 0:
        reasons.append("Toplu taşıma aksaması araç trafiğini artırabilir.")

    if bool(row["is_school_day"]):
        reasons.append("Okul günü olması sabah ve akşam saatlerinde yoğunluk oluşturabilir.")

    return reasons


def create_action_recommendations(row):
    """
    Sonuca göre uygulanabilir öneriler üretir.
    """

    recommendations = []

    if row["work_suitability_label"] == "Çok uygun":
        recommendations.append("Bu gün yol çalışması için tercih edilebilir.")
        recommendations.append("Standart trafik bilgilendirmesi yeterli olabilir.")

    elif row["work_suitability_label"] == "Uygun":
        recommendations.append("Bu gün çalışma yapılabilir ancak trafik etkisi takip edilmelidir.")
        recommendations.append("Sürücüler için önceden bilgilendirme yapılması önerilir.")

    else:
        recommendations.append("Bu gün yol çalışması yapılması önerilmez.")
        recommendations.append("Alternatif gün veya gece çalışması değerlendirilmelidir.")

    if row["work_time_type"] == "Gündüz":
        recommendations.append("Mümkünse çalışma gece saatlerine alınmalıdır.")

    if int(row["closed_lane_count"]) >= 2:
        recommendations.append("Kapalı şerit sayısı azaltılırsa uygunluk artabilir.")

    if row["risk_level"] in ["Yüksek", "Çok yüksek"]:
        recommendations.append("Alternatif güzergah planı hazırlanmalıdır.")

    if int(row["public_transport_disruption_level"]) > 0:
        recommendations.append("Toplu taşıma aksaması varsa ek trafik önlemi alınmalıdır.")

    return recommendations


def create_recommendation_text(best_row):
    """
    En uygun gün için özet karar metni üretir.
    """

    label = best_row["work_suitability_label"]
    risk = best_row["risk_level"]

    if label == "Çok uygun":
        main_text = "Seçilen tarih aralığında yol çalışması için en uygun gün bu tarihtir."
    elif label == "Uygun":
        main_text = "Seçilen tarih aralığında bu gün yol çalışması için kullanılabilir görünmektedir."
    else:
        main_text = "Seçilen tarih aralığında bu gün en iyi seçenek olsa da yol çalışması için uygun görünmemektedir."

    return (
        f"{main_text} "
        f"Uygunluk sonucu: {label}. "
        f"Risk seviyesi: {risk}. "
        "Aşağıdaki gerekçe ve öneriler dikkate alınarak planlama yapılmalıdır."
    )


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
    Seçilen ilçe için gelecek N gün tahmin üretir.
    """

    pipeline = package["pipeline"]
    target_col = package["target_col"]
    categorical_features = package["categorical_features"]
    numeric_features = package["numeric_features"]

    model_columns = categorical_features + numeric_features

    start_date = pd.to_datetime(start_date)

    global_mean = history_df[target_col].mean()

    district_history = get_district_history(
        history_df=history_df,
        district=district,
        target_col=target_col
    )

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

        for col in model_columns:
            if col not in input_df.columns:
                input_df[col] = 0

        input_df = input_df[model_columns]

        base_prediction = pipeline.predict(input_df)[0]

        scenario_prediction, scenario_multiplier, scenario_reasons = apply_scenario_adjustment(
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

        suitability_label = suitability_label_from_score(
            suitability_score,
            risk_level
        )

        results.append(
            {
                "date": current_date.date(),
                "district": district,
                "base_prediction": round(float(base_prediction), 2),
                "scenario_prediction": round(float(scenario_prediction), 2),
                "scenario_multiplier": round(float(scenario_multiplier), 3),
                "risk_level": risk_level,

                # Kullanıcıya puan gösterilmeyecek ama sıralama için içeride tutulacak.
                "work_suitability_score": suitability_score,
                "work_suitability_label": suitability_label,

                "scenario_reasons": ", ".join(scenario_reasons),

                # Gerekçe ve öneri üretiminde kullanılacak senaryo bilgileri
                "road_work": road_work,
                "closed_lane_count": closed_lane_count,
                "rain_level": rain_level,
                "event_level": event_intensity,
                "is_school_day": school_day,
                "work_time_type": work_time_type,
                "accident_risk_level": accident_risk,
                "public_transport_disruption_level": public_transport_disruption,
            }
        )

        # Recursive tahmin:
        # Bir sonraki günün lag değerlerinde bugünkü senaryolu tahmini kullanılır.
        district_values.append(scenario_prediction)

    return pd.DataFrame(results)


def make_result_chart(result_df):
    """
    Streamlit üzerinde gösterilecek trafik tahmin grafiği üretir.
    """

    fig, ax = plt.subplots(figsize=(10, 5))

    ax.plot(
        result_df["date"],
        result_df["base_prediction"],
        marker="o",
        label="Normal Tahmin"
    )

    ax.plot(
        result_df["date"],
        result_df["scenario_prediction"],
        marker="o",
        label="Senaryolu Tahmin"
    )

    ax.set_title("Normal Tahmin vs Senaryolu Trafik Tahmini")
    ax.set_xlabel("Tarih")
    ax.set_ylabel("Trafik Skoru")
    ax.legend()
    ax.grid(True, alpha=0.3)

    plt.xticks(rotation=30)
    plt.tight_layout()

    return fig


# ============================================================
# 4) ARAYÜZ
# ============================================================

package = load_model_package()
history_df = load_history_data()

st.title("🚦 İlçe Bazlı Trafik Tahmin ve Yol Çalışması Karar Destek Sistemi")

st.write(
    """
Bu uygulama, geçmiş trafik verileriyle eğitilmiş makine öğrenmesi modelini kullanarak
ilçe bazlı trafik tahmini üretir. Yol çalışması, kapalı şerit, yağmur, etkinlik ve benzeri
senaryo parametreleriyle tahmini yeniden değerlendirir.
"""
)

with st.expander("Model ve veri bilgisi", expanded=False):
    st.write(f"**Model:** {package['model_name']}")
    st.write(f"**Hedef kolon:** {package['target_col']}")
    st.write(f"**Veri satırı:** {len(history_df):,}")
    st.write(f"**İlçe sayısı:** {history_df['district'].nunique()}")
    st.write(f"**Tarih aralığı:** {history_df['date'].min().date()} - {history_df['date'].max().date()}")

st.sidebar.header("Tahmin Parametreleri")

districts = sorted(history_df["district"].dropna().astype(str).unique().tolist())

selected_district = st.sidebar.selectbox(
    "İlçe seç",
    districts,
    index=districts.index("Kadıköy") if "Kadıköy" in districts else 0
)

start_date = st.sidebar.date_input(
    "Başlangıç tarihi",
    value=pd.to_datetime("2026-05-19").date()
)

days = st.sidebar.slider(
    "Kaç gün tahmin yapılsın?",
    min_value=1,
    max_value=30,
    value=7
)

st.sidebar.subheader("Yol Çalışması Senaryosu")

road_work = st.sidebar.checkbox(
    "Yol çalışması var mı?",
    value=True
)

closed_lane_count = st.sidebar.slider(
    "Kapalı şerit sayısı",
    min_value=0,
    max_value=3,
    value=1
)

work_time_type = st.sidebar.radio(
    "Çalışma zamanı",
    ["Gündüz", "Gece"],
    index=1
)

st.sidebar.subheader("Çevresel Etkiler")

rain_level = st.sidebar.selectbox(
    "Yağmur seviyesi",
    options=[0, 1, 2, 3],
    format_func=lambda x: {
        0: "Yok",
        1: "Hafif",
        2: "Orta",
        3: "Yoğun"
    }[x]
)

event_intensity = st.sidebar.selectbox(
    "Etkinlik yoğunluğu (Konser Fuar vs.)",
    options=[0, 1, 2, 3],
    format_func=lambda x: {
        0: "Yok",
        1: "Düşük",
        2: "Orta",
        3: "Yüksek"
    }[x]
)

school_day = st.sidebar.checkbox(
    "Okul günü mü?",
    value=True
)

accident_risk = st.sidebar.selectbox(
    "Kaza riski",
    options=[0, 1, 2, 3],
    format_func=lambda x: {
        0: "Yok",
        1: "Düşük",
        2: "Orta",
        3: "Yüksek"
    }[x]
)

public_transport_disruption = st.sidebar.selectbox(
    "Toplu taşıma aksaması",
    options=[0, 1, 2, 3],
    format_func=lambda x: {
        0: "Yok",
        1: "Düşük",
        2: "Orta",
        3: "Yüksek"
    }[x]
)

run_button = st.sidebar.button("Tahmin Yap", type="primary")


if run_button:
    result_df = predict_future_days(
        package=package,
        history_df=history_df,
        district=selected_district,
        start_date=start_date,
        days=days,
        road_work=road_work,
        closed_lane_count=closed_lane_count,
        rain_level=rain_level,
        event_intensity=event_intensity,
        school_day=school_day,
        work_time_type=work_time_type,
        accident_risk=accident_risk,
        public_transport_disruption=public_transport_disruption
    )

    # Her gün için açıklanabilir gerekçe ve öneri listeleri üret.
    result_df["reasons"] = result_df.apply(create_suitability_reasons, axis=1)
    result_df["recommendations"] = result_df.apply(create_action_recommendations, axis=1)

    sorted_result = result_df.sort_values(
        ["work_suitability_score", "scenario_prediction"],
        ascending=[False, True]
    ).reset_index(drop=True)

    best_day = sorted_result.iloc[0]

    worst_day = result_df.sort_values(
        ["work_suitability_score", "scenario_prediction"],
        ascending=[True, False]
    ).iloc[0]

    st.subheader("📌 Özet Sonuç")

    col1, col2, col3, col4 = st.columns(4)

    col1.metric("En uygun tarih", str(best_day["date"]))
    col2.metric("Uygunluk", best_day["work_suitability_label"])
    col3.metric("Risk seviyesi", best_day["risk_level"])
    col4.metric("Senaryolu trafik", best_day["scenario_prediction"])

    st.info(create_recommendation_text(best_day))

    st.subheader("📌 Karar Gerekçeleri")

    for reason in best_day["reasons"]:
        st.write(f"- {reason}")

    st.subheader("✅ Öneriler")

    for recommendation in best_day["recommendations"]:
        st.write(f"- {recommendation}")

    st.subheader("📅 Alternatif Gün Önerileri")

    alternative_days = sorted_result.head(3).copy()

    alternative_view = alternative_days[
        [
            "date",
            "district",
            "work_suitability_label",
            "risk_level",
            "scenario_prediction"
        ]
    ].rename(
        columns={
            "date": "Tarih",
            "district": "İlçe",
            "work_suitability_label": "Uygunluk",
            "risk_level": "Risk",
            "scenario_prediction": "Senaryolu Trafik"
        }
    )

    st.dataframe(
        alternative_view,
        use_container_width=True
    )

    st.subheader("📊 Tahmin Tablosu")

    display_df = result_df[
        [
            "date",
            "district",
            "base_prediction",
            "scenario_prediction",
            "risk_level",
            "work_suitability_label",
            "scenario_multiplier"
        ]
    ].copy()

    display_df = display_df.rename(
        columns={
            "date": "Tarih",
            "district": "İlçe",
            "base_prediction": "Normal Tahmin",
            "scenario_prediction": "Senaryolu Tahmin",
            "risk_level": "Risk",
            "work_suitability_label": "Uygunluk",
            "scenario_multiplier": "Senaryo Katsayısı"
        }
    )

    st.dataframe(
        display_df,
        use_container_width=True
    )

    st.subheader("📈 Trafik Tahmin Grafiği")
    st.pyplot(make_result_chart(result_df))

    st.subheader("✅ Günlere Göre Uygunluk Durumu")

    suitability_view = result_df[
        [
            "date",
            "district",
            "work_suitability_label",
            "risk_level"
        ]
    ].copy()

    suitability_view = suitability_view.rename(
        columns={
            "date": "Tarih",
            "district": "İlçe",
            "work_suitability_label": "Uygunluk",
            "risk_level": "Risk"
        }
    )

    st.dataframe(
        suitability_view,
        use_container_width=True
    )

    st.subheader("🔍 En İyi ve En Riskli Gün Karşılaştırması")

    c1, c2 = st.columns(2)

    with c1:
        st.success("En uygun gün")
        st.write(f"**Tarih:** {best_day['date']}")
        st.write(f"**Normal tahmin:** {best_day['base_prediction']}")
        st.write(f"**Senaryolu tahmin:** {best_day['scenario_prediction']}")
        st.write(f"**Risk:** {best_day['risk_level']}")
        st.write(f"**Uygunluk:** {best_day['work_suitability_label']}")
        st.write(f"**Etkiler:** {best_day['scenario_reasons']}")

    with c2:
        st.error("En riskli gün")
        st.write(f"**Tarih:** {worst_day['date']}")
        st.write(f"**Normal tahmin:** {worst_day['base_prediction']}")
        st.write(f"**Senaryolu tahmin:** {worst_day['scenario_prediction']}")
        st.write(f"**Risk:** {worst_day['risk_level']}")
        st.write(f"**Uygunluk:** {worst_day['work_suitability_label']}")
        st.write(f"**Etkiler:** {worst_day['scenario_reasons']}")

    # CSV içinde de puanı gizliyoruz.
    download_df = result_df.drop(
        columns=["work_suitability_score"],
        errors="ignore"
    ).copy()

    # Liste kolonlarını CSV'de okunabilir metne dönüştürüyoruz.
    if "reasons" in download_df.columns:
        download_df["reasons"] = download_df["reasons"].apply(lambda items: " | ".join(items) if isinstance(items, list) else items)

    if "recommendations" in download_df.columns:
        download_df["recommendations"] = download_df["recommendations"].apply(lambda items: " | ".join(items) if isinstance(items, list) else items)

    csv_bytes = download_df.to_csv(
        index=False,
        encoding="utf-8-sig"
    ).encode("utf-8-sig")

    st.download_button(
        label="CSV olarak indir",
        data=csv_bytes,
        file_name="trafik_senaryo_tahminleri.csv",
        mime="text/csv"
    )

else:
    st.warning("Sol menüden parametreleri seçip **Tahmin Yap** butonuna bas.")
