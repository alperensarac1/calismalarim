from pathlib import Path

import pandas as pd
import numpy as np
import joblib
import matplotlib.pyplot as plt

from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder
from sklearn.pipeline import Pipeline

from sklearn.linear_model import Ridge
from sklearn.ensemble import (
    RandomForestRegressor,
    ExtraTreesRegressor,
    GradientBoostingRegressor
)


# ============================================================
# 1) KLASÖR VE DOSYA YOLLARI
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

# EDA sonrası oluşan temiz veri seti.
# Bu dosya eda_prepare_dataset.py çalıştıktan sonra oluşmalı.
DATA_PATH = BASE_DIR / "data" / "processed" / "istanbul_ilce_gunluk_trafik_model_ready.csv"

MODEL_DIR = BASE_DIR / "models"
OUTPUT_DIR = BASE_DIR / "outputs"

MODEL_DIR.mkdir(exist_ok=True)
OUTPUT_DIR.mkdir(exist_ok=True)

MODEL_PATH = MODEL_DIR / "best_traffic_model.joblib"
RESULTS_PATH = OUTPUT_DIR / "model_results.csv"
PREDICTIONS_PATH = OUTPUT_DIR / "predictions.csv"
PLOT_PATH = OUTPUT_DIR / "actual_vs_predicted.png"
FEATURE_IMPORTANCE_PATH = OUTPUT_DIR / "feature_importance.csv"


# ============================================================
# 2) VERİYİ OKUMA
# ============================================================

def load_dataset():
    """
    Model-ready veri setini okur.

    Beklenen dosya:
    data/processed/istanbul_ilce_gunluk_trafik_model_ready.csv

    Bu dosya daha önce eda_prepare_dataset.py tarafından oluşturulmalıdır.
    """

    if not DATA_PATH.exists():
        raise FileNotFoundError(
            f"Model-ready veri seti bulunamadı:\n{DATA_PATH}\n\n"
            "Önce şu dosyayı çalıştırmalısın:\n"
            "python eda_prepare_dataset.py"
        )

    df = pd.read_csv(DATA_PATH)

    print("\n[1] Model-ready veri seti okundu.")
    print(f"Satır sayısı : {len(df):,}")
    print(f"Sütun sayısı : {df.shape[1]}")
    print(f"Sütunlar     : {list(df.columns)}")

    if "date" not in df.columns:
        raise RuntimeError("Veri setinde 'date' kolonu bulunamadı.")

    if "district" not in df.columns:
        raise RuntimeError("Veri setinde 'district' kolonu bulunamadı.")

    df["date"] = pd.to_datetime(df["date"], errors="coerce")

    before_count = len(df)
    df = df.dropna(subset=["date"]).copy()
    after_count = len(df)

    if before_count != after_count:
        print(f"Geçersiz tarih nedeniyle silinen satır: {before_count - after_count}")

    return df


# ============================================================
# 3) HEDEF KOLONU BULMA
# ============================================================

def detect_target_column(df):
    """
    Tahmin edilecek hedef kolonu otomatik bulur.

    Şu an iki ihtimali destekliyoruz:

    avg_traffic_density:
        Eğer veri setinde doğrudan trafik yoğunluğu varsa.

    avg_congestion_score:
        Eğer trafik sıkışıklığı skoru ortalama hızdan üretildiyse.
    """

    possible_targets = [
        "avg_traffic_density",
        "avg_congestion_score"
    ]

    for col in possible_targets:
        if col in df.columns:
            print(f"\n[2] Hedef kolon seçildi: {col}")
            return col

    raise RuntimeError(
        "Hedef kolon bulunamadı.\n"
        "Beklenen kolonlardan biri olmalıydı:\n"
        "- avg_traffic_density\n"
        "- avg_congestion_score\n\n"
        f"Mevcut kolonlar: {list(df.columns)}"
    )


# ============================================================
# 4) FEATURE HAZIRLAMA
# ============================================================

