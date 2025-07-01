import SwiftUI
import Foundation

@MainActor
class MasaDetayViewModel: ObservableObject {
    private let masaId: Int
    private let dao = AdisyonService.shared

    @Published var urunler: [MasaUrun] = []
    @Published var tumUrunler: [Urun] = []
    @Published var kategoriler: [Kategori] = []
    @Published var toplamFiyat: Float = 0
    @Published var isLoading: Bool = false
    @Published var masa: Masa? = nil
    @Published var odemeTamamlandi: Bool = false

    init(masaId: Int) {
        self.masaId = masaId
    }

    func yukleTumVeriler() async {
        print("📥 Veriler yükleniyor…")
        isLoading = true
        do {
            let gelenMasa = try await dao.masaGetir(masaId: masaId)
            masa = gelenMasa

            async let masaUrunleri = dao.getMasaUrunleri(masaId: masaId)
            async let tumUrunlerListe = dao.getUrunler()
            async let kategoriListe = dao.getKategoriler()

            let (mUrunler, tUrunler, kListe) = try await (masaUrunleri, tumUrunlerListe, kategoriListe)

            let guncellenmisTumUrunler: [Urun] = tUrunler.map { urun in
                if let eslesen = mUrunler.first(where: { $0.urunId == urun.id }) {
                    var kopya = urun
                    kopya.urunAdet = eslesen.adet
                    return kopya
                } else {
                    var kopya = urun
                    kopya.urunAdet = 0
                    return kopya
                }
            }

            urunler = mUrunler
            tumUrunler = guncellenmisTumUrunler
            kategoriler = kListe
            toplamFiyat = mUrunler.reduce(0) { $0 + $1.toplamFiyat }

            print("✅ Veriler yüklendi. Ürün: \(mUrunler.count), Toplam: \(toplamFiyat) TL")
            print("📦 Tüm ürün id'leri: \(tUrunler.map { $0.id })")
            print("📦 Masadaki ürün id'leri: \(mUrunler.map { $0.urunId })")


        } catch {
            print("⛔️ Veri yükleme hatası: \(error.localizedDescription)")
        }
        isLoading = false
    }

    func urunEkle(urunId: Int, adet: Int = 1) async {
        print("📦 Ürün ekleniyor → masa: \(masaId), urun: \(urunId), adet: \(adet)")
        do {
            let sonuc = try await dao.masaUrunEkle(masaId: masaId, urunId: urunId)
            print("✅ Ürün eklendi. Sunucu yanıtı: byte")
            await yukleTumVeriler()
        } catch {
            print("⛔️ Ürün ekleme hatası: \(error.localizedDescription)")
        }
    }

    func urunCikar(urunId: Int) async {
        print("🗑️ Ürün çıkarılıyor → masa: \(masaId), urun: \(urunId)")
        do {
            try await dao.urunCikar(masaId: masaId, urunId: urunId)
            await yukleTumVeriler()
        } catch {
            print("⛔️ Ürün çıkarma hatası: \(error.localizedDescription)")
        }
    }

    func odemeAl(onSuccess: @escaping () -> Void) {
        Task {
            print("💳 Ödeme işlemi başlatılıyor...")
            do {
                _ = try await dao.masaOde(masaId: masaId)
                tumUrunler = tumUrunler.map { urun in
                    var kopya = urun
                    kopya.urunAdet = 0
                    return kopya
                }
                toplamFiyat = 0
                odemeTamamlandi = true
                print("✅ Ödeme tamamlandı.")
                onSuccess()
            } catch {
                print("⛔️ Ödeme alma hatası: \(error.localizedDescription)")
            }
        }
    }
}
