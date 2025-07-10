//
//  Model.swift
//  ChatSwift
//
//  Created by Alperen Saraç on 4.07.2025.
//

import Foundation

struct KonusulanKisi: Codable {
    let id: Int
    let ad: String
    let numara: String
    let son_mesaj: String
    let tarih: String
}

struct Kullanici: Codable {
    let id: Int
    let ad: String
    let numara: String
}

struct Mesaj: Codable {
    let id: Int
    let gonderen_id: Int
    let alici_id: Int
    let mesaj_text: String?
    let resim_var: Int
    let resim_url: String?
    let tarih: String
}

struct KonusulanKisiListResponse: Codable {
    let success: Bool
    let kisiler: [KonusulanKisi]
}

struct KullaniciListResponse: Codable {
    let success: Bool
    let kullanicilar: [Kullanici]
}

struct MesajListResponse: Codable {
    let success: Bool
    let mesajlar: [Mesaj]
}

struct SimpleResponse: Codable {
    let success: Bool
    let message: String?
    let id: Int?
    let error: String?
}
