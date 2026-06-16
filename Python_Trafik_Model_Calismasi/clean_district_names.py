from pathlib import Path
import pandas as pd


BASE_DIR = Path(__file__).resolve().parent

INPUT_PATH = BASE_DIR / "data" / "processed" / "istanbul_ilce_gunluk_trafik_2024.csv"

OUTPUT_PATH = BASE_DIR / "data" / "processed" / "istanbul_ilce_gunluk_trafik_2024_clean.csv"


def clean_district_name(value):
    """
    District değeri şu formda geliyor:
    'Kadıköy, İstanbul, Marmara Bölgesi, Türkiye'

    Biz sadece ilk parçayı alıyoruz:
    'Kadıköy'
    """

    if pd.isna(value):
        return value

    text = str(value).strip()

    # Virgülden önceki ilk parça ilçe adı.
    district = text.split(",")[0].strip()

    return district


def main():
    print("İlçe isimleri temizleniyor...")

    df = pd.read_csv(INPUT_PATH)

    print("\nEski district örnekleri:")
    print(df["district"].dropna().unique()[:10])

    df["district"] = df["district"].apply(clean_district_name)

    print("\nTemizlenmiş district örnekleri:")
    print(df["district"].dropna().unique()[:10])

    print("\nİlçe sayısı:", df["district"].nunique())

    print("\nİlçeler:")
    for district in sorted(df["district"].dropna().unique()):
        print("-", district)

    df.to_csv(
        OUTPUT_PATH,
        index=False,
        encoding="utf-8-sig"
    )

    print("\nTemiz veri kaydedildi:")
    print(OUTPUT_PATH)


if __name__ == "__main__":
    main()