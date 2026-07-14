//
//  ApiClient.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

/*
    APIClient

    PHP backend'e POST isteği atan genel sınıftır.

    SwiftUI tarafında native URLSession kullanıyoruz.

    PHP tarafında $_POST kullandığımız için:
    Content-Type = application/x-www-form-urlencoded
*/
final class APIClient {

    static let shared = APIClient()
    static let baseURL = "https://alperensaracdeneme.com/event_ticket_api/"

    private init() {}

    func post<T: Decodable>(
        endpoint: String,
        parameters: [String: String]
    ) async throws -> APIResponse<T> {

        guard let url = URL(string: APIClient.baseURL + endpoint) else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"

        request.setValue(
            "application/x-www-form-urlencoded",
            forHTTPHeaderField: "Content-Type"
        )

        /*
            POST body hazırlama.

            Örnek:
            email=a%40test.com&password=123456
        */
        let bodyString = parameters
            .map { key, value in
                let escapedKey = key.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? key
                let escapedValue = value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? value
                return "\(escapedKey)=\(escapedValue)"
            }
            .joined(separator: "&")

        request.httpBody = bodyString.data(using: .utf8)

        /*
            Swift Concurrency:
            URLSession.shared.data(for:) async çalışır.
        */
        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            throw APIError.httpError(code: httpResponse.statusCode)
        }

        do {
            let decoder = JSONDecoder()
            return try decoder.decode(APIResponse<T>.self, from: data)
        } catch {
            /*
                Decode hatasında ham JSON'u console'da görmek çok faydalı olur.
            */
            if let rawJson = String(data: data, encoding: .utf8) {
                print("JSON Decode Error Raw Response:")
                print(rawJson)
            }

            throw error
        }
    }
}

/*
    API hata tipleri.
*/
enum APIError: LocalizedError {
    case invalidURL
    case invalidResponse
    case httpError(code: Int)

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Geçersiz API adresi"
        case .invalidResponse:
            return "Geçersiz sunucu cevabı"
        case .httpError(let code):
            return "HTTP sunucu hatası: \(code)"
        }
    }
}
