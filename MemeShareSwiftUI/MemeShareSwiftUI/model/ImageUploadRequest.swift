//
//  ImageUploadRequest.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
struct ImageUploadRequest: Codable, Equatable, Hashable {
    let roomId: Int
    let userId: Int
    let base64Image: String
    let caption: String

    enum CodingKeys: String, CodingKey {
        case roomId      = "room_id"
        case userId      = "user_id"
        case base64Image = "base64_image"
        case caption
    }
}
