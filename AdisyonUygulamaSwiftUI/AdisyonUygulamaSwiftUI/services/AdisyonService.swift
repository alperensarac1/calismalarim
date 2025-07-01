//  AdisyonService.swift  – tam sürüm
import Foundation

private func log(_ t: String, _ m: String){  }

struct GenelResponse      : Codable { let status:String; let message:String }
struct UrunSilResponse    : Codable { let status:String; let message:String }
struct KategoriSilResponse: Codable { let status:String; let message:String }

final class AdisyonService {
    static let shared = AdisyonService(); private init(){}

    private let baseURL = URL(string:"https://alperensaracdeneme.com/adisyon/")!
    private let json = JSONDecoder()

    // ---------- ortak GET/POST ----------
    private func post(_ path: String,
                      _ body: [String: Any]) async throws -> Data {

        // 1️⃣ URL & aksi halde boş kalacak query listesi
        var comps = URLComponents(url: baseURL.appendingPathComponent(path),
                                  resolvingAgainstBaseURL: false)!

        //   →  cache-busting timestamp
        comps.queryItems = [ URLQueryItem(name: "_ts",
                                          value: String(Date().timeIntervalSince1970)) ]

        var req = URLRequest(url: comps.url!)
        req.httpMethod = "POST"

        // 2️⃣ Gövdeyi form-urlencoded olarak hazırla
        var form = URLComponents()
        form.queryItems = body.map { URLQueryItem(name: $0.key,
                                                  value: "\($0.value)") }
        req.httpBody = form.percentEncodedQuery?.data(using: .utf8)
        req.setValue("application/x-www-form-urlencoded; charset=utf-8",
                     forHTTPHeaderField: "Content-Type")

        // 3️⃣   **asıl kritik satır** → cevabı mutlaka sunucudan al
        req.cachePolicy = .reloadIgnoringLocalCacheData
        req.setValue("no-cache", forHTTPHeaderField: "Cache-Control")

        // 4️⃣ İstek
        let (data, resp) = try await URLSession.shared.data(for: req)
        log("POST-RESP",
            "status \((resp as? HTTPURLResponse)?.statusCode ?? -1) · \(data.count) B")

        return data          // üst katman decode edecek
    }

    private func get(_ path: String,
                     _ query: [String:String]? = nil) async throws -> Data {

        // ► URL oluştur
        var comps = URLComponents(url: baseURL.appendingPathComponent(path),
                                  resolvingAgainstBaseURL: false)!
        comps.queryItems = query?.map { URLQueryItem(name: $0.key, value: $0.value) } ?? []

        // ► CACHE-BUSTING: her isteğe zaman damgası ekle
        comps.queryItems?.append(URLQueryItem(name: "_ts",
                                              value: String(Date().timeIntervalSince1970)))

        let url = comps.url!

        // ► İsteği daima sunucudan al
        var req = URLRequest(url: url)
        req.cachePolicy = .reloadIgnoringLocalCacheData          // ⬅️ kritik satır
        req.setValue("no-cache", forHTTPHeaderField: "Cache-Control")

        let (data, resp) = try await URLSession.shared.data(for: req)

        // (opsiyonel) log
        log("GET-RESP", "status \((resp as? HTTPURLResponse)?.statusCode ?? -1) · \(data.count) B")
        return data
    }


    // ---------- Liste GET ----------
    func getMasalar()        async throws -> [Masa]        { try await json.decode([Masa].self, from:get("masa_listesi.php")) }
    func getUrunler()        async throws -> [Urun]        { try await json.decode([Urun].self, from:get("urunleri_getir.php")) }
    func getKategoriler()    async throws -> [Kategori]    { try await json.decode([Kategori].self, from:get("kategorileri_getir.php")) }
    func getMasa(masaId:Int) async throws -> Masa          { try await json.decode(Masa.self, from:get("masa_getir.php",["masa_id":"\(masaId)"])) }
    func getMasaUrunleri(masaId:Int) async throws -> [MasaUrun] {
        try await json.decode([MasaUrun].self, from:get("masa_urunleri.php",["masa_id":"\(masaId)"]))
    }

    func masaUrunEkle(masaId: Int, urunId: Int) async throws {
        _ = try await get("masa_urun_ekle.php",
                          ["masa_id": "\(masaId)",
                           "urun_id": "\(urunId)",
                           "adet": "1"])
        //  ✅ küçük gecikme; DB commit’ini garanti altına alır
        try await Task.sleep(nanoseconds: 100_000_000)   // 0.1 sn
    }

    func urunCikar(masaId: Int, urunId: Int) async throws -> UrunCikarResponse {
        let data = try await post("urun_cikar.php",
                                  ["masa_id": masaId,
                                   "urun_id": urunId])
        return try json.decode(UrunCikarResponse.self, from: data)
    }

    // ---------- Ürün ekle/sil ----------
    func urunEkle(urunAd:String, urunFiyat:Float, urunKategori:Int, urunAdet:Int, urunResimBase64:String) async throws -> Data {
        try await post("urun_ekle.php",[
            "urun_ad":urunAd,
            "urun_fiyat":urunFiyat,
            "urun_kategori":urunKategori,
            "urun_adet":urunAdet,
            "urun_resim":urunResimBase64
        ])
    }
    func urunSil(urunAd:String) async throws -> UrunSilResponse {
        let d = try await post("urun_sil.php",["urun_ad":urunAd])
        return try json.decode(UrunSilResponse.self, from:d)
    }

    // ---------- Kategori ekle/sil ----------
    func kategoriEkle(kategoriAd:String) async throws -> Data {
        try await post("kategori_ekle.php",["kategori_ad":kategoriAd])
    }
    func kategoriSil(id:Int) async throws -> KategoriSilResponse {
        let d = try await post("kategori_sil.php",["kategori_id":id])
        return try json.decode(KategoriSilResponse.self, from:d)
    }

    // ---------- Diğer işlemler ----------
    func masaEkle() async throws -> Data               { try await post("masa_ekle.php", [:]) }
    func masaSil(masaId:Int) async throws -> Data      { try await post("masa_sil.php", ["masa_id":masaId]) }
    func masaBirlestir(anaMasaId:Int,birlestirilecekMasaId:Int) async throws -> Data {
        try await post("masa_birlestir.php",["ana_masa_id":anaMasaId,"birlestirilecek_masa_id":birlestirilecekMasaId])
    }
    func masaOde(masaId:Int) async throws -> Data      { try await post("masa_odeme.php",["masa_id":masaId]) }
    func masaGetir(masaId: Int) async throws -> Masa {
        let params = ["masa_id": "\(masaId)"]
        let data = try await get("masa_getir.php", params)
        return try json.decode(Masa.self, from: data)
    }
    func getToplamFiyat(masaId:Int) async throws -> [String:Float] {
        let d = try await get("masa_toplam_fiyat.php",["masa_id":"\(masaId)"])
        return try json.decode([String:Float].self, from:d)
    }
}

private struct ToplamFiyatResponse: Codable {
    let masaId: Int
    let toplamFiyat: Float

    enum CodingKeys: String, CodingKey {
        case masaId       = "masa_id"
        case toplamFiyat  = "toplam_fiyat"
    }
}

struct UrunCikarResponse: Codable {
    let success: Bool
    let message: String
    let kalanAdet: Int?        // sunucu 0 veya >0 döndürebilir

    enum CodingKeys: String, CodingKey {
        case success
        case message
        case kalanAdet = "kalan_adet"
    }
}
