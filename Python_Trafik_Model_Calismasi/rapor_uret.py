from pathlib import Path
from datetime import datetime

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.platypus import (
    SimpleDocTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
    Image,
    PageBreak
)
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont


# ============================================================
# 1) DOSYA YOLLARI
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

DATA_DIR = BASE_DIR / "data" / "processed"
OUTPUT_DIR = BASE_DIR / "outputs"
OUTPUT_DIR.mkdir(exist_ok=True)

CLEAN_DATA_PATH = DATA_DIR / "istanbul_ilce_gunluk_trafik_2024_clean.csv"
MODEL_READY_PATH = DATA_DIR / "istanbul_ilce_gunluk_trafik_model_ready.csv"

MODEL_RESULTS_PATH = OUTPUT_DIR / "model_results.csv"
FEATURE_IMPORTANCE_PATH = OUTPUT_DIR / "feature_importance.csv"
PREDICTIONS_PATH = OUTPUT_DIR / "predictions.csv"

ACTUAL_VS_PREDICTED_PNG = OUTPUT_DIR / "actual_vs_predicted.png"
TARGET_DISTRIBUTION_PNG = OUTPUT_DIR / "target_distribution.png"
DISTRICT_AVG_TRAFFIC_PNG = OUTPUT_DIR / "district_avg_traffic.png"

MODEL_COMPARISON_PNG = OUTPUT_DIR / "model_comparison_for_report.png"
FEATURE_IMPORTANCE_PNG = OUTPUT_DIR / "feature_importance_for_report.png"
PREDICTION_ERROR_PNG = OUTPUT_DIR / "prediction_error_for_report.png"

PDF_OUTPUT_PATH = OUTPUT_DIR / "trafik_model_raporu.pdf"


# ============================================================
# 2) TÜRKÇE KARAKTER DESTEKLİ FONT
# ============================================================

