# -*- mode: python ; coding: utf-8 -*-

from pathlib import Path

from PyInstaller.utils.hooks import (
    collect_data_files,
    collect_submodules,
    copy_metadata
)

import streamlit


project_dir = Path.cwd()

datas = []
binaries = []
hiddenimports = []


# ============================================================
# 1) Streamlit veri dosyaları ve metadata
# ============================================================
# Streamlit EXE içinde çalışırken static/frontend dosyalarına ve
# package metadata bilgisine ihtiyaç duyar.
# Metadata olmazsa:
# importlib.metadata.PackageNotFoundError: streamlit
# hatası alınabilir.
# ============================================================

datas += collect_data_files("streamlit", include_py_files=False)

# Streamlit static klasörünü ayrıca garantiye alıyoruz.
streamlit_dir = Path(streamlit.__file__).resolve().parent

datas += [
      (
        str(streamlit_dir / "static"),
        "streamlit/static"
    ),
    (
        str(streamlit_dir / "web"),
        "streamlit/web"
    )
]

# Paket metadata bilgileri
metadata_packages = [
    "streamlit",
    "altair",
    "pandas",
    "numpy",
    "scikit-learn",
    "joblib",
    "matplotlib",
    "holidays",
    "pyarrow",
    "tornado",
    "jsonschema",
    "watchdog",
    "blinker",
    "protobuf",
    "requests",
    "packaging",
    "tenacity",
    "click",
    "jinja2",
    "tzdata"
    "pydeck",
    "gitpython",
    "cachetools",
    "typing-extensions",
    "starlette",
    "uvicorn",
    "websockets",
]

for package_name in metadata_packages:
    try:
        datas += copy_metadata(package_name)
    except Exception:
        pass


# ============================================================
# 2) Gerekli hidden importlar
# ============================================================
# Bazı paketler modülleri dinamik import ettiği için PyInstaller
# bunları otomatik göremeyebilir. Burada açıkça ekliyoruz.
# ============================================================

hiddenimports += [
    "importlib.metadata",

    # Streamlit
    "streamlit",
    "streamlit.version",
    "streamlit.web",
    "streamlit.web.bootstrap",
    "streamlit.runtime",
    "streamlit.runtime.scriptrunner",
    "streamlit.runtime.state",
    "streamlit.runtime.caching",
    "streamlit.components.v1",

    # Veri / model
    "pandas",
    "numpy",
    "joblib",

    # Scikit-learn
    "sklearn",
    "sklearn.ensemble",
    "sklearn.ensemble._forest",
    "sklearn.tree",
    "sklearn.tree._tree",
    "sklearn.tree._utils",
    "sklearn.compose",
    "sklearn.compose._column_transformer",
    "sklearn.preprocessing",
    "sklearn.preprocessing._encoders",
    "sklearn.pipeline",
    "sklearn.metrics",
    "sklearn.utils",
    "sklearn.utils._cython_blas",
    "sklearn.neighbors._typedefs",
    "sklearn.neighbors._quad_tree",

    # Grafik
    "matplotlib",
    "matplotlib.pyplot",
    "matplotlib.backends.backend_agg",

    # Diğerleri
    "holidays",
    "altair",
    "pyarrow",
    "tornado",
    "watchdog",
    "blinker",
    "click",
    "jinja2",
    "jsonschema",
    "packaging",
    "protobuf",
    "requests",
    "tenacity",
    "toml",
    "tzdata",
]


# sklearn alt modüllerini alıyoruz ama test modüllerini hariç tutuyoruz.
try:
    hiddenimports += collect_submodules(
        "sklearn",
        filter=lambda name: ".tests" not in name and ".test_" not in name
    )
except Exception:
    pass


# ============================================================
# 3) Kendi uygulama dosyalarımız
# ============================================================
# DİKKAT:
# data klasörünün tamamını EXE içine almıyoruz.
# Çünkü data/raw içinde 6GB+ ham CSV vardı.
# Sadece uygulamanın ihtiyaç duyduğu hazır CSV dosyasını ekliyoruz.
# ============================================================

datas += [
    # Streamlit uygulaması
    (
        str(project_dir / "app.py"),
        "."
    ),

    # Streamlit config
    (
        str(project_dir / ".streamlit" / "config.toml"),
        ".streamlit"
    ),

    # Eğitilmiş model
    (
        str(project_dir / "models" / "best_traffic_model.joblib"),
        "models"
    ),

    # Model-ready veri seti
    (
        str(project_dir / "data" / "processed" / "istanbul_ilce_gunluk_trafik_model_ready.csv"),
        "data/processed"
    ),
]


# ============================================================
# 4) Gereksiz / büyük paketleri dışarıda bırak
# ============================================================
# Bunlar veri hazırlama aşamasında lazım olabilir ama Streamlit
# tahmin uygulamasında kullanılmıyor.
# ============================================================

excludes = [
    # Test / notebook ortamları
    "pytest",
    "IPython",
    "notebook",
    "jupyter",
    "jupyterlab",

    # Büyük ML/DL paketleri
    "torch",
    "tensorflow",
    "keras",

    # Veri hazırlama aşamasında kullanılan ama EXE için gereksiz paketler
    "geopandas",
    "shapely",
    "pyproj",
    "pyogrio",
    "rtree",
    "kaggle",
    "pygeohash",

    # Test paketleri
    "pandas.tests",
    "numpy.tests",
    "matplotlib.tests",
    "pyarrow.tests",
    "tornado.test",
    "jsonschema.tests",
    "sklearn.tests",

    # GUI frameworkleri
    "PyQt5",
    "PyQt6",
    "PySide2",
    "PySide6",
    "tkinter",
]


# ============================================================
# 5) Analysis
# ============================================================

a = Analysis(
    ["launcher.py"],
    pathex=[str(project_dir)],
    binaries=binaries,
    datas=datas,
    hiddenimports=hiddenimports,
    hookspath=[],
    hooksconfig={
        "matplotlib": {
            "backends": ["Agg"]
        }
    },
    runtime_hooks=[],
    excludes=excludes,
    noarchive=False,
    optimize=0,
)


# ============================================================
# 6) Python archive
# ============================================================

pyz = PYZ(
    a.pure,
    a.zipped_data,
    cipher=None
)


# ============================================================
# 7) Tek EXE oluşturma
# ============================================================

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name="TrafikTahminApp",
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=False,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=True,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
)