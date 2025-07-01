//
//  Urun.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation

struct Urun: Codable {
    let id: Int
    var urunAd: String
    var urunFiyat: Float
    var urunResim: String
    var urunAdet: Int
    var urunKategori: Kategori

    enum CodingKeys: String, CodingKey {
        case id
        case urunAd       = "urun_ad"
        case urunFiyat    = "urun_fiyat"
        case urunResim    = "urun_resim"
        case urunAdet     = "urun_adet"
        case urunKategori
    }
}
