//
//  UrunViewModel.swift
//

import Foundation

@MainActor
class UrunViewModel: ObservableObject {
    private let dao = AdisyonServisDao.shared

    @Published var urunler: [Urun] = []
    @Published var kategoriler: [Kategori] = []

    @Published var kategoriSilSonuc: KategoriSilResponse?
    @Published var silmeSonucu: UrunSilResponse?

    // MARK: - Ürünleri Yükle
    func urunleriYukle() async {
        do {
            urunler = try await dao.urunleriGetir()
        } catch {
            print("⛔️ Ürün yüklenemedi:", error.localizedDescription)
        }
    }

    // MARK: - Ürün Ekle
    func urunEkle(ad: String, fiyat: Float,
                  resimBase64: String, kategoriId: Int) {
        Task {
            do {
                // adet = 1
                try await dao.urunEkle(
                    urunAd: ad,
                    fiyat: fiyat,
                    kategoriId: kategoriId,
                    adet: 1,
                    base64: resimBase64
                )
                // 🔄 Listeyi yenile
                urunler = try await dao.urunleriGetir()
            } catch {
                print("⛔️ Ürün ekleme hatası:", error.localizedDescription)
            }
        }
    }

    // MARK: - Kategorileri Yükle
    func kategorileriYukle() async {
        do {
            kategoriler = try await dao.kategorileriGetir()
        } catch {
            print("⛔️ Kategoriler yüklenemedi:", error.localizedDescription)
        }
    }

    // MARK: - Kategori Ekle
    func kategoriEkle(ad: String) {
        Task {
            do {
                _ = try await dao.kategoriEkle(ad: ad)
                // 🔄 Listeyi yenile
                kategoriler = try await dao.kategorileriGetir()
            } catch {
                print("⛔️ Kategori ekleme hatası:", error.localizedDescription)
            }
        }
    }

    // MARK: - Kategori Sil
    func kategoriSil(id: Int) {
        Task {
            do {
                kategoriSilSonuc = try await dao.kategoriSil(id: id)
                // 🔄 Listeyi yenile
                kategoriler = try await dao.kategorileriGetir()
            } catch {
                print("⛔️ Kategori silme hatası:", error.localizedDescription)
                kategoriSilSonuc = nil
            }
        }
    }

    // MARK: - Ürün Sil
    func urunSil(ad: String) {
        Task {
            do {
                silmeSonucu = try await dao.urunSil(urunAd: ad)
                // 🔄 Listeyi yenile
                urunler = try await dao.urunleriGetir()
            } catch {
                print("⛔️ Ürün silme hatası:", error.localizedDescription)
            }
        }
    }
}
