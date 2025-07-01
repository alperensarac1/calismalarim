//
//  AdisyonService.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation

// MARK: - Response Models (Placeholder)
struct UrunSilResponse: Codable {
    // örn: let success: Bool
}

struct KategoriSilResponse: Codable {
    // örn: let success: Bool
}

// MARK: - Service

final class AdisyonService {
    static let shared = AdisyonService()
    private init() {}

    private let baseURL = URL(string: "https://alperensaracdeneme.com/adisyon/")!  // “https://your.api.base.url/” kısmını kendi sunucu adresinizle değiştirin.

    private let jsonDecoder: JSONDecoder = {
        let decoder = JSONDecoder()
        // Eğer tarih formatları veya farklı anahtar stilleri kullanılıyorsa buraya ekleyin.
        return decoder
    }()

    // Helper: Form URL-encoded body oluşturma
    private func makeFormBody(from parameters: [String: Any]) -> Data? {
        let queryItems = parameters.map { key, value in
            URLQueryItem(name: key, value: "\(value)")
        }
        var components = URLComponents()
        components.queryItems = queryItems
        return components.percentEncodedQuery?.data(using: .utf8)
    }

    // Helper: Genel POST isteği
    private func postRequest(
        path: String,
        formParameters: [String: Any]
    ) async throws -> Data {
        let url = baseURL.appendingPathComponent(path)
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded; charset=utf-8", forHTTPHeaderField: "Content-Type")
        request.httpBody = makeFormBody(from: formParameters)

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResp = response as? HTTPURLResponse,
              200..<300 ~= httpResp.statusCode else {
            throw URLError(.badServerResponse)
        }
        return data
    }

    // Helper: Genel GET isteği (sorgu parametreli veya parametresiz)
    private func getRequest(
        path: String,
        queryParameters: [String: String]? = nil
    ) async throws -> Data {
        var components = URLComponents(url: baseURL.appendingPathComponent(path), resolvingAgainstBaseURL: false)!
        if let queries = queryParameters {
            components.queryItems = queries.map { URLQueryItem(name: $0.key, value: $0.value) }
        }
        guard let url = components.url else {
            throw URLError(.badURL)
        }

        let (data, response) = try await URLSession.shared.data(from: url)
        guard let httpResp = response as? HTTPURLResponse,
              200..<300 ~= httpResp.statusCode else {
            throw URLError(.badServerResponse)
        }
        return data
    }

    // MARK: - API Methods

    /// GET "masa_listesi.php" → [Masa]
    func getMasalar() async throws -> [Masa] {
        let data = try await getRequest(path: "masa_listesi.php")
        return try jsonDecoder.decode([Masa].self, from: data)
    }

    /// GET "masa_urunleri.php?masa_id={masaId}" → [MasaUrun]
    func getMasaUrunleri(masaId: Int) async throws -> [MasaUrun] {
        let params = ["masa_id": "\(masaId)"]
        let data = try await getRequest(path: "masa_urunleri.php", queryParameters: params)
        return try jsonDecoder.decode([MasaUrun].self, from: data)
    }

    /// POST "masa_sil.php" (FormUrlEncoded) { "masa_id": masaId } → ResponseBody (ham veri)
    func masaSil(masaId: Int) async throws -> Data {
        return try await postRequest(
            path: "masa_sil.php",
            formParameters: ["masa_id": masaId]
        )
    }

    /// POST "urun_ekle.php" (FormUrlEncoded)
    /// { "urun_ad": urunAd, "urun_fiyat": urunFiyat, "urun_kategori": urunKategori, "urun_adet": urunAdet, "urun_resim": urunResimBase64 }
    func urunEkle(
        urunAd: String,
        urunFiyat: Float,
        urunKategori: Int,
        urunAdet: Int,
        urunResimBase64: String
    ) async throws -> Data {
        let params: [String: Any] = [
            "urun_ad": urunAd,
            "urun_fiyat": urunFiyat,
            "urun_kategori": urunKategori,
            "urun_adet": urunAdet,
            "urun_resim": urunResimBase64
        ]
        return try await postRequest(path: "urun_ekle.php", formParameters: params)
    }

