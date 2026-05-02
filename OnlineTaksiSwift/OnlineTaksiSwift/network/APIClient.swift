//
//  APIClient.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation

final class APIClient {

    static let shared = APIClient()
    private init() {}

    func request<T: Decodable, B: Encodable>(
        endpoint: Endpoint,
        method: String,
        body: B?,
        responseType: T.Type,
        completion: @escaping (Result<T, Error>) -> Void
    ) {
        guard let url = URL(string: Constants.baseURL + endpoint.path) else {
            completion(.failure(NSError(domain: "URL_ERROR", code: -1)))
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if let token = SessionManager.shared.token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        if let body = body {
            do {
                request.httpBody = try JSONEncoder().encode(body)
            } catch {
                completion(.failure(error))
                return
            }
        }

        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    completion(.failure(error))
                    return
                }

                guard let http = response as? HTTPURLResponse,
                      let data = data else {
                    completion(.failure(NSError(domain: "RESPONSE_ERROR", code: -2)))
                    return
                }

                guard (200...299).contains(http.statusCode) else {
                    let message = String(data: data, encoding: .utf8) ?? "Sunucu hatası"
                    completion(.failure(NSError(domain: message, code: http.statusCode)))
                    return
                }

                do {
                    let decoded = try JSONDecoder().decode(T.self, from: data)
                    completion(.success(decoded))
                } catch {
                    completion(.failure(error))
                }
            }
        }.resume()
    }
}

struct EmptyBody: Encodable {}
