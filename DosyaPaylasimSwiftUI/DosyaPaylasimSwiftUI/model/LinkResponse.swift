//
//  LinkResponse.swift
//  DosyaPaylasimSwift
//
//  Created by Alperen Saraç on 7.09.2025.
//

import Foundation
struct UploadResponse: Codable {
    let ok: Bool?
    let code: String?
    let downloadUrl: String?
    let infoUrl: String?
    let expiresAt: String?
    let error: String?
    
    enum CodingKeys: String, CodingKey {
        case ok
        case code
        case downloadUrl = "download_url"
        case infoUrl = "info_url"
        case expiresAt = "expires_at"
        case error
    }
}
