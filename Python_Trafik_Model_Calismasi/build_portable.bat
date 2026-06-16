@echo off
chcp 65001

echo ========================================
echo Trafik Tahmin Portable Build
echo ========================================

cd /d "%~dp0"

echo.
echo Proje klasörü:
echo %cd%

echo.
echo Eski build klasörleri temizleniyor...
rmdir /s /q build 2>nul
rmdir /s /q dist 2>nul

echo.
echo Sanal ortam Python kontrol ediliyor...

if not exist ".venv\Scripts\python.exe" (
    echo HATA: .venv\Scripts\python.exe bulunamadı.
    pause
    exit /b 1
)

echo.
echo Paketler kuruluyor...
".venv\Scripts\python.exe" -m pip install -r requirements.txt
".venv\Scripts\python.exe" -m pip install pyinstaller

echo.
echo Spec dosyası kontrol ediliyor...

if not exist "trafik_tahmin_folder.spec" (
    echo HATA: trafik_tahmin_folder.spec bulunamadı.
    pause
    exit /b 1
)

echo.
echo Portable klasör oluşturuluyor...
".venv\Scripts\python.exe" -m PyInstaller --clean --noconfirm trafik_tahmin_folder.spec

if not exist "dist\TrafikTahminApp\TrafikTahminApp.exe" (
    echo.
    echo HATA: Portable EXE oluşturulamadı.
    echo Yukarıdaki PyInstaller hata mesajını kontrol et.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Portable build başarıyla tamamlandı.
echo Klasör:
echo dist\TrafikTahminApp
echo.
echo Başka bilgisayara bu klasörü komple kopyala:
echo dist\TrafikTahminApp
echo.
echo Çalıştırılacak dosya:
echo dist\TrafikTahminApp\TrafikTahminApp.exe
echo ========================================

pause