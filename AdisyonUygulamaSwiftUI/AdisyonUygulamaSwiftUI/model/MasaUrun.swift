//
//  MasaUrun.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import Foundation

struct MasaUrun: Codable {
    let urunId: Int
    let urunAd: String
    let birimFiyat: Float
    let adet: Int
    let toplamFiyat: Float

    enum CodingKeys: String, CodingKey {
        case urunId       = "urun_id"
        case urunAd       = "urun_ad"
        case birimFiyat   = "birim_fiyat"
        case adet
        case toplamFiyat  = "toplam_fiyat"
    }
}
