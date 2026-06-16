# -*- mode: python ; coding: utf-8 -*-

from pathlib import Path

from PyInstaller.utils.hooks import (
    collect_all,
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
# Streamlit dosyaları
# ============================================================

# Streamlit'in kendi static/frontend dosyalarını alıyoruz.
datas += collect_data_files("streamlit", include_py_files=False)

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


# ============================================================
# Metadata dosyaları
# ============================================================

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
    "tzdata",
    "pydeck",
    "gitpython",
    "cachetools",
    "typing-extensions",
    "starlette",
    "uvicorn",
    "websockets",
    "anyio",
]

for package_name in metadata_packages:
    try:
        datas += copy_metadata(package_name)
    except Exception:
        pass


# ============================================================
# Ana paketlerin gerekli dosya/dll/hidden importlarını topla
# ============================================================

packages_to_collect = [
    "streamlit",
    "pandas",
    "numpy",
    "sklearn",
    "joblib",
    "matplotlib",
    "holidays",
    "altair",
    "pyarrow",
    "tornado",
    "watchdog",
    "blinker",
    "click",
    "jinja2",
    "jsonschema",
    "requests",
    "tenacity",
    "toml",
    "tzdata",
    "starlette",
    "uvicorn",
    "websockets",
]

for package_name in packages_to_collect:
    try:
        package_datas, package_binaries, package_hiddenimports = collect_all(package_name)
        datas += package_datas
        binaries += package_binaries
        hiddenimports += package_hiddenimports
    except Exception:
        pass


# sklearn alt modüllerini alıyoruz, test modüllerini hariç tutuyoruz.
try:
    hiddenimports += collect_submodules(
        "sklearn",
        filter=lambda name: ".tests" not in name and ".test_" not in name
    )
except Exception:
    pass


# ============================================================
# Uygulama dosyaları
# ============================================================

datas += [
    (
        str(project_dir / "app.py"),
        "."
    ),

    (
        str(project_dir / ".streamlit" / "config.toml"),
        ".streamlit"
    ),

    (
        str(project_dir / "models" / "best_traffic_model.joblib"),
        "models"
    ),

    (
        str(project_dir / "data" / "processed" / "istanbul_ilce_gunluk_trafik_model_ready.csv"),
        "data/processed"
    ),
]


# ============================================================
# Gereksiz paketleri dışarıda bırak
# ============================================================

excludes = [
    "pytest",
    "IPython",
    "notebook",
    "jupyter",
    "jupyterlab",

    "torch",
    "tensorflow",
    "keras",

    "geopandas",
    "shapely",
    "pyproj",
    "pyogrio",
    "rtree",
    "kaggle",
    "pygeohash",

    "pandas.tests",
    "numpy.tests",
    "matplotlib.tests",
    "pyarrow.tests",
    "tornado.test",
    "jsonschema.tests",
    "sklearn.tests",

    "PyQt5",
    "PyQt6",
    "PySide2",
    "PySide6",
    "tkinter",
]


# ============================================================
# Analysis
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


pyz = PYZ(
    a.pure,
    a.zipped_data,
    cipher=None
)


# ============================================================
# EXE
# ============================================================

exe = EXE(
    pyz,
    a.scripts,
    [],
    exclude_binaries=True,
    name="TrafikTahminApp",
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=False,
    console=True,
    disable_windowed_traceback=False,
)


# ============================================================
# COLLECT
# ============================================================
# Burası tek EXE yerine klasörlü portable çıktı üretir.
# dist/TrafikTahminApp/TrafikTahminApp.exe oluşur.
# ============================================================

coll = COLLECT(
    exe,
    a.binaries,
    a.datas,
    strip=False,
    upx=False,
    upx_exclude=[],
    name="TrafikTahminApp",
)