def prepare_features(df, target_col):
    """
    Model için X ve y verilerini hazırlar.

    Kullanılmayacak kolonlar:
    - date: tarih sıralama için gerekli ama doğrudan modele verilmeyecek
    - target_col: tahmin edilecek hedef

    Kullanılacak kolonlar:
    - district
    - month
    - day
    - day_of_week
    - is_weekend
    - week_of_year
    - quarter
    - month_sin / month_cos
    - day_of_week_sin / day_of_week_cos
    - is_holiday
    - lag_* kolonları
    - rolling_* kolonları
    """

    print("\n[3] Feature hazırlama başladı.")

    df = df.copy()

    # Hedef kolonu sayısal yap.
    df[target_col] = pd.to_numeric(df[target_col], errors="coerce")

    before_count = len(df)
    df = df.dropna(subset=[target_col]).copy()
    after_count = len(df)

    if before_count != after_count:
        print(f"Hedef kolonu boş/geçersiz olduğu için silinen satır: {before_count - after_count}")

    # Modele doğrudan vermeyeceğimiz kolonlar
    ignore_cols = [
        "date",
        target_col
    ]

    feature_cols = [
        col for col in df.columns
        if col not in ignore_cols
    ]

    if not feature_cols:
        raise RuntimeError("Model için kullanılacak feature kolonu bulunamadı.")

    # Kategorik kolonlar
    categorical_features = []

    if "district" in feature_cols:
        categorical_features.append("district")

    # Sayısal kolonlar
    numeric_features = [
        col for col in feature_cols
        if col not in categorical_features
    ]

    # District temizliği
    if "district" in categorical_features:
        df["district"] = df["district"].astype(str).str.strip()
        df["district"] = df["district"].replace({"": "unknown"})

    # Sayısal kolonları sayısal formata çevir.
    for col in numeric_features:
        df[col] = pd.to_numeric(df[col], errors="coerce")

    # Sayısal eksikleri median ile doldur.
    for col in numeric_features:
        missing_count = df[col].isna().sum()

        if missing_count > 0:
            median_value = df[col].median()

            # Eğer tüm kolon boşsa 0 ver.
            if pd.isna(median_value):
                median_value = 0

            df[col] = df[col].fillna(median_value)

    # Kategorik eksikleri unknown ile doldur.
    for col in categorical_features:
        df[col] = df[col].fillna("unknown").astype(str)

    X = df[feature_cols].copy()
    y = df[target_col].copy()

    print(f"Kategorik kolonlar: {categorical_features}")
    print(f"Sayısal kolonlar  : {numeric_features}")
    print(f"Toplam feature    : {len(feature_cols)}")
    print(f"Model satır sayısı: {len(X):,}")

    return X, y, df, categorical_features, numeric_features


# ============================================================
# 5) TARİH BAZLI TRAIN / TEST AYRIMI
# ============================================================

def time_based_split(df, X, y, test_ratio=0.20):
    """
    Veriyi tarih bazlı böler.

    ÖNEMLİ:
    Aynı tarih hem eğitim hem test tarafına düşmez.

    Neden?
    Çünkü trafik tahmini geleceği tahmin etmeye çalışır.
    Rastgele train/test ayrımı yaparsak model aynı günün farklı ilçelerini
    hem eğitimde hem testte görebilir. Bu da sonucu olduğundan iyi gösterir.

    Mantık:
    - Benzersiz tarihleri sırala.
    - İlk %80 tarih eğitim.
    - Son %20 tarih test.
    """

    print("\n[4] Tarih bazlı kesin train/test ayrımı yapılıyor.")

    df = df.copy()
    df["date"] = pd.to_datetime(df["date"], errors="coerce")
    df = df.dropna(subset=["date"]).copy()

    unique_dates = sorted(df["date"].dt.date.unique())

    if len(unique_dates) < 10:
        raise RuntimeError(
            "Tarih sayısı çok az. Sağlıklı zaman bazlı test için daha fazla gün verisi gerekir."
        )

    split_date_index = int(len(unique_dates) * (1 - test_ratio))

    # Güvenlik: index sınır dışına çıkmasın.
    split_date_index = min(max(split_date_index, 1), len(unique_dates) - 1)

    split_date = unique_dates[split_date_index]

    train_mask = df["date"].dt.date < split_date
    test_mask = df["date"].dt.date >= split_date

    train_indices = df[train_mask].index
    test_indices = df[test_mask].index

    X_train = X.loc[train_indices].copy()
    X_test = X.loc[test_indices].copy()

    y_train = y.loc[train_indices].copy()
    y_test = y.loc[test_indices].copy()

    test_df = df.loc[test_indices].copy()

    print(f"Toplam benzersiz tarih sayısı: {len(unique_dates)}")
    print(f"Kesim tarihi: {split_date}")

    print(f"Eğitim satırı: {len(X_train):,}")
    print(f"Test satırı  : {len(X_test):,}")

    print(
        f"Eğitim tarih aralığı: "
        f"{df.loc[train_indices, 'date'].min().date()} - "
        f"{df.loc[train_indices, 'date'].max().date()}"
    )

    print(
        f"Test tarih aralığı  : "
        f"{df.loc[test_indices, 'date'].min().date()} - "
        f"{df.loc[test_indices, 'date'].max().date()}"
    )

    # Kontrol: aynı tarih iki tarafa düşmüş mü?
    train_dates = set(df.loc[train_indices, "date"].dt.date.unique())
    test_dates = set(df.loc[test_indices, "date"].dt.date.unique())

    overlap_dates = train_dates.intersection(test_dates)

    if overlap_dates:
        raise RuntimeError(
            "Hata: Bazı tarihler hem train hem test içinde var.\n"
            f"Ortak tarihler: {sorted(list(overlap_dates))[:10]}"
        )

    return X_train, X_test, y_train, y_test, test_df


