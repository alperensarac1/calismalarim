//
//  YorumInsertRequest.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import Foundation
struct YorumInsertRequest: Codable {
    let haber_id: String
    let kullanici: String
    let yorum: String
}