def register_turkish_font():
    """
    ReportLab varsayılan fontları Türkçe karakterlerde sorun çıkarabilir.
    Bu yüzden Windows Arial veya DejaVu Sans fontunu kullanmaya çalışıyoruz.
    """

    possible_fonts = [
        Path("C:/Windows/Fonts/arial.ttf"),
        Path("C:/Windows/Fonts/Calibri.ttf"),
        Path("C:/Windows/Fonts/segoeui.ttf"),
        Path("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
        Path("/usr/local/share/fonts/DejaVuSans.ttf"),
    ]

    for font_path in possible_fonts:
        if font_path.exists():
            pdfmetrics.registerFont(TTFont("TRFont", str(font_path)))
            return "TRFont"

    # Hiçbiri bulunamazsa Helvetica kullanılır.
    # Türkçe karakterlerde bozulma olabilir.
    return "Helvetica"


FONT_NAME = register_turkish_font()


# ============================================================
# 3) PDF STİLLERİ
# ============================================================

def get_styles():
    styles = getSampleStyleSheet()

    styles.add(
        ParagraphStyle(
            name="ReportTitle",
            parent=styles["Title"],
            fontName=FONT_NAME,
            fontSize=20,
            leading=26,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#1F2937"),
            spaceAfter=18,
        )
    )

    styles.add(
        ParagraphStyle(
            name="SectionTitle",
            parent=styles["Heading1"],
            fontName=FONT_NAME,
            fontSize=14,
            leading=18,
            textColor=colors.HexColor("#111827"),
            spaceBefore=12,
            spaceAfter=8,
        )
    )

    styles.add(
        ParagraphStyle(
            name="SubTitle",
            parent=styles["Heading2"],
            fontName=FONT_NAME,
            fontSize=11,
            leading=14,
            textColor=colors.HexColor("#374151"),
            spaceBefore=8,
            spaceAfter=6,
        )
    )

    styles.add(
        ParagraphStyle(
            name="BodyTR",
            parent=styles["BodyText"],
            fontName=FONT_NAME,
            fontSize=9,
            leading=13,
            alignment=TA_LEFT,
            textColor=colors.HexColor("#1F2937"),
            spaceAfter=6,
        )
    )

    styles.add(
        ParagraphStyle(
            name="SmallTR",
            parent=styles["BodyText"],
            fontName=FONT_NAME,
            fontSize=8,
            leading=11,
            textColor=colors.HexColor("#374151"),
        )
    )

    return styles


# ============================================================
# 4) VERİ OKUMA
# ============================================================

def safe_read_csv(path):
    """
    Dosya varsa okur, yoksa None döndürür.
    """

    if not path.exists():
        print(f"Uyarı: Dosya bulunamadı: {path}")
        return None

    return pd.read_csv(path)


def load_project_data():
    clean_df = safe_read_csv(CLEAN_DATA_PATH)
    model_df = safe_read_csv(MODEL_READY_PATH)
    model_results_df = safe_read_csv(MODEL_RESULTS_PATH)
    feature_importance_df = safe_read_csv(FEATURE_IMPORTANCE_PATH)
    predictions_df = safe_read_csv(PREDICTIONS_PATH)

    if clean_df is not None and "date" in clean_df.columns:
        clean_df["date"] = pd.to_datetime(clean_df["date"], errors="coerce")

    if model_df is not None and "date" in model_df.columns:
        model_df["date"] = pd.to_datetime(model_df["date"], errors="coerce")

    if predictions_df is not None and "date" in predictions_df.columns:
        predictions_df["date"] = pd.to_datetime(predictions_df["date"], errors="coerce")

    return {
        "clean_df": clean_df,
        "model_df": model_df,
        "model_results_df": model_results_df,
        "feature_importance_df": feature_importance_df,
        "predictions_df": predictions_df,
    }


# ============================================================
# 5) KOLON AÇIKLAMALARI
# ============================================================

COLUMN_DESCRIPTIONS = {
    "date": "Trafik kaydının ait olduğu gün. Modelde doğrudan kullanılmadı; tarihsel özellikler üretmek için kullanıldı.",
    "district": "İstanbul ilçesi. Modelde kategorik değişken olarak kullanıldı.",
    "avg_congestion_score": "Hedef değişken. Ortalama hızdan türetilen günlük ilçe bazlı trafik sıkışıklığı skorudur.",
    "max_traffic_value": "Aynı güne ait maksimum trafik değeri. Veri sızıntısı riski nedeniyle modelden çıkarıldı.",
    "min_traffic_value": "Aynı güne ait minimum trafik değeri. Veri sızıntısı riski nedeniyle modelden çıkarıldı.",
    "record_count": "O gün ve ilçe için kullanılan trafik kayıt sayısı. Modelden çıkarıldı.",
    "year": "Yıl bilgisi. Veri yalnızca 2024 olduğu için tek değerli kaldı ve çıkarıldı.",
    "month": "Ay bilgisi. Mevsimsel ve dönemsel trafik değişimini yakalamak için kullanıldı.",
    "day": "Ayın günü. Gün içi/takvimsel örüntüler için kullanıldı.",
    "day_of_week": "Haftanın günü. Pazartesi=0, Pazar=6. Haftalık trafik ritmini yakalamak için kullanıldı.",
    "is_weekend": "Hafta sonu bilgisi. Cumartesi/Pazar için 1, diğer günler için 0.",
    "week_of_year": "Yılın haftası. Yıl içindeki dönemsel değişimleri temsil eder.",
    "quarter": "Yılın çeyreği. Sezonsal trafik farklarını temsil eder.",
    "month_sin": "Ay bilgisinin döngüsel sinüs dönüşümü. Aralık-Ocak yakınlığını daha doğru temsil eder.",
    "month_cos": "Ay bilgisinin döngüsel kosinüs dönüşümü.",
    "day_of_week_sin": "Haftanın günü bilgisinin döngüsel sinüs dönüşümü.",
    "day_of_week_cos": "Haftanın günü bilgisinin döngüsel kosinüs dönüşümü.",
    "is_holiday": "Türkiye resmi tatil bilgisi. Tatil günlerinde trafik davranışı değişebileceği için kullanıldı.",
    "lag_1": "Aynı ilçenin bir önceki günkü trafik skoru.",
    "lag_2": "Aynı ilçenin iki gün önceki trafik skoru.",
    "lag_3": "Aynı ilçenin üç gün önceki trafik skoru.",
    "lag_7": "Aynı ilçenin bir hafta önceki trafik skoru.",
    "rolling_3_mean": "Aynı ilçenin son 3 günlük ortalama trafik skoru.",
    "rolling_7_mean": "Aynı ilçenin son 7 günlük ortalama trafik skoru.",
    "rolling_3_std": "Aynı ilçenin son 3 günlük trafik oynaklığı.",
    "rolling_7_std": "Aynı ilçenin son 7 günlük trafik oynaklığı.",
}


# ============================================================
# 6) GRAFİK ÜRETİMİ
# ============================================================

def create_model_comparison_plot(model_results_df):
    if model_results_df is None or model_results_df.empty:
        return None

    if "model" not in model_results_df.columns or "mae" not in model_results_df.columns:
        return None

    df = model_results_df.sort_values("mae").copy()

    plt.figure(figsize=(8, 4.5))
    plt.bar(df["model"], df["mae"])
    plt.title("Model Karşılaştırması - MAE")
    plt.xlabel("Model")
    plt.ylabel("MAE")
    plt.xticks(rotation=20, ha="right")
    plt.tight_layout()
    plt.savefig(MODEL_COMPARISON_PNG, dpi=160)
    plt.close()

    return MODEL_COMPARISON_PNG


def create_feature_importance_plot(feature_importance_df):
    if feature_importance_df is None or feature_importance_df.empty:
        return None

    if "feature" not in feature_importance_df.columns or "importance" not in feature_importance_df.columns:
        return None

    df = feature_importance_df.sort_values("importance", ascending=False).head(15).copy()

    plt.figure(figsize=(8, 5))
    plt.barh(df["feature"][::-1], df["importance"][::-1])
    plt.title("En Önemli 15 Özellik")
    plt.xlabel("Önem Skoru")
    plt.tight_layout()
    plt.savefig(FEATURE_IMPORTANCE_PNG, dpi=160)
    plt.close()

    return FEATURE_IMPORTANCE_PNG


def create_prediction_error_plot(predictions_df):
    if predictions_df is None or predictions_df.empty:
        return None

    if "abs_error" not in predictions_df.columns:
        return None

    plt.figure(figsize=(8, 4.5))
    plt.hist(predictions_df["abs_error"].dropna(), bins=35)
    plt.title("Test Seti Mutlak Hata Dağılımı")
    plt.xlabel("Mutlak Hata")
    plt.ylabel("Frekans")
    plt.tight_layout()
    plt.savefig(PREDICTION_ERROR_PNG, dpi=160)
    plt.close()

    return PREDICTION_ERROR_PNG


# ============================================================
# 7) TABLO YARDIMCILARI
# ============================================================

def make_table(data, col_widths=None, font_size=8):
    table = Table(data, colWidths=col_widths, repeatRows=1)

    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E5E7EB")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.HexColor("#111827")),
                ("FONTNAME", (0, 0), (-1, -1), FONT_NAME),
                ("FONTSIZE", (0, 0), (-1, -1), font_size),
                ("ALIGN", (0, 0), (-1, 0), "CENTER"),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("GRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#D1D5DB")),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F9FAFB")]),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
                ("TOPPADDING", (0, 0), (-1, -1), 4),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
            ]
        )
    )

    return table


