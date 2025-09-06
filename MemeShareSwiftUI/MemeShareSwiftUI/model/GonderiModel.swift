//
//  GonderiModel.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
struct GonderiModel: Codable, Equatable, Hashable {
    let id: Int
    let userId: Int
    let roomId: Int
    let mediaType: String  // "image" | "video"
    let mediaUrl: String
    let caption: String
    let uploadedAt: String // Sunucu string döndürüyor; istersen Date'e çevirebiliriz.

    enum CodingKeys: String, CodingKey {
        case id
        case userId     = "user_id"
        case roomId     = "room_id"
        case mediaType  = "media_type"
        case mediaUrl   = "media_url"
        case caption
        case uploadedAt = "uploaded_at"
    }
}
