import Foundation


struct Masa: Decodable,Identifiable,Hashable,Equatable {
    let id: Int
    let toplamFiyat: Float
    let acikMi: Int
    let sure: String

    // Manuel oluşturma için eklediğimiz init
    init(id: Int, toplamFiyat: Float, acikMi: Int, sure: String) {
        self.id = id
        self.toplamFiyat = toplamFiyat
        self.acikMi = acikMi
        self.sure = sure
    }

    enum CodingKeys: String, CodingKey {
        case id
        case toplamFiyat = "toplam_fiyat"
        case acikMi      = "acik_mi"
        case sure
    }

    // JSON decode işlemi
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)

        // id: String veya Int gelebilir
        if let str = try? c.decode(String.self, forKey: .id),
           let intID = Int(str) {
            id = intID
        } else {
            id = try c.decode(Int.self, forKey: .id)
        }

        // toplam_fiyat: Int veya Float gelebilir
        if let tf = try? c.decode(Float.self, forKey: .toplamFiyat) {
            toplamFiyat = tf
        } else if let ti = try? c.decode(Int.self, forKey: .toplamFiyat) {
            toplamFiyat = Float(ti)
        } else {
            toplamFiyat = 0
        }

        // acik_mi: String veya Int gelebilir
        if let str = try? c.decode(String.self, forKey: .acikMi),
           let flag = Int(str) {
            acikMi = flag
        } else {
            acikMi = try c.decode(Int.self, forKey: .acikMi)
        }

        // sure her zaman String
        sure = try c.decode(String.self, forKey: .sure)
    }
}