def add_image_if_exists(story, image_path, width_cm=16):
    if image_path is None:
        return

    image_path = Path(image_path)

    if not image_path.exists():
        return

    img = Image(str(image_path))
    img._restrictSize(width_cm * cm, 12 * cm)
    story.append(img)
    story.append(Spacer(1, 0.35 * cm))


# ============================================================
# 8) RAPOR BÖLÜMLERİ
# ============================================================

def add_cover(story, styles):
    story.append(Paragraph("İlçe Bazlı Trafik Tahmin Modeli Raporu", styles["ReportTitle"]))

    story.append(
        Paragraph(
            "Bu rapor, İstanbul ilçeleri için günlük trafik sıkışıklığı tahmini amacıyla "
            "hazırlanan veri setini, model öncesi veri inceleme sürecini, kullanılan sütunları, "
            "model karşılaştırmalarını, hata analizini ve grafik sonuçlarını özetler.",
            styles["BodyTR"]
        )
    )

    story.append(Spacer(1, 0.4 * cm))

    meta_data = [
        ["Rapor tarihi", datetime.now().strftime("%d.%m.%Y %H:%M")],
        ["Proje tipi", "Makine öğrenmesi tabanlı trafik tahmini ve karar destek sistemi"],
        ["Hedef", "İlçe ve gün bazlı trafik sıkışıklığı tahmini"],
        ["Kullanım amacı", "Yol çalışması planlaması için uygun gün seçimi ve senaryo analizi"],
    ]

    story.append(make_table(meta_data, col_widths=[5 * cm, 11 * cm], font_size=9))
    story.append(PageBreak())