# ============================================================
# 6) PREPROCESSOR
# ============================================================

def build_preprocessor(categorical_features, numeric_features):
    """
    Model öncesi veri dönüşüm pipeline'ını oluşturur.

    district:
        OneHotEncoder ile sayısal hale çevrilir.

    numeric_features:
        Doğrudan modele verilir.
    """

    transformers = []

    if categorical_features:
        transformers.append(
            (
                "categorical",
                OneHotEncoder(handle_unknown="ignore"),
                categorical_features
            )
        )

    if numeric_features:
        transformers.append(
            (
                "numeric",
                "passthrough",
                numeric_features
            )
        )

    if not transformers:
        raise RuntimeError("Preprocessor için feature bulunamadı.")

    preprocessor = ColumnTransformer(
        transformers=transformers,
        remainder="drop"
    )

    return preprocessor


# ============================================================
# 7) MODELLER
# ============================================================

def get_models():
    """
    Birden fazla modeli karşılaştırıyoruz.

    Ridge Regression:
        Basit referans model.

    Random Forest:
        Güçlü, stabil, yorumu kolay.

    Extra Trees:
        Random Forest'a benzer ama daha rastgele bölmeler yapar.
        Çoğu tabular veri probleminde güçlü sonuç verir.

    Gradient Boosting:
        Ardışık şekilde hataları azaltmaya çalışır.
    """

    models = {
        "Ridge Regression": Ridge(alpha=1.0),

        "Random Forest": RandomForestRegressor(
            n_estimators=500,
            max_depth=None,
            min_samples_split=2,
            min_samples_leaf=1,
            random_state=42,
            n_jobs=-1
        ),

        "Extra Trees": ExtraTreesRegressor(
            n_estimators=500,
            max_depth=None,
            min_samples_split=2,
            min_samples_leaf=1,
            random_state=42,
            n_jobs=-1
        ),

        "Gradient Boosting": GradientBoostingRegressor(
            n_estimators=500,
            learning_rate=0.03,
            max_depth=3,
            random_state=42
        )
    }

    return models


# ============================================================
# 8) MODEL EĞİTME VE KARŞILAŞTIRMA
# ============================================================

def train_models(
    X_train,
    X_test,
    y_train,
    y_test,
    categorical_features,
    numeric_features
):
    """
    Modelleri eğitir, test verisi üzerinde değerlendirir
    ve en iyi modeli seçer.

    En iyi model seçim kriteri:
    - MAE en düşük olan model.

    MAE:
        Ortalama mutlak hata.
        Trafik skorunda ortalama kaç birim hata yaptığımızı gösterir.
    """

    print("\n[5] Model eğitimi başlıyor.")

    preprocessor = build_preprocessor(
        categorical_features=categorical_features,
        numeric_features=numeric_features
    )

    models = get_models()

    results = []

    best_pipeline = None
    best_model_name = None
    best_mae = float("inf")

    for model_name, model in models.items():
        print(f"\nModel eğitiliyor: {model_name}")

        pipeline = Pipeline(
            steps=[
                ("preprocessor", preprocessor),
                ("model", model)
            ]
        )

        pipeline.fit(X_train, y_train)

        y_pred = pipeline.predict(X_test)

        mae = mean_absolute_error(y_test, y_pred)
        rmse = np.sqrt(mean_squared_error(y_test, y_pred))
        r2 = r2_score(y_test, y_pred)

        print(f"MAE : {mae:.4f}")
        print(f"RMSE: {rmse:.4f}")
        print(f"R2  : {r2:.4f}")

        results.append({
            "model": model_name,
            "mae": mae,
            "rmse": rmse,
            "r2": r2
        })

        if mae < best_mae:
            best_mae = mae
            best_model_name = model_name
            best_pipeline = pipeline

    results_df = pd.DataFrame(results).sort_values("mae").reset_index(drop=True)

    print("\n[6] Model karşılaştırma sonucu:")
    print(results_df)

    print(f"\nEn iyi model: {best_model_name}")

    return best_pipeline, best_model_name, results_df


