//
//  ApiClient.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation

enum ApiError: Error, LocalizedError {
    case badURL
    case badResponse
    case decodeFail
    case server(String)

    var errorDescription: String? {
        switch self {
        case .badURL: return "URL hatası"
        case .badResponse: return "Sunucu yanıtı hatalı"
        case .decodeFail: return "Veri çözümlenemedi"
        case .server(let m): return m
        }
    }
}

final class ApiClient {
    static let shared = ApiClient()
    private init() {}

    private let base = "https://alperensaracdeneme.com/yardim/"

    private func makeURL(_ path: String, query: [String: String] = [:]) throws -> URL {
        guard var comp = URLComponents(string: base + path) else { throw ApiError.badURL }
        if !query.isEmpty {
            comp.queryItems = query.map { URLQueryItem(name: $0.key, value: $0.value) }
        }
        guard let url = comp.url else { throw ApiError.badURL }
        return url
    }

    func post<T: Codable, R: Codable>(_ path: String, body: T, response: R.Type) async throws -> R {
        let url = try makeURL(path)
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.httpBody = try JSONEncoder().encode(body)

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard (resp as? HTTPURLResponse)?.statusCode == 200 else { throw ApiError.badResponse }

        guard let decoded = try? JSONDecoder().decode(R.self, from: data) else { throw ApiError.decodeFail }
        return decoded
    }

    func get<R: Codable>(_ path: String, query: [String: String], response: R.Type) async throws -> R {
        let url = try makeURL(path, query: query)
        let (data, resp) = try await URLSession.shared.data(from: url)
        guard (resp as? HTTPURLResponse)?.statusCode == 200 else { throw ApiError.badResponse }

        guard let decoded = try? JSONDecoder().decode(R.self, from: data) else { throw ApiError.decodeFail }
        return decoded
    }
}
