import os
import zipfile
import subprocess
from pathlib import Path

import requests
import pandas as pd
import numpy as np

import geopandas as gpd
from shapely.geometry import Point

try:
    import pygeohash as pgh
except ImportError:
    pgh = None


# ============================================================
# 1) GENEL AYARLAR
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

RAW_DIR = BASE_DIR / "data" / "raw"
GEO_DIR = BASE_DIR / "data" / "geo"
PROCESSED_DIR = BASE_DIR / "data" / "processed"
TEMP_DIR = BASE_DIR / "data" / "temp_year"

RAW_DIR.mkdir(parents=True, exist_ok=True)
GEO_DIR.mkdir(parents=True, exist_ok=True)
PROCESSED_DIR.mkdir(parents=True, exist_ok=True)
TEMP_DIR.mkdir(parents=True, exist_ok=True)

# Hangi yılı çalışmak istiyorsan burayı değiştir.
TARGET_YEAR = 2024

# Büyük veri seti
KAGGLE_DATASET = "omertarikyilmaz/istabul-traffic-2020-2024"

# İstanbul ilçe sınırları
DISTRICT_GEOJSON_URL = (
    "https://raw.githubusercontent.com/"
    "sahircansurmeli/istanbul-geojson/master/ilce_geojson.json"
)

DISTRICT_GEOJSON_PATH = GEO_DIR / "ilce_geojson.json"

ZIP_PATH = RAW_DIR / "istanbul_traffic_2020_2024.zip"
EXTRACT_DIR = RAW_DIR / "istanbul_traffic_2020_2024"

FINAL_DATASET_PATH = (
    PROCESSED_DIR / f"istanbul_ilce_gunluk_trafik_{TARGET_YEAR}.csv"
)


# ============================================================
# 2) KAGGLE VERİ SETİNİ İNDİRME
# ============================================================

def download_kaggle_dataset():
    """
    Kaggle üzerinden 2020-2024 trafik veri setini indirir.

    Gerekli:
    C:/Users/KULLANICI_ADIN/.kaggle/kaggle.json
    """

    print("\n[1] Kaggle 2020-2024 trafik veri seti indiriliyor...")

    if ZIP_PATH.exists():
        print(f"ZIP zaten var: {ZIP_PATH}")
        return ZIP_PATH

    command = [
        "kaggle",
        "datasets",
        "download",
        "-d",
        KAGGLE_DATASET,
        "-p",
        str(RAW_DIR),
        "-o"
    ]

    try:
        subprocess.run(command, check=True)
    except FileNotFoundError:
        raise RuntimeError(
            "Kaggle komutu bulunamadı.\n"
            "Önce şunu kur:\n"
            "pip install kaggle"
        )
    except subprocess.CalledProcessError:
        raise RuntimeError(
            "Kaggle veri seti indirilemedi.\n\n"
            "Kontrol etmen gerekenler:\n"
            "1) Kaggle hesabından API token aldın mı?\n"
            "2) kaggle.json dosyası .kaggle klasöründe mi?\n"
            "3) Kaggle veri seti sayfasında kullanım koşullarını kabul ettin mi?"
        )

    zip_files = list(RAW_DIR.glob("*.zip"))

    if not zip_files:
        raise FileNotFoundError("RAW klasöründe indirilen zip bulunamadı.")

    latest_zip = max(zip_files, key=lambda p: p.stat().st_mtime)
    latest_zip.rename(ZIP_PATH)

    print(f"İndirildi: {ZIP_PATH}")

    return ZIP_PATH


# ============================================================
# 3) ZIP AÇMA
# ============================================================

def extract_dataset(zip_path):
    """
    İndirilen zip dosyasını açar.
    """

    print("\n[2] ZIP dosyası açılıyor...")

    if EXTRACT_DIR.exists() and list(EXTRACT_DIR.rglob("*.csv")):
        print(f"Veri zaten açılmış: {EXTRACT_DIR}")
        return EXTRACT_DIR

    EXTRACT_DIR.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(zip_path, "r") as zip_ref:
        zip_ref.extractall(EXTRACT_DIR)

    print(f"ZIP açıldı: {EXTRACT_DIR}")

    return EXTRACT_DIR