# ============================================================
# 9) TAHMİNLERİ KAYDETME
# ============================================================

def save_predictions(best_pipeline, X_test, y_test, test_df):
    """
    Test verisi üzerindeki tahminleri CSV olarak kaydeder.
    """

    print("\n[7] Test tahminleri kaydediliyor.")

    y_pred = best_pipeline.predict(X_test)

    predictions_df = test_df[["date", "district"]].copy()

    predictions_df["actual"] = y_test.values
    predictions_df["predicted"] = y_pred
    predictions_df["error"] = predictions_df["actual"] - predictions_df["predicted"]
    predictions_df["abs_error"] = predictions_df["error"].abs()

    predictions_df = predictions_df.sort_values(["date", "district"]).reset_index(drop=True)

    predictions_df.to_csv(
        PREDICTIONS_PATH,
        index=False,
        encoding="utf-8-sig"
    )

    print(f"Tahmin dosyası kaydedildi: {PREDICTIONS_PATH}")

    print("\nTahmin örnekleri:")
    print(predictions_df.head(10))

    return predictions_df


# ============================================================
# 10) GRAFİK OLUŞTURMA
# ============================================================

def plot_actual_vs_predicted(predictions_df):
    """
    Gerçek değerler ile tahmin değerlerini karşılaştıran grafik üretir.
    """

    print("\n[8] Gerçek vs tahmin grafiği oluşturuluyor.")

    plt.figure(figsize=(10, 6))

    plt.scatter(
        predictions_df["actual"],
        predictions_df["predicted"],
        alpha=0.5
    )

    min_val = min(
        predictions_df["actual"].min(),
        predictions_df["predicted"].min()
    )

    max_val = max(
        predictions_df["actual"].max(),
        predictions_df["predicted"].max()
    )

    plt.plot(
        [min_val, max_val],
        [min_val, max_val],
        linestyle="--"
    )

    plt.xlabel("Gerçek trafik skoru")
    plt.ylabel("Tahmin edilen trafik skoru")
    plt.title("Gerçek Trafik Skoru vs Model Tahmini")
    plt.tight_layout()

    plt.savefig(PLOT_PATH, dpi=150)
    plt.close()

    print(f"Grafik kaydedildi: {PLOT_PATH}")


# ============================================================
# 11) FEATURE IMPORTANCE KAYDETME
# ============================================================

def save_feature_importance(best_pipeline, categorical_features, numeric_features):
    """
    Ağaç tabanlı modeller için feature importance kaydeder.

    Ridge Regression gibi modellerde feature_importances_ yoktur.
    Bu durumda sessizce geçer.
    """

    print("\n[9] Feature importance çıkarılıyor.")

    model = best_pipeline.named_steps["model"]
    preprocessor = best_pipeline.named_steps["preprocessor"]

    if not hasattr(model, "feature_importances_"):
        print("Bu model feature_importances_ desteklemiyor. Atlandı.")
        return

    feature_names = []

    # Kategorik feature isimleri
    if categorical_features:
        try:
            cat_transformer = preprocessor.named_transformers_["categorical"]
            cat_names = cat_transformer.get_feature_names_out(categorical_features)
            feature_names.extend(cat_names.tolist())
        except Exception:
            feature_names.extend(categorical_features)

    # Sayısal feature isimleri
    feature_names.extend(numeric_features)

    importances = model.feature_importances_

    # Güvenlik kontrolü
    if len(feature_names) != len(importances):
        print(
            "Feature isim sayısı ile importance sayısı eşleşmedi.\n"
            f"Feature isim sayısı: {len(feature_names)}\n"
            f"Importance sayısı  : {len(importances)}\n"
            "Feature importance kaydı atlandı."
        )
        return

    importance_df = pd.DataFrame({
        "feature": feature_names,
        "importance": importances
    }).sort_values("importance", ascending=False)

    importance_df.to_csv(
        FEATURE_IMPORTANCE_PATH,
        index=False,
        encoding="utf-8-sig"
    )

    print(f"Feature importance kaydedildi: {FEATURE_IMPORTANCE_PATH}")

    print("\nEn önemli 15 feature:")
    print(importance_df.head(15))


