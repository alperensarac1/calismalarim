//
//  UploadResponse.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
struct UploadResponse: Decodable {
    let success: Bool
    let message: String?
    let mediaUrl: String?

    enum CodingKeys: String, CodingKey {
        case success, message
        case mediaUrl = "media_url"
    }
}
