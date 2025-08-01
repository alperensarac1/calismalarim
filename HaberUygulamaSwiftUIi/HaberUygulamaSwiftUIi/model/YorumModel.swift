//
//  YorumModel.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import Foundation
struct YorumModel: Codable,Identifiable {
    let id: String
    let yorum: String
    let kullanici: String
    let tarih: String
}
