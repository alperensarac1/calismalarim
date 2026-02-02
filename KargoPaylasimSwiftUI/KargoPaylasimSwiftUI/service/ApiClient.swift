import Foundation

final class APIClient {
    let tokenStore: TokenStore

    init(tokenStore: TokenStore) {
        self.tokenStore = tokenStore
    }

    func get<T: Decodable>(_ ep: Endpoint, query: [String:String] = [:], as: T.Type) async throws -> T {
        var comp = URLComponents(url: ep.url, resolvingAgainstBaseURL: false)!
        if !query.isEmpty {
            comp.queryItems = query.map { URLQueryItem(name: $0.key, value: $0.value) }
        }
        guard let url = comp.url else { throw APIError.badURL }

        var req = URLRequest(url: url)
        req.httpMethod = "GET"
        if let t = tokenStore.token { req.setValue(t, forHTTPHeaderField: "X-Auth-Token") }

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard let http = resp as? HTTPURLResponse else { throw APIError.badResponse }
        if http.statusCode == 401 { throw APIError.unauthorized }

        do { return try JSONDecoder().decode(T.self, from: data) }
        catch {
            let raw = String(data: data, encoding: .utf8) ?? ""
            throw APIError.server("Decode error. Raw:\n\(raw)")
        }
    }

    func postJSON<B: Encodable, T: Decodable>(_ ep: Endpoint, body: B, as: T.Type) async throws -> T {
        var req = URLRequest(url: ep.url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let t = tokenStore.token { req.setValue(t, forHTTPHeaderField: "X-Auth-Token") }
        req.httpBody = try JSONEncoder().encode(body)

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard let http = resp as? HTTPURLResponse else { throw APIError.badResponse }
        if http.statusCode == 401 { throw APIError.unauthorized }

        do { return try JSONDecoder().decode(T.self, from: data) }
        catch {
            let raw = String(data: data, encoding: .utf8) ?? ""
            throw APIError.server("Decode error. Raw:\n\(raw)")
        }
    }
}