def add_dataset_summary(story, styles, clean_df, model_df):
    story.append(Paragraph("1. Veri Seti Özeti", styles["SectionTitle"]))

    if clean_df is None:
        story.append(Paragraph("Temiz yıllık veri seti bulunamadı.", styles["BodyTR"]))
        return

    date_min = clean_df["date"].min().date() if "date" in clean_df.columns else "-"
    date_max = clean_df["date"].max().date() if "date" in clean_df.columns else "-"

    district_count = clean_df["district"].nunique() if "district" in clean_df.columns else "-"
    row_count = len(clean_df)
    col_count = clean_df.shape[1]

    model_row_count = len(model_df) if model_df is not None else "-"
    model_col_count = model_df.shape[1] if model_df is not None else "-"

    data = [
        ["Ölçüt", "Değer"],
        ["Temiz veri satır sayısı", f"{row_count:,}"],
        ["Temiz veri sütun sayısı", str(col_count)],
        ["Model-ready satır sayısı", f"{model_row_count:,}" if isinstance(model_row_count, int) else str(model_row_count)],
        ["Model-ready sütun sayısı", str(model_col_count)],
        ["İlçe sayısı", str(district_count)],
        ["Tarih aralığı", f"{date_min} - {date_max}"],
    ]

    story.append(make_table(data, col_widths=[6 * cm, 10 * cm], font_size=9))
    story.append(Spacer(1, 0.4 * cm))

    story.append(
        Paragraph(
            "Veri seti, ham trafik kayıtlarının ilçe ve gün seviyesinde gruplanmasıyla oluşturuldu. "
            "Model-ready veri setinde aynı güne ait maksimum, minimum ve kayıt sayısı gibi tahmin anında "
            "bilinmeyecek sütunlar çıkarıldı. Bunun yerine geçmiş günlere ait lag ve rolling özellikleri üretildi.",
            styles["BodyTR"]
        )
    )


def add_column_explanations(story, styles, model_df):
    story.append(Paragraph("2. Kullanılan Sütunlar ve Açıklamaları", styles["SectionTitle"]))

    if model_df is None:
        story.append(Paragraph("Model-ready veri seti bulunamadı.", styles["BodyTR"]))
        return

    table_data = [["Sütun", "Açıklama"]]

    for col in model_df.columns:
        desc = COLUMN_DESCRIPTIONS.get(col, "Bu sütun için açıklama tanımlanmamış.")
        table_data.append([col, desc])

    story.append(make_table(table_data, col_widths=[4.2 * cm, 12.2 * cm], font_size=7))
    story.append(Spacer(1, 0.3 * cm))


def add_feature_importance_section(story, styles, feature_importance_df, feature_plot_path):
    story.append(PageBreak())
    story.append(Paragraph("3. Özelliklerin Modele Etkisi", styles["SectionTitle"]))

    if feature_importance_df is None or feature_importance_df.empty:
        story.append(Paragraph("Feature importance dosyası bulunamadı.", styles["BodyTR"]))
        return

    top_df = feature_importance_df.sort_values("importance", ascending=False).head(15)

    table_data = [["Sıra", "Özellik", "Önem Skoru"]]

    for i, row in enumerate(top_df.itertuples(index=False), start=1):
        table_data.append(
            [
                str(i),
                str(row.feature),
                f"{float(row.importance):.6f}"
            ]
        )

    story.append(
        Paragraph(
            "Aşağıdaki tablo ve grafik, eğitilen en iyi modelin hangi değişkenlere daha fazla önem verdiğini gösterir. "
            "Projede en yüksek etkinin genellikle son 7 günlük ortalama trafik davranışından geldiği görülmektedir.",
            styles["BodyTR"]
        )
    )

    story.append(make_table(table_data, col_widths=[1.3 * cm, 9.5 * cm, 5.2 * cm], font_size=8))
    story.append(Spacer(1, 0.4 * cm))

    add_image_if_exists(story, feature_plot_path, width_cm=16)


