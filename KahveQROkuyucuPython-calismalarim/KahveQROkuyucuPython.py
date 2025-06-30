import cv2
import requests
import json
import tkinter as tk
from tkinter import messagebox
from PIL import Image, ImageTk

# API URL'lerini tanımla
DOGRULAMA_API = "https://alperensaracdeneme.com/kahveservis/kod_sorgula.php"  # QR kod doğrulama API
HEDIYE_KAHVE_API = "https://alperensaracdeneme.com/kahveservis/hediye_kahve_kullan.php"  # Hediye kahve kullan API
KAHVE_AL_API = "https://alperensaracdeneme.com/kahveservis/kahve_al.php"  # Kahve al API


class QRKodOkuyucu:
    def __init__(self, root):
        self.dogrulama_kodu = None  # Butonlar için başlangıçta boş değer
        self.kullanici_telefon = None  # Telefon numarasını saklamak için

        self.root = root
        self.root.title("Kahve QR Kod Okuyucu")

        self.kahve_sayisi = tk.StringVar(value="Kahve Sayısı: -")
        self.hediye_kahve_sayisi = tk.StringVar(value="Hediye Kahve Sayısı: -")

        # Ekran bileşenleri
        self.label_info = tk.Label(root, text="QR Kod Tarayın", font=("Arial", 14))
        self.label_info.pack(pady=10)

        self.kahve_label = tk.Label(root, textvariable=self.kahve_sayisi, font=("Arial", 12))
        self.kahve_label.pack()

        self.hediye_label = tk.Label(root, textvariable=self.hediye_kahve_sayisi, font=("Arial", 12))
        self.hediye_label.pack()

        self.btn_hediye_kullan = tk.Button(root, text="Hediye Kahve Kullan", command=self.hediye_kahve_kullan,
                                           state=tk.DISABLED)
        self.btn_hediye_kullan.pack(pady=5)

        self.btn_kahve_al = tk.Button(root, text="Kahve Al", command=self.kahve_al, state=tk.DISABLED)
        self.btn_kahve_al.pack(pady=5)

        # QR kod taramayı başlat
        self.qr_kod_tara()

    def qr_kod_tara(self):
        cap = cv2.VideoCapture(0)
        detector = cv2.QRCodeDetector()

        while True:
            ret, frame = cap.read()
            if not ret:
                continue

            # QR kodunu oku
            data, bbox, _ = detector.detectAndDecode(frame)
            if data:
                cap.release()
                cv2.destroyAllWindows()
                self.qr_kodu_isle(data)
                break

            cv2.imshow("QR Kod Tarayıcı", frame)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

        cap.release()
        cv2.destroyAllWindows()
        cv2.waitKey(500)  # MacOS'ta pencere çakışmasını önler
        self.qr_kodu_isle(data)

    def qr_kodu_isle(self, qr_data):
        try:
            response = requests.post(DOGRULAMA_API, data={"dogrulama_kodu": qr_data})

            # Ham yanıtı kontrol et
            print("API Yanıtı (ham):", response.text)  # API'den gelen ham yanıtı yazdır

            # JSON verisini almaya çalış
            try:
                result = response.json()
            except ValueError as e:
                messagebox.showerror("API Hatası", f"Yanıt JSON formatında değil: {e}")
                return

            print("API Yanıtı (JSON):", result)  # JSON yanıtını kontrol et

            # API'den gelen veriyi kontrol et
            if result.get("success") == 1:
                self.kullanici_telefon = result.get("kullanici_tel")  # Telefon numarasını kaydet
                kahve_sayisi = result.get('icilen_kahve', '-')
                hediye_sayisi = result.get('hediye_kahve', '-')
                self.kahve_sayisi.set(f"Kahve Sayısı: {kahve_sayisi}")
                self.hediye_kahve_sayisi.set(f"Hediye Kahve Sayısı: {hediye_sayisi}")

                self.dogrulama_kodu = qr_data
                # Butonları aktif et
                self.btn_hediye_kullan.config(state=tk.NORMAL)
                self.btn_kahve_al.config(state=tk.NORMAL)
            else:
                messagebox.showerror("Hata", "Doğrulama kodu geçersiz!")
        except Exception as e:
            messagebox.showerror("Bağlantı Hatası", str(e))

    def hediye_kahve_kullan(self):
        if not self.dogrulama_kodu:
            messagebox.showerror("Hata", "Önce QR kod tarayın!")
            return

        try:
            response = requests.post(HEDIYE_KAHVE_API, data={"dogrulama_kodu": self.dogrulama_kodu})
            result = response.json()

            if result.get("success") == 1:
                messagebox.showinfo("Başarılı", "Hediye kahve kullanıldı!")
                self.qr_kodu_isle(self.dogrulama_kodu)  # API verisini tekrar güncelle
            else:
                messagebox.showerror("Hata", result.get("message"))
        except Exception as e:
            messagebox.showerror("Bağlantı Hatası", str(e))

    def kahve_al(self):
        """
        Kahve alma API'sini çağırır ve PHP'den gelen tüm yanıtı işler.
        """
        if not self.dogrulama_kodu:
            messagebox.showerror("Hata", "Önce QR kod tarayın!")
            return

        if not self.kullanici_telefon:
            messagebox.showerror("Hata", "Telefon numarası eksik!")
            return

        try:
            # PHP API'sine telefon numarası ile istek yapılır
            response = requests.post(KAHVE_AL_API, data={
                "telefon_no": self.kullanici_telefon
            })

            # Yanıtın içeriğini yazdıralım
            print("API Yanıtı:", response.text)  # Yanıtı kontrol et

            # PHP yanıtını JSON formatında al
            result = response.json()

            # PHP'den gelen yanıtı kontrol et
            if result.get("success") == 1:
                # Başarı mesajı
                messagebox.showinfo("Başarılı",
                                    f"Kahve alındı!\nİçilen Kahve: {result['icilen_kahve']}\nHediye Kahve: {result['hediye_kahve']}")
                self.kahve_sayisi.set(f"Kahve Sayısı: {result['icilen_kahve']}")
                self.hediye_kahve_sayisi.set(f"Hediye Kahve Sayısı: {result['hediye_kahve']}")
            else:
                # Hata mesajı
                messagebox.showerror("Hata", result.get("message", "Bilinmeyen bir hata oluştu."))
        except Exception as e:
            messagebox.showerror("Bağlantı Hatası", str(e))


if __name__ == "__main__":
    root = tk.Tk()
    app = QRKodOkuyucu(root)
    root.mainloop()
