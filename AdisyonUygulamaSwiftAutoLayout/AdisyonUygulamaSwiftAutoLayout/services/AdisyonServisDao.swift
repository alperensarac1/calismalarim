//
//  AdisyonServisDao.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation

// MARK: - AdisyonServisDao

final class AdisyonServisDao {
    static let shared = AdisyonServisDao()
    private init() {}

    private let servis = AdisyonService.shared

    // MARK: - Ürünleri Getir

    func urunleriGetir() async throws -> [Urun] {
        return try await servis.getUrunler()
    }

    // MARK: - Masa Sil

    func masaSil(masaId: Int) async throws -> Data {
        return try await servis.masaSil(masaId: masaId)
    }

    // MARK: - Ürün Ekle (Genel)

    func urunEkle(
        urunAd: String,
        fiyat: Float,
        kategoriId: Int,
        adet: Int,
        base64: String
    ) async {
        do {
            _ = try await servis.urunEkle(
                urunAd: urunAd,
                urunFiyat: fiyat,
                urunKategori: kategoriId,
                urunAdet: adet,
                urunResimBase64: base64
            )
        } catch {
            print("⚠️ Ürün ekleme hatası: \(error)")
        }
    }

    // MARK: - Masa Birleştir

    func masaBirlestir(
        anaMasaId: Int,
        birlestirilecekMasaId: Int
    ) async throws -> Data {
        return try await servis.masaBirlestir(
            anaMasaId: anaMasaId,
            birlestirilecekMasaId: birlestirilecekMasaId
        )
    }

    // MARK: - Masa Ekle

    func masaEkle() async throws -> Data {
        return try await servis.masaEkle()
    }

    // MARK: - Masaları Getir

    func masalariGetir() async throws -> [Masa] {
        return try await servis.getMasalar()
    }

    // MARK: - Masa Ürünlerini Getir

    func masaUrunleriniGetir(masaId: Int) async throws -> [MasaUrun] {
        return try await servis.getMasaUrunleri(masaId: masaId)
    }

    // MARK: - Ürün Ekle (Masaya)

    func urunEkle(
        masaId: Int,
        urunId: Int,
        adet: Int
    ) async throws -> Data {
        return try await servis.urunEkle(
            masaId: masaId,
            urunId: urunId,
            adet: adet
        )
    }

    // MARK: - Masa Ödeme Yap

    func masaOdemeYap(masaId: Int) async throws -> Data {
        return try await servis.masaOde(masaId: masaId)
    }

    // MARK: - Masa Toplam Fiyat
    func masaToplamFiyat(masaId: Int) async throws -> Float {
        let yanit = try await servis.getToplamFiyat(masaId: masaId)
        return yanit["toplam_fiyat"] ?? 0.0
    }

    // MARK: - Ürün Çıkar
    func urunCikar(
        masaId: Int,
        urunId: Int
    ) async throws -> Data {
        return try await servis.urunCikar(
            masaId: masaId,
            urunId: urunId
        )
    }

    // MARK: - Kategorileri Getir

    func kategorileriGetir() async throws -> [Kategori] {
        return try await servis.getKategoriler()
    }

    // MARK: - Masa Getir

    func masaGetir(masaId: Int) async throws -> Masa {
        return try await servis.masaGetir(masaId: masaId)
    }

    // MARK: - Kategori Ekle

    func kategoriEkle(ad: String) async throws -> Data {
        return try await servis.kategoriEkle(kategoriAd: ad)
    }

    // MARK: - Kategori Sil

    /// Kotlin'deki: `override suspend fun kategoriSil(id: Int): KategoriSilResponse`
    func kategoriSil(id: Int) async throws -> KategoriSilResponse {
        return try await servis.kategoriSil(id: id)
    }

    // MARK: - Ürün Sil

    /// Kotlin'deki: `override suspend fun urunSil(urunAd: String): UrunSilResponse`
    func urunSil(urunAd: String) async throws -> UrunSilResponse {
        return try await servis.urunSil(urunAd: urunAd)
    }
}
