//
//  MasaDetayViewModel.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation
import Combine

@MainActor
class MasaDetayViewModel: ObservableObject {
    // MARK: - Özellikler
    private let masaId: Int
    private let dao = AdisyonServisDao.shared

    @Published var urunler: [MasaUrun] = []             // Masaya ait ürünler
    @Published var tumUrunler: [Urun] = []               // Tüm ürünler (adet bilgisi güncellenmiş)
    @Published var kategoriler: [Kategori] = []
    @Published var toplamFiyat: Float = 0
    @Published var isLoading: Bool = false
    @Published var masa: Masa? = nil
    @Published var odemeTamamlandi: Bool = false

    // MARK: - Başlatıcı
    init(masaId: Int) {
        self.masaId = masaId
    }

    // MARK: - Tüm Verileri Yükle
    /// Masa bilgisi, masa ürünleri, tüm ürünler ve kategoriler çekilip
    /// ilgili listeler güncellenir. Toplam fiyat hesaplanır.
    func yukleTumVeriler() {
        Task {
            isLoading = true

            do {
                // 1. Masa bilgisini al
                let gelenMasa = try await dao.masaGetir(masaId: masaId)
                masa = gelenMasa

                // 2. Masaya ait ürünleri, tüm ürünleri ve kategorileri aynı anda çek
                async let masaUrunleri = dao.masaUrunleriniGetir(masaId: masaId)
                async let tumUrunlerListe = dao.urunleriGetir()
                async let kategoriListe = dao.kategorileriGetir()

                let (mUrunler, tUrunler, kListe) = try await (masaUrunleri, tumUrunlerListe, kategoriListe)

                // 3. Tüm ürün listesindeki adet bilgilerini, masadaki ürün adetine göre güncelle
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

                // 4. Yayınlanan değerleri güncelle
                urunler = mUrunler
                tumUrunler = guncellenmisTumUrunler
                kategoriler = kListe
                toplamFiyat = mUrunler.reduce(0) { $0 + $1.toplamFiyat }

            } catch {
                print("MasaDetayViewModel – Veri yükleme hatası: \(error.localizedDescription)")
            }

            isLoading = false
        }
    }

    // MARK: - Ürün Ekle
    /// Belirtilen ürün masaya eklendikten sonra
    /// masa ürünleri ve toplam fiyat yeniden güncellenir.
    func urunEkle(urunId: Int, adet: Int = 1) {
        Task {
            do {
                // Servise masaya ürün ekleme isteği
                _ = try await dao.urunEkle(masaId: masaId, urunId: urunId, adet: adet)

                // 1) Masaya ait ürünleri tekrar çek
                let mUrunler = try await dao.masaUrunleriniGetir(masaId: masaId)
                urunler = mUrunler
                toplamFiyat = mUrunler.reduce(0) { $0 + $1.toplamFiyat }

                // 2) Tüm ürün listesindeki adet bilgisini de yeniden güncellemek istiyorsak
                let tUrunler = try await dao.urunleriGetir()
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
                tumUrunler = guncellenmisTumUrunler

                // 3) İsterseniz tüm verileri baştan da yükleyebilirsiniz:
                // yukleTumVeriler()

            } catch {
                print("MasaDetayViewModel – Ürün ekleme hatası: \(error.localizedDescription)")
            }
        }
    }

    // MARK: - Ürün Çıkar
    /// Masadaki ilgili ürün adeti azaltılır, ardından liste ve toplam fiyat güncellenir.
    func urunCikar(urunId: Int) {
        Task {
            do {
                // 1. Servise ürün çıkarma isteği
                _ = try await dao.urunCikar(masaId: masaId, urunId: urunId)

                // 2. TümÜrünler listesindeki ilgili ürünün adetini azalt
                tumUrunler = tumUrunler.map { urun in
                    var kopya = urun
                    if urun.id == urunId && urun.urunAdet > 0 {
                        kopya.urunAdet = urun.urunAdet - 1
                    }
                    return kopya
                }

                // 3. Masaya ait ürünleri ve toplam fiyatı güncelle
                let mUrunler = try await dao.masaUrunleriniGetir(masaId: masaId)
                urunler = mUrunler
                toplamFiyat = mUrunler.reduce(0) { $0 + $1.toplamFiyat }

                // 4. İsterseniz verileri baştan da yükleyebilirsiniz:
                // yukleTumVeriler()

            } catch {
                print("MasaDetayViewModel – Ürün çıkarma hatası: \(error.localizedDescription)")
            }
        }
    }

    // MARK: - Ödeme Al
    /// Ödeme alındıktan sonra tüm ürün adetleri sıfırlanır ve toplam fiyat sıfırlanır.
    /// onSuccess kapanışı çağrılarak UI bilgilendirilir.
    func odemeAl(onSuccess: @escaping () -> Void) {
        Task {
            do {
                // 1. Ödeme servisini çağır
                _ = try await dao.masaOdemeYap(masaId: masaId)

                // 2. TümÜrünler listesindeki tüm adetleri sıfırla
                tumUrunler = tumUrunler.map { urun in
                    var kopya = urun
                    kopya.urunAdet = 0
                    return kopya
                }

                // 3. Toplam fiyat sıfırla
                toplamFiyat = 0

                // 4. Ödeme tamamlandı bayrağını güncelle
                odemeTamamlandi = true

                // 5. Başarı kapanışını çağır
                onSuccess()

            } catch {
                print("MasaDetayViewModel – Ödeme alma hatası: \(error.localizedDescription)")
            }
        }
    }
}