# ============================================================
# 4) GEOJSON İNDİRME
# ============================================================

def download_district_geojson():
    """
    İstanbul ilçe sınırı GeoJSON dosyasını indirir.
    """

    print("\n[3] İlçe GeoJSON indiriliyor...")

    if DISTRICT_GEOJSON_PATH.exists():
        print(f"GeoJSON zaten var: {DISTRICT_GEOJSON_PATH}")
        return DISTRICT_GEOJSON_PATH

    response = requests.get(DISTRICT_GEOJSON_URL, timeout=60)

    if response.status_code != 200:
        raise RuntimeError(
            f"GeoJSON indirilemedi. Status: {response.status_code}"
        )

    DISTRICT_GEOJSON_PATH.write_bytes(response.content)

    print(f"GeoJSON indirildi: {DISTRICT_GEOJSON_PATH}")

    return DISTRICT_GEOJSON_PATH


# ============================================================
# 5) KOLON BULMA YARDIMCILARI
# ============================================================

def normalize_column_name(col):
    return (
        str(col)
        .strip()
        .lower()
        .replace(" ", "_")
        .replace("-", "_")
        .replace(".", "_")
    )


def find_column(columns, possible_names):
    normalized_map = {
        normalize_column_name(col): col
        for col in columns
    }

    for name in possible_names:
        key = normalize_column_name(name)
        if key in normalized_map:
            return normalized_map[key]

    for normalized_col, original_col in normalized_map.items():
        for name in possible_names:
            key = normalize_column_name(name)
            if key in normalized_col:
                return original_col

    return None


def detect_columns_from_sample(df):
    """
    CSV örneği üzerinden kolonları otomatik algılar.
    """

    columns = df.columns

    date_col = find_column(
        columns,
        [
            "DATE_TIME",
            "date_time",
            "datetime",
            "timestamp",
            "date",
            "tarih",
            "zaman"
        ]
    )

    lat_col = find_column(
        columns,
        [
            "LATITUDE",
            "latitude",
            "lat",
            "enlem"
        ]
    )

    lon_col = find_column(
        columns,
        [
            "LONGITUDE",
            "longitude",
            "lon",
            "lng",
            "boylam"
        ]
    )

    geohash_col = find_column(
        columns,
        [
            "GEOHASH",
            "geohash"
        ]
    )

    density_col = find_column(
        columns,
        [
            "traffic_density",
            "density",
            "yogunluk",
            "yoğunluk",
            "traffic_index"
        ]
    )

    avg_speed_col = find_column(
        columns,
        [
            "AVERAGE_SPEED",
            "average_speed",
            "avg_speed",
            "ortalama_hiz",
            "ortalama_hız"
        ]
    )

    vehicle_count_col = find_column(
        columns,
        [
            "NUMBER_OF_VEHICLES",
            "number_of_vehicles",
            "vehicle_count",
            "arac_sayisi",
            "araç_sayısı"
        ]
    )

    print("\nAlgılanan kolonlar:")
    print(f"Tarih kolonu       : {date_col}")
    print(f"Latitude kolonu    : {lat_col}")
    print(f"Longitude kolonu   : {lon_col}")
    print(f"Geohash kolonu     : {geohash_col}")
    print(f"Yoğunluk kolonu    : {density_col}")
    print(f"Ortalama hız kolonu: {avg_speed_col}")
    print(f"Araç sayısı kolonu : {vehicle_count_col}")

    if date_col is None:
        raise RuntimeError("Tarih kolonu bulunamadı.")

    if density_col is None and avg_speed_col is None:
        raise RuntimeError(
            "Ne trafik yoğunluğu ne de ortalama hız kolonu bulundu."
        )

    if lat_col is None and lon_col is None and geohash_col is None:
        raise RuntimeError(
            "Koordinat veya geohash kolonu bulunamadı. İlçe eşleştirmesi yapılamaz."
        )

    return {
        "date_col": date_col,
        "lat_col": lat_col,
        "lon_col": lon_col,
        "geohash_col": geohash_col,
        "density_col": density_col,
        "avg_speed_col": avg_speed_col,
        "vehicle_count_col": vehicle_count_col
    }


