//
//  SimpleResponse.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
struct SimpleResponse: Decodable,Equatable {
    let success: Bool
    let message: String?
    let roomCode: String?
    let roomId: Int?

    enum CodingKeys: String, CodingKey {
        case success, message
        case roomCode = "room_code"
        case roomId   = "room_id"
    }
}
