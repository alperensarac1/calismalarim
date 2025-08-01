//
//  HaberModel.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import Foundation

struct HaberModel: Codable,Identifiable {
    let id: String
    let baslik: String
    let icerik: String
    let media_type: String
    let media_url: String
    let yayinlanma_tarihi: String
    let sondakika: String
    let yazar_id: String?
    let tur_id: String?
    let ad: String?
    let soyad: String?
    let unvan: String?
    let tur_adi: String?
}

