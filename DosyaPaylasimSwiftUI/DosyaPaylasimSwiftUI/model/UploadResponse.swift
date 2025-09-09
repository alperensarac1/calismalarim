//
//  UploadResponse.swift
//  DosyaPaylasimSwift
//
//  Created by Alperen Saraç on 7.09.2025.
//

import Foundation
struct LinkResponse: Codable {
    let ok: Bool?
    let code: String?
    let originalName: String?
    let sizeBytes: Int64?
    let mimeType: String?
    let createdAt: String?
    let expiresAt: String?
    let expired: Bool?
    let downloadUrl: String?
    let error: String?
    
    enum CodingKeys: String, CodingKey {
        case ok
        case code
        case originalName = "original_name"
        case sizeBytes = "size_bytes"
        case mimeType = "mime_type"
        case createdAt = "created_at"
        case expiresAt = "expires_at"
        case expired
        case downloadUrl = "download_url"
        case error
    }
}
