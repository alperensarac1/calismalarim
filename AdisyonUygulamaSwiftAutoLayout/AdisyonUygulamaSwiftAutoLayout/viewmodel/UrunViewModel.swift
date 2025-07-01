//
//  UrunViewModel.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation
import Combine

@MainActor
class UrunViewModel: ObservableObject {
    private let dao = AdisyonServisDao.shared

    @Published var urunler: [Urun] = []
    @Published var kategoriler: [Kategori] = []
    @Published var sonuc: Bool? = nil
    @Published var silmeSonucu: UrunSilResponse? = nil
    @Published var kategoriSilSonuc: KategoriSilResponse? = nil

    // MARK: - Ürünleri Yükle
    func urunleriYukle() async {
        do {
            let liste = try await dao.urunleriGetir()
            self.urunler = liste
        } catch {
            print("UrunViewModel – Ürün yüklenemedi: \(error.localizedDescription)")
        }
    }

    // MARK: - Ürün Ekle
    func urunEkle(ad: String, fiyat: Float, resimBase64: String, kategoriId: Int) {
        Task {
            do {
                // Adet sabit 1 olarak gönderiliyor
                _ = try await dao.urunEkle(
                    urunAd: ad,
                    fiyat: fiyat,
                    kategoriId: kategoriId,
                    adet: 1,
                    base64: resimBase64
                )
            } catch {
                print("UrunViewModel – Ürün ekleme hatası: \(error.localizedDescription)")
            }
        }
    }

    // MARK: - Kategorileri Yükle
    func kategorileriYukle() async {
        do {
            let liste = try await dao.kategorileriGetir()
            self.kategoriler = liste
        } catch {
            print("UrunViewModel – Kategoriler yüklenemedi: \(error.localizedDescription)")
        }
    }

    // MARK: - Kategori Ekle
    func kategoriEkle(ad: String) {
        Task {
            do {
                _ = try await dao.kategoriEkle(ad: ad)
                // Başarı durumunu bildir
                self.sonuc = true
            } catch {
                print("UrunViewModel – Kategori ekleme hatası: \(error.localizedDescription)")
                self.sonuc = false
            }
        }
    }

    // MARK: - Kategori Sil
    func kategoriSil(id: Int) {
        Task {
            do {
                let resp = try await dao.kategoriSil(id: id)
                self.kategoriSilSonuc = resp
            } catch {
                print("UrunViewModel – Kategori silme hatası: \(error.localizedDescription)")
                // Hata durumunda boş bir Yanıt atmak yerine nil bırakabilirsiniz veya özel bir hata modeli dönebilirsiniz
                self.kategoriSilSonuc = nil
            }
        }
    }

    // MARK: - Ürün Sil
    func urunSil(ad: String) {
        Task {
            do {
                let resp = try await dao.urunSil(urunAd: ad)

                    // Başarıyla silindiyse listeyi yenile
                    let yeniListe = try await dao.urunleriGetir()
                    self.urunler = yeniListe
            } catch {
                // Hata durumunda silmeSonucu’nu hata içeren bir modelle güncelle
                let hataMesaji = error.localizedDescription
                print("UrunViewModel – Ürün silme hatası: \(hataMesaji)")
            }
        }
    }
}
