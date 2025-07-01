//
//  AdisyonServisDao.swift
//

import Foundation

enum AdisyonHata: Error { case istekBasarisiz(String) }

// MARK: - AdisyonServisDao
final class AdisyonServisDao {
    static let shared = AdisyonServisDao(); private init() {}

    private let servis = AdisyonService.shared

    // ---------- Liste / Get ----------
    func urunleriGetir() async throws -> [Urun]            { try await servis.getUrunler()   }
    func masalariGetir() async throws -> [Masa]            { try await servis.getMasalar()   }
    func kategorileriGetir() async throws -> [Kategori]    { try await servis.getKategoriler() }
    func masaGetir(masaId:Int) async throws -> Masa        { try await servis.getMasa(masaId: masaId) }
    func masaUrunleriniGetir(masaId:Int) async throws -> [MasaUrun] {
        try await servis.getMasaUrunleri(masaId: masaId)
    }

    // ---------- Masa → Ürün ekle (DAİMA 1 adet) ----------
    func masaUrunEkle(masaId: Int, urunId: Int) async throws {
        try await servis.masaUrunEkle(masaId: masaId, urunId: urunId)
    }


    // ---------- Masa → Ürün çıkar ----------
    func urunCikar(masaId: Int, urunId: Int) async throws {
        let resp = try await servis.urunCikar(masaId: masaId, urunId: urunId)
        guard resp.success else { throw AdisyonHata.istekBasarisiz(resp.message) }
    }

    // ---------- Diğer işlemler (imzalar değişmedi) ----------
    func masaSil(masaId:Int) async throws -> Data                    { try await servis.masaSil(masaId: masaId) }
    func masaEkle() async throws -> Data                             { try await servis.masaEkle() }
    func masaBirlestir(anaMasaId:Int,birlestirilecekMasaId:Int) async throws -> Data {
        try await servis.masaBirlestir(anaMasaId: anaMasaId, birlestirilecekMasaId: birlestirilecekMasaId)
    }
    func masaOdemeYap(masaId:Int) async throws -> Data               { try await servis.masaOde(masaId: masaId) }
    func masaToplamFiyat(masaId:Int) async throws -> Float           {
        try await servis.getToplamFiyat(masaId: masaId)["toplam_fiyat"] ?? 0.0
    }

    // ---------- Stok/Ürün/Kategori ----------
    func urunEkle(urunAd:String,fiyat:Float,kategoriId:Int,adet:Int, base64:String) async {
        try? await servis.urunEkle(urunAd: urunAd,
                                   urunFiyat: fiyat,
                                   urunKategori: kategoriId,
                                   urunAdet: adet,
                                   urunResimBase64: base64)
    }
    func urunSil(urunAd:String) async throws -> UrunSilResponse      { try await servis.urunSil(urunAd: urunAd) }
    func kategoriEkle(ad:String) async throws -> Data                { try await servis.kategoriEkle(kategoriAd: ad) }
    func kategoriSil(id:Int) async throws -> KategoriSilResponse     { try await servis.kategoriSil(id: id) }
}