    /// POST "urun_sil.php" (FormUrlEncoded) { "urun_ad": urunAd } → UrunSilResponse
    func urunSil(urunAd: String) async throws -> UrunSilResponse {
        let data = try await postRequest(
            path: "urun_sil.php",
            formParameters: ["urun_ad": urunAd]
        )
        return try jsonDecoder.decode(UrunSilResponse.self, from: data)
    }

    /// GET "masa_urun_ekle.php?masa_id={masaId}&urun_id={urunId}&adet={adet}" → ResponseBody (ham veri)
    func urunEkle(masaId: Int, urunId: Int, adet: Int) async throws -> Data {
        let params = [
            "masa_id": "\(masaId)",
            "urun_id": "\(urunId)",
            "adet": "\(adet)"
        ]
        let data = try await getRequest(path: "masa_urun_ekle.php", queryParameters: params)
        return data
    }

    /// POST "masa_birlestir.php" (FormUrlEncoded)
    /// { "ana_masa_id": anaMasaId, "birlestirilecek_masa_id": birlestirilecekMasaId }
    func masaBirlestir(anaMasaId: Int, birlestirilecekMasaId: Int) async throws -> Data {
        let params: [String: Any] = [
            "ana_masa_id": anaMasaId,
            "birlestirilecek_masa_id": birlestirilecekMasaId
        ]
        return try await postRequest(path: "masa_birlestir.php", formParameters: params)
    }

    /// POST "masa_ekle.php" (FormUrlEncoded) — parametre yok
    func masaEkle() async throws -> Data {
        return try await postRequest(path: "masa_ekle.php", formParameters: [:])
    }

    /// GET "urunleri_getir.php" → [Urun]
    func getUrunler() async throws -> [Urun] {
        let data = try await getRequest(path: "urunleri_getir.php")
        return try jsonDecoder.decode([Urun].self, from: data)
    }

    /// POST "masa_odeme.php" (FormUrlEncoded) { "masa_id": masaId } → ResponseBody (ham veri)
    func masaOde(masaId: Int) async throws -> Data {
        return try await postRequest(path: "masa_odeme.php", formParameters: ["masa_id": masaId])
    }

    /// GET "masa_toplam_fiyat.php?masa_id={masaId}" → [String: Float]
    func getToplamFiyat(masaId: Int) async throws -> [String: Float] {
        let params = ["masa_id": "\(masaId)"]
        let data = try await getRequest(path: "masa_toplam_fiyat.php", queryParameters: params)
        return try jsonDecoder.decode([String: Float].self, from: data)
    }

    /// POST "urun_cikar.php" (FormUrlEncoded) { "masa_id": masaId, "urun_id": urunId } → ResponseBody (ham veri)
    func urunCikar(masaId: Int, urunId: Int) async throws -> Data {
        let params: [String: Any] = [
            "masa_id": masaId,
            "urun_id": urunId
        ]
        return try await postRequest(path: "urun_cikar.php", formParameters: params)
    }

    /// GET "kategorileri_getir.php" → [Kategori]
    func getKategoriler() async throws -> [Kategori] {
        let data = try await getRequest(path: "kategorileri_getir.php")
        return try jsonDecoder.decode([Kategori].self, from: data)
    }

    /// GET "masa_getir.php?masa_id={masaId}" → Masa
    func masaGetir(masaId: Int) async throws -> Masa {
        let params = ["masa_id": "\(masaId)"]
        let data = try await getRequest(path: "masa_getir.php", queryParameters: params)
        return try jsonDecoder.decode(Masa.self, from: data)
    }

    /// POST "kategori_ekle.php" (FormUrlEncoded) { "kategori_ad": kategoriAd } → ResponseBody (ham veri)
    func kategoriEkle(kategoriAd: String) async throws -> Data {
        return try await postRequest(path: "kategori_ekle.php", formParameters: ["kategori_ad": kategoriAd])
    }

    /// POST "kategori_sil.php" (FormUrlEncoded) { "kategori_id": id } → KategoriSilResponse
    func kategoriSil(id: Int) async throws -> KategoriSilResponse {
        let data = try await postRequest(path: "kategori_sil.php", formParameters: ["kategori_id": id])
        return try jsonDecoder.decode(KategoriSilResponse.self, from: data)
    }
}
