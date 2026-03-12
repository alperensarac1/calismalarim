//
//  ApiClient.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 1.03.2026.
//

import Foundation

enum ApiError: Error {
    case badURL
    case badResponse
    case decodeError
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

    func post<B: Codable, R: Codable>(_ path: String, body: B) async throws -> R {
        let url = try makeURL(path)
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.httpBody = try JSONEncoder().encode(body)

        let (data, resp) = try await URLSession.shared.data(for: req)

        if let http = resp as? HTTPURLResponse {
            print("POST PATH:", path)
            print("STATUS:", http.statusCode)
        }

        print("RAW RESPONSE:", String(data: data, encoding: .utf8) ?? "nil")

        guard let http = resp as? HTTPURLResponse else {
            throw ApiError.badResponse
        }

        guard http.statusCode == 200 else {
            throw ApiError.badResponse
        }

        do {
            let decoded = try JSONDecoder().decode(R.self, from: data)
            return decoded
        } catch {
            print("DECODE ERROR:", error)
            throw ApiError.decodeError
        }
    }

    func get<R: Codable>(_ path: String, query: [String: String]) async throws -> R {
        let url = try makeURL(path, query: query)
        let (data, resp) = try await URLSession.shared.data(from: url)
        guard (resp as? HTTPURLResponse)?.statusCode == 200 else { throw ApiError.badResponse }
        guard let decoded = try? JSONDecoder().decode(R.self, from: data) else { throw ApiError.decodeError }
        return decoded
    }
}