# ============================================================
# 6) İLÇE SINIRLARINI OKUMA
# ============================================================

def read_districts():
    """
    İstanbul ilçe sınırlarını okur.
    """

    print("\n[4] İlçe sınırları okunuyor...")

    districts = gpd.read_file(DISTRICT_GEOJSON_PATH)

    district_name_col = find_column(
        districts.columns,
        [
            "name",
            "ilce",
            "ilçe",
            "district",
            "district_name",
            "adi",
            "ad",
            "ilce_adi"
        ]
    )

    if district_name_col is None:
        raise RuntimeError(
            f"GeoJSON içinde ilçe adı kolonu bulunamadı: {list(districts.columns)}"
        )

    districts = districts.rename(columns={district_name_col: "district"})

    if districts.crs is None:
        districts = districts.set_crs(epsg=4326)

    districts = districts.to_crs(epsg=4326)

    print(f"İlçe sayısı: {len(districts)}")
    print(f"İlçe adı kolonu: district")

    return districts[["district", "geometry"]]


# ============================================================
# 7) CSV DOSYALARINI BULMA
# ============================================================

def find_csv_files():
    """
    Açılan klasördeki bütün CSV dosyalarını bulur.
    """

    print("\n[5] CSV dosyaları aranıyor...")

    csv_files = sorted(EXTRACT_DIR.rglob("*.csv"))

    if not csv_files:
        raise FileNotFoundError("CSV dosyası bulunamadı.")

    print(f"Bulunan CSV sayısı: {len(csv_files)}")

    for f in csv_files[:10]:
        size_mb = f.stat().st_size / (1024 * 1024)
        print(f"- {f.name} / {size_mb:.2f} MB")

    if len(csv_files) > 10:
        print("...")

    return csv_files


# ============================================================
# 8) GEOHASH ÇÖZME
# ============================================================

def decode_geohash_to_coordinates(df, geohash_col):
    """
    Geohash varsa latitude / longitude üretir.
    """

    if pgh is None:
        raise RuntimeError(
            "pygeohash kurulu değil.\n"
            "Kurmak için:\n"
            "pip install pygeohash"
        )

    def safe_decode_lat(gh):
        try:
            return pgh.decode(str(gh))[0]
        except Exception:
            return np.nan

    def safe_decode_lon(gh):
        try:
            return pgh.decode(str(gh))[1]
        except Exception:
            return np.nan

    df["latitude_generated"] = df[geohash_col].apply(safe_decode_lat)
    df["longitude_generated"] = df[geohash_col].apply(safe_decode_lon)

    return df, "latitude_generated", "longitude_generated"


# ============================================================
# 9) TEK CHUNK İŞLEME
# ============================================================

