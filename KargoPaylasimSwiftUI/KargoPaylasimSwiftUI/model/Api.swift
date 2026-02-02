//
//  Api.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation


struct ApiResp<T: Decodable>: Decodable {
    let ok: Bool
    let data: T?
    let error: String?
}

enum APIError: LocalizedError {
    case server(String)
    case badURL
    case badResponse
    case decode
    case unauthorized

    var errorDescription: String? {
        switch self {
        case .server(let s): return s
        case .badURL: return "URL hatalı"
        case .badResponse: return "Sunucu yanıtı hatalı"
        case .decode: return "Veri çözümlenemedi"
        case .unauthorized: return "Oturum geçersiz"
        }
    }
}