# ============================================================
# 12) MODELİ KAYDETME
# ============================================================

def save_model(
    best_pipeline,
    best_model_name,
    target_col,
    categorical_features,
    numeric_features
):
    """
    En iyi modeli joblib formatında kaydeder.

    Sadece model değil, bütün pipeline kaydedilir:
    - OneHotEncoder
    - Sayısal kolon geçişi
    - Eğitilmiş model

    Ayrıca predict_scenario.py tarafında lazım olacak kolon bilgilerini de kaydediyoruz.
    """

    print("\n[10] Model kaydediliyor.")

    model_package = {
        "model_name": best_model_name,
        "target_col": target_col,
        "categorical_features": categorical_features,
        "numeric_features": numeric_features,
        "pipeline": best_pipeline
    }

    joblib.dump(model_package, MODEL_PATH)

    print(f"Model kaydedildi: {MODEL_PATH}")


# ============================================================
# 13) BASİT ÖRNEK TAHMİN
# ============================================================

def example_prediction(best_pipeline, X):
    """
    Modelin çalıştığını görmek için mevcut X verisinden bir örnek alıp tahmin yapar.
    """

    print("\n[11] Örnek tahmin yapılıyor.")

    if X.empty:
        print("X boş olduğu için örnek tahmin yapılamadı.")
        return

    sample = X.iloc[[0]].copy()

    prediction = best_pipeline.predict(sample)[0]

    print("Örnek girdi:")
    print(sample)

    print(f"\nTahmin edilen trafik skoru: {prediction:.4f}")


# ============================================================
# 14) ANA AKIŞ
# ============================================================

def main():
    print("Gerçekçi Trafik Tahmin Modeli Eğitimi")
    print("=" * 70)

    # 1. Veri oku
    df = load_dataset()

    # 2. Hedef kolonu bul
    target_col = detect_target_column(df)

    # 3. Feature hazırla
    X, y, cleaned_df, categorical_features, numeric_features = prepare_features(
        df=df,
        target_col=target_col
    )

    # 4. Tarih bazlı train/test ayrımı
    X_train, X_test, y_train, y_test, test_df = time_based_split(
        df=cleaned_df,
        X=X,
        y=y,
        test_ratio=0.20
    )

    # 5. Modelleri eğit
    best_pipeline, best_model_name, results_df = train_models(
        X_train=X_train,
        X_test=X_test,
        y_train=y_train,
        y_test=y_test,
        categorical_features=categorical_features,
        numeric_features=numeric_features
    )

    # 6. Model sonuçlarını kaydet
    results_df.to_csv(
        RESULTS_PATH,
        index=False,
        encoding="utf-8-sig"
    )

    print(f"\nModel sonuçları kaydedildi: {RESULTS_PATH}")

    # 7. Tahminleri kaydet
    predictions_df = save_predictions(
        best_pipeline=best_pipeline,
        X_test=X_test,
        y_test=y_test,
        test_df=test_df
    )

    # 8. Grafik oluştur
    plot_actual_vs_predicted(predictions_df)

    # 9. Feature importance kaydet
    save_feature_importance(
        best_pipeline=best_pipeline,
        categorical_features=categorical_features,
        numeric_features=numeric_features
    )

    # 10. Modeli kaydet
    save_model(
        best_pipeline=best_pipeline,
        best_model_name=best_model_name,
        target_col=target_col,
        categorical_features=categorical_features,
        numeric_features=numeric_features
    )

    # 11. Örnek tahmin
    example_prediction(
        best_pipeline=best_pipeline,
        X=X
    )

    print("\nModel eğitimi tamamlandı.")


if __name__ == "__main__":
    main()