def process_chunk(chunk, columns_info, districts):
    """
    Bir CSV parçasını temizler, ilçe eşleştirmesi yapar,
    günlük-ilçe seviyesinde ara aggregate üretir.
    """

    date_col = columns_info["date_col"]
    lat_col = columns_info["lat_col"]
    lon_col = columns_info["lon_col"]
    geohash_col = columns_info["geohash_col"]
    density_col = columns_info["density_col"]
    avg_speed_col = columns_info["avg_speed_col"]
    vehicle_count_col = columns_info["vehicle_count_col"]

    chunk = chunk.copy()

    # Tarih parse
    chunk[date_col] = pd.to_datetime(chunk[date_col], errors="coerce")
    chunk = chunk.dropna(subset=[date_col]).copy()

    # Sadece hedef yıl
    chunk = chunk[chunk[date_col].dt.year == TARGET_YEAR].copy()

    if chunk.empty:
        return pd.DataFrame()

    # Koordinat yoksa geohash'ten üret
    if lat_col is None or lon_col is None:
        chunk, lat_col, lon_col = decode_geohash_to_coordinates(
            chunk,
            geohash_col
        )

    chunk[lat_col] = pd.to_numeric(chunk[lat_col], errors="coerce")
    chunk[lon_col] = pd.to_numeric(chunk[lon_col], errors="coerce")

    chunk = chunk.dropna(subset=[lat_col, lon_col]).copy()

    # İstanbul çevresi basit filtre
    chunk = chunk[
        (chunk[lat_col] >= 40.0) &
        (chunk[lat_col] <= 42.0) &
        (chunk[lon_col] >= 27.0) &
        (chunk[lon_col] <= 30.5)
    ].copy()

    if chunk.empty:
        return pd.DataFrame()

    geometry = [
        Point(xy)
        for xy in zip(chunk[lon_col], chunk[lat_col])
    ]

    traffic_gdf = gpd.GeoDataFrame(
        chunk,
        geometry=geometry,
        crs="EPSG:4326"
    )

    joined = gpd.sjoin(
        traffic_gdf,
        districts,
        how="left",
        predicate="within"
    )

    joined = joined[joined["district"].notna()].copy()

    if joined.empty:
        return pd.DataFrame()

    joined["date"] = joined[date_col].dt.date

    # Hedef değer
    if density_col is not None:
        joined["traffic_target"] = pd.to_numeric(
            joined[density_col],
            errors="coerce"
        )
    else:
        # Ortalama hız varsa sıkışıklık skoru üretelim.
        # Hız düştükçe skor artsın.
        joined[avg_speed_col] = pd.to_numeric(
            joined[avg_speed_col],
            errors="coerce"
        )

        # Sabit referans hız kullanıyoruz.
        # Böylece her chunk içinde farklı max_speed üretmeyiz.
        # Ortalama hız 0'a yaklaştıkça sıkışıklık skoru yükselir.
        REFERENCE_SPEED = 150
        joined["traffic_target"] = REFERENCE_SPEED - joined[avg_speed_col]

    joined = joined.dropna(subset=["traffic_target"]).copy()

    # Ek kolonlar
    if avg_speed_col is not None:
        joined[avg_speed_col] = pd.to_numeric(
            joined[avg_speed_col],
            errors="coerce"
        )

    if vehicle_count_col is not None:
        joined[vehicle_count_col] = pd.to_numeric(
            joined[vehicle_count_col],
            errors="coerce"
        )

    agg_dict = {
        "traffic_target": ["mean", "max", "min", "std", "count"]
    }

    if avg_speed_col is not None:
        agg_dict[avg_speed_col] = ["mean", "max", "min"]

    if vehicle_count_col is not None:
        agg_dict[vehicle_count_col] = ["sum", "mean"]

    daily_chunk = (
        joined
        .groupby(["date", "district"])
        .agg(agg_dict)
        .reset_index()
    )

    daily_chunk.columns = [
        "_".join(col).strip("_")
        if isinstance(col, tuple)
        else col
        for col in daily_chunk.columns
    ]

    return daily_chunk


# ============================================================
# 10) TÜM CSV'LERİ CHUNK CHUNK İŞLEME
# ============================================================