def add_model_results_section(story, styles, model_results_df, model_plot_path):
    story.append(PageBreak())
    story.append(Paragraph("4. Model Karşılaştırma Sonuçları", styles["SectionTitle"]))

    if model_results_df is None or model_results_df.empty:
        story.append(Paragraph("Model sonuçları dosyası bulunamadı.", styles["BodyTR"]))
        return

    df = model_results_df.copy()

    table_data = [["Model", "MAE", "RMSE", "R2"]]

    for row in df.itertuples(index=False):
        table_data.append(
            [
                str(row.model),
                f"{float(row.mae):.4f}",
                f"{float(row.rmse):.4f}",
                f"{float(row.r2):.4f}",
            ]
        )

    story.append(
        Paragraph(
            "Modeller tarih bazlı train/test ayrımı ile değerlendirildi. Aynı tarih hem eğitim hem test "
            "tarafına düşmeyecek şekilde ayrım yapıldı. Bu yaklaşım, trafik tahmini gibi gelecek günleri "
            "öngörmeyi amaçlayan problemlerde rastgele bölmeden daha gerçekçidir.",
            styles["BodyTR"]
        )
    )

    story.append(make_table(table_data, col_widths=[6 * cm, 3.2 * cm, 3.2 * cm, 3.2 * cm], font_size=8))
    story.append(Spacer(1, 0.4 * cm))

    best = df.sort_values("mae").iloc[0]

    story.append(
        Paragraph(
            f"En iyi model MAE değerine göre <b>{best['model']}</b> olarak seçilmiştir. "
            f"Bu modelin MAE değeri {best['mae']:.4f}, RMSE değeri {best['rmse']:.4f}, "
            f"R2 değeri ise {best['r2']:.4f} olarak ölçülmüştür.",
            styles["BodyTR"]
        )
    )

    add_image_if_exists(story, model_plot_path, width_cm=15)


def add_prediction_analysis_section(story, styles, predictions_df, error_plot_path):
    story.append(PageBreak())
    story.append(Paragraph("5. Test Tahminleri ve Hata Analizi", styles["SectionTitle"]))

    if predictions_df is None or predictions_df.empty:
        story.append(Paragraph("Tahmin dosyası bulunamadı.", styles["BodyTR"]))
        return

    mae = predictions_df["abs_error"].mean() if "abs_error" in predictions_df.columns else None
    max_error = predictions_df["abs_error"].max() if "abs_error" in predictions_df.columns else None

    if "date" in predictions_df.columns:
        date_min = predictions_df["date"].min().date()
        date_max = predictions_df["date"].max().date()
    else:
        date_min = "-"
        date_max = "-"

    summary_data = [
        ["Ölçüt", "Değer"],
        ["Test satır sayısı", f"{len(predictions_df):,}"],
        ["Test tarih aralığı", f"{date_min} - {date_max}"],
        ["Ortalama mutlak hata", f"{mae:.4f}" if mae is not None else "-"],
        ["Maksimum mutlak hata", f"{max_error:.4f}" if max_error is not None else "-"],
    ]

    story.append(make_table(summary_data, col_widths=[6 * cm, 10 * cm], font_size=9))
    story.append(Spacer(1, 0.4 * cm))

    sample_cols = ["date", "district", "actual", "predicted", "abs_error"]
    available_cols = [c for c in sample_cols if c in predictions_df.columns]

    sample_df = predictions_df[available_cols].head(10).copy()

    table_data = [available_cols]

    for row in sample_df.itertuples(index=False):
        formatted_row = []
        for value in row:
            if isinstance(value, float):
                formatted_row.append(f"{value:.4f}")
            else:
                formatted_row.append(str(value))
        table_data.append(formatted_row)

    story.append(Paragraph("İlk 10 test tahmini örneği:", styles["SubTitle"]))
    story.append(make_table(table_data, font_size=7))
    story.append(Spacer(1, 0.4 * cm))

    add_image_if_exists(story, error_plot_path, width_cm=15)
    add_image_if_exists(story, ACTUAL_VS_PREDICTED_PNG, width_cm=15)


