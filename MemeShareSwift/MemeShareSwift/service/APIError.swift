//
//  ApiError.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
enum APIError: Error, LocalizedError {
    case invalidURL
    case invalidResponse
    case server(status: Int)
    case decodeFailed
    case fileNotFound
    case unknown(Error)

    var errorDescription: String? {
        switch self {
        case .invalidURL: return "Geçersiz URL."
        case .invalidResponse: return "Geçersiz sunucu yanıtı."
        case .server(let status): return "Sunucu hatası: \(status)"
        case .decodeFailed: return "Yanıt çözümlenemedi."
        case .fileNotFound: return "Dosya bulunamadı."
        case .unknown(let e): return e.localizedDescription
        }
    }
}