def build_yearly_daily_dataset(csv_files, districts):
    """
    Tüm CSV dosyalarını okuyup hedef yıl için günlük-ilçe veri üretir.
    """

    print("\n[6] Yıllık veri seti oluşturuluyor...")

    # İlk CSV'den kolonları algıla
    sample = pd.read_csv(csv_files[0], nrows=1000)
    columns_info = detect_columns_from_sample(sample)

    temp_outputs = []

    chunk_size = 150_000
    part_no = 0

    for csv_index, csv_file in enumerate(csv_files, start=1):
        print(f"\nCSV işleniyor [{csv_index}/{len(csv_files)}]: {csv_file.name}")

        try:
            reader = pd.read_csv(
                csv_file,
                chunksize=chunk_size,
                low_memory=False
            )
        except UnicodeDecodeError:
            reader = pd.read_csv(
                csv_file,
                chunksize=chunk_size,
                low_memory=False,
                encoding="latin1"
            )

        for chunk_index, chunk in enumerate(reader, start=1):
            daily_chunk = process_chunk(
                chunk=chunk,
                columns_info=columns_info,
                districts=districts
            )

            if daily_chunk.empty:
                continue

            part_no += 1

            temp_path = TEMP_DIR / f"daily_part_{part_no:05d}.csv"

            daily_chunk.to_csv(
                temp_path,
                index=False,
                encoding="utf-8-sig"
            )

            temp_outputs.append(temp_path)

            if part_no % 10 == 0:
                print(f"Ara çıktı sayısı: {part_no}")

    if not temp_outputs:
        raise RuntimeError(
            f"{TARGET_YEAR} yılı için işlenebilir kayıt bulunamadı."
        )

    print("\n[7] Ara günlük çıktılar birleştiriliyor...")

    all_parts = []

    for path in temp_outputs:
        part = pd.read_csv(path)
        all_parts.append(part)

    combined = pd.concat(all_parts, ignore_index=True)

    # Ara çıktılar da aggregate olduğu için tekrar ağırlıklı birleştirme yapacağız.
    # Ortalama değerleri count ile ağırlıklandırıyoruz.
    combined["weighted_target_sum"] = (
        combined["traffic_target_mean"] *
        combined["traffic_target_count"]
    )

    combined["weighted_max_sum"] = (
        combined["traffic_target_max"] *
        combined["traffic_target_count"]
    )

    combined["weighted_min_sum"] = (
        combined["traffic_target_min"] *
        combined["traffic_target_count"]
    )

    final = (
        combined
        .groupby(["date", "district"])
        .agg(
            target_weighted_sum=("weighted_target_sum", "sum"),
            record_count=("traffic_target_count", "sum"),
            max_traffic_value=("traffic_target_max", "max"),
            min_traffic_value=("traffic_target_min", "min"),
        )
        .reset_index()
    )

    final["avg_congestion_score"] = (
        final["target_weighted_sum"] /
        final["record_count"]
    )

    final = final.drop(columns=["target_weighted_sum"])

    # Tarih özellikleri
    final["date"] = pd.to_datetime(final["date"])
    final["year"] = final["date"].dt.year
    final["month"] = final["date"].dt.month
    final["day"] = final["date"].dt.day
    final["day_of_week"] = final["date"].dt.dayofweek
    final["is_weekend"] = final["day_of_week"].isin([5, 6]).astype(int)

    final = final[
        [
            "date",
            "district",
            "avg_congestion_score",
            "max_traffic_value",
            "min_traffic_value",
            "record_count",
            "year",
            "month",
            "day",
            "day_of_week",
            "is_weekend"
        ]
    ].copy()

    final = final.sort_values(["date", "district"]).reset_index(drop=True)

    return final


# ============================================================
# 11) ANA AKIŞ
# ============================================================

def main():
    print("İstanbul 1 Yıllık İlçe Bazlı Trafik Veri Seti Oluşturma")
    print("=" * 70)
    print(f"Hedef yıl: {TARGET_YEAR}")

    zip_path = download_kaggle_dataset()

    extract_dataset(zip_path)

    download_district_geojson()

    districts = read_districts()

    csv_files = find_csv_files()

    final_df = build_yearly_daily_dataset(
        csv_files=csv_files,
        districts=districts
    )

    final_df.to_csv(
        FINAL_DATASET_PATH,
        index=False,
        encoding="utf-8-sig"
    )

    print("\nİşlem tamamlandı.")
    print(f"Final veri seti kaydedildi:")
    print(FINAL_DATASET_PATH)

    print("\nVeri özeti:")
    print(f"Satır sayısı : {len(final_df):,}")
    print(f"İlçe sayısı  : {final_df['district'].nunique()}")
    print(f"Tarih aralığı: {final_df['date'].min().date()} - {final_df['date'].max().date()}")
    print(f"Aylar        : {sorted(final_df['month'].unique().tolist())}")

    print("\nİlk 10 satır:")
    print(final_df.head(10))


if __name__ == "__main__":
    main()