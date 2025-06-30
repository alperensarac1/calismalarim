//
//  CRUDCevap.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 29.03.2025.
//

import Foundation

struct CRUDCevap: Codable {
    let success: Int
    let message: String
    let icilenKahve: Int
    let hediyeKahve: Int

    enum CodingKeys: String, CodingKey {
        case success
        case message
        case icilenKahve = "icilen_kahve"
        case hediyeKahve = "hediye_kahve"
    }
}

