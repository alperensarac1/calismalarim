//
//  KullaniciResponse.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
struct KullaniciResponse: Decodable,Equatable {
    let success: Bool
    let message: String?     // ⬅️ optional
    let userId: Int?         // ⬅️ optional

    enum CodingKeys: String, CodingKey {
        case success
        case message
        case userId = "user_id"
    }
}
