from pathlib import Path
import ast
import json
import pandas as pd
import geopandas as gpd


BASE_DIR = Path(__file__).resolve().parent

TRAFFIC_PATH = BASE_DIR / "data" / "processed" / "istanbul_ilce_gunluk_trafik_2024.csv"
GEOJSON_PATH = BASE_DIR / "data" / "geo" / "ilce_geojson.json"


def normalize_name(value):
    """
    İlçe isimlerini karşılaştırmak için sadeleştirir.
    Büyük/küçük harf ve boşluk farklarını azaltır.
    """

    if pd.isna(value):
        return ""

    text = str(value).strip().upper()

    # Türkçe karakterleri sadeleştirelim.
    replacements = {
        "İ": "I",
        "İ": "I",
        "Ş": "S",
        "Ğ": "G",
        "Ü": "U",
        "Ö": "O",
        "Ç": "C",
    }

    for old, new in replacements.items():
        text = text.replace(old, new)

    return text


def parse_address_value(address_value):
    """
    GeoJSON içindeki address kolonu bazen dict,
    bazen string gibi gelebilir.

    Bu fonksiyon address içinden ilçe adını çıkarmaya çalışır.
    """

    if pd.isna(address_value):
        return None

    address_obj = None

    # Eğer zaten dict ise doğrudan kullan.
    if isinstance(address_value, dict):
        address_obj = address_value

    # Eğer string ise dict'e çevirmeyi dene.
    elif isinstance(address_value, str):
        text = address_value.strip()

        try:
            address_obj = json.loads(text)
        except Exception:
            try:
                address_obj = ast.literal_eval(text)
            except Exception:
                address_obj = None

    if not isinstance(address_obj, dict):
        return None

    # OSM/Nominatim çıktılarında ilçe farklı anahtarlarla gelebilir.
    possible_keys = [
        "city_district",
        "town",
        "county",
        "municipality",
        "suburb",
        "district",
        "city"
    ]

    for key in possible_keys:
        value = address_obj.get(key)
        if value:
            return str(value).strip()

    return None


def parse_display_name(display_name):
    """
    display_name genelde şöyle olur:
    'Kadıköy, İstanbul, Marmara Bölgesi, Türkiye'

    İlk parçayı ilçe adı olarak almayı deniyoruz.
    """

    if pd.isna(display_name):
        return None

    parts = str(display_name).split(",")

    if not parts:
        return None

    first_part = parts[0].strip()

    if not first_part:
        return None

    return first_part


def extract_geojson_district(row):
    """
    GeoJSON satırından ilçe adını çıkarır.
    Önce address kolonuna bakar.
    Olmazsa display_name kullanır.
    """

    district_from_address = None

    if "address" in row:
        district_from_address = parse_address_value(row["address"])

    if district_from_address:
        return district_from_address

    if "display_name" in row:
        return parse_display_name(row["display_name"])

    return None


def main():
    df = pd.read_csv(TRAFFIC_PATH)
    geo = gpd.read_file(GEOJSON_PATH)

    print("Trafik verisindeki kolonlar:")
    print(df.columns.tolist())

    print("\nGeoJSON kolonları:")
    print(geo.columns.tolist())

    print("\nTrafik verisindeki ilk ilçe değerleri:")
    print(df["district"].dropna().astype(str).unique()[:20])

    # GeoJSON içinden ilçe adlarını çıkar.
    geo["district_extracted"] = geo.apply(extract_geojson_district, axis=1)

    print("\nGeoJSON içinden çıkarılan ilk ilçe değerleri:")
    print(geo["district_extracted"].dropna().astype(str).unique()[:20])

    traffic_districts_raw = sorted(df["district"].dropna().astype(str).unique().tolist())
    geo_districts_raw = sorted(geo["district_extracted"].dropna().astype(str).unique().tolist())

    traffic_districts_norm = set(normalize_name(x) for x in traffic_districts_raw)
    geo_districts_norm = set(normalize_name(x) for x in geo_districts_raw)

    missing_in_traffic = sorted(geo_districts_norm - traffic_districts_norm)
    extra_in_traffic = sorted(traffic_districts_norm - geo_districts_norm)

    print("\nTrafik verisindeki ilçe sayısı:", len(traffic_districts_norm))
    print("GeoJSON ilçe sayısı:", len(geo_districts_norm))

    print("\nTrafik verisindeki ilçeler:")
    for d in traffic_districts_raw:
        print("-", d)

    print("\nGeoJSON içinden çıkarılan ilçeler:")
    for d in geo_districts_raw:
        print("-", d)

    print("\nGeoJSON'da olup trafik verisinde olmayan normalize ilçeler:")
    if missing_in_traffic:
        for d in missing_in_traffic:
            print("-", d)
    else:
        print("Yok")

    print("\nTrafik verisinde olup GeoJSON'da birebir eşleşmeyen normalize ilçeler:")
    if extra_in_traffic:
        for d in extra_in_traffic:
            print("-", d)
    else:
        print("Yok")


if __name__ == "__main__":
    main()