def add_existing_eda_graphs(story, styles):
    story.append(PageBreak())
    story.append(Paragraph("6. Veri İnceleme Grafikleri", styles["SectionTitle"]))

    story.append(
        Paragraph(
            "Bu bölümde EDA aşamasında üretilen hedef değişken dağılımı ve ilçe bazlı ortalama trafik "
            "grafikleri yer almaktadır. Bu grafikler veri setinin genel davranışını anlamaya yardımcı olur.",
            styles["BodyTR"]
        )
    )

    add_image_if_exists(story, TARGET_DISTRIBUTION_PNG, width_cm=15)
    add_image_if_exists(story, DISTRICT_AVG_TRAFFIC_PNG, width_cm=15)


def add_conclusion(story, styles):
    story.append(PageBreak())
    story.append(Paragraph("7. Genel Değerlendirme", styles["SectionTitle"]))

    paragraphs = [
        "Bu çalışma sonucunda İstanbul ilçeleri için günlük bazda trafik sıkışıklığı tahmini yapabilen bir makine öğrenmesi akışı oluşturulmuştur.",
        "Veri hazırlama aşamasında koordinat bazlı ham trafik kayıtları ilçe ve gün seviyesine indirgenmiş, ardından model için uygun tarihsel ve geçmiş trafik özellikleri üretilmiştir.",
        "Model doğruluğunu yapay şekilde artırabilecek aynı güne ait maksimum, minimum ve kayıt sayısı gibi sütunlar modelden çıkarılmıştır.",
        "Model karşılaştırmasında Random Forest, Extra Trees, Gradient Boosting ve Ridge Regression modelleri test edilmiştir.",
        "En iyi model, daha sonra senaryo tabanlı karar destek uygulamasında kullanılmak üzere joblib formatında kaydedilmiştir.",
        "Uygulama tarafında yol çalışması, kapalı şerit, yağmur, etkinlik, okul günü, kaza riski ve toplu taşıma aksaması gibi parametreler kural tabanlı senaryo katmanı ile modele eklenmiştir.",
        "Bu yapı doğrudan bir trafik yönetim karar destek sistemi olarak kullanılabilir. Daha güçlü sonuçlar için ileride 2025 ve 2026 trafik verileri, hava durumu kayıtları, kaza verileri, etkinlik takvimleri ve gerçek yol çalışması kayıtları da modele dahil edilebilir.",
    ]

    for p in paragraphs:
        story.append(Paragraph(p, styles["BodyTR"]))


# ============================================================
# 9) SAYFA NUMARASI
# ============================================================

def add_page_number(canvas, doc):
    canvas.saveState()
    canvas.setFont(FONT_NAME, 8)
    page_text = f"Sayfa {doc.page}"
    canvas.drawRightString(20 * cm, 1.2 * cm, page_text)
    canvas.restoreState()


# ============================================================
# 10) PDF ÜRETİMİ
# ============================================================

def generate_pdf():
    data = load_project_data()

    clean_df = data["clean_df"]
    model_df = data["model_df"]
    model_results_df = data["model_results_df"]
    feature_importance_df = data["feature_importance_df"]
    predictions_df = data["predictions_df"]

    # Rapor için ek grafikler üret
    model_plot_path = create_model_comparison_plot(model_results_df)
    feature_plot_path = create_feature_importance_plot(feature_importance_df)
    error_plot_path = create_prediction_error_plot(predictions_df)

    styles = get_styles()

    doc = SimpleDocTemplate(
        str(PDF_OUTPUT_PATH),
        pagesize=A4,
        rightMargin=1.5 * cm,
        leftMargin=1.5 * cm,
        topMargin=1.5 * cm,
        bottomMargin=1.5 * cm,
    )

    story = []

    add_cover(story, styles)
    add_dataset_summary(story, styles, clean_df, model_df)
    add_column_explanations(story, styles, model_df)
    add_feature_importance_section(story, styles, feature_importance_df, feature_plot_path)
    add_model_results_section(story, styles, model_results_df, model_plot_path)
    add_prediction_analysis_section(story, styles, predictions_df, error_plot_path)
    add_existing_eda_graphs(story, styles)
    add_conclusion(story, styles)

    doc.build(
        story,
        onFirstPage=add_page_number,
        onLaterPages=add_page_number,
    )

    print("\nPDF raporu başarıyla oluşturuldu:")
    print(PDF_OUTPUT_PATH)


if __name__ == "__main__":
    generate_pdf()