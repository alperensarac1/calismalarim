import Foundation

final class APIClient {
    private let baseURL: URL
    private let tokenStore: TokenStore

    init(baseURL: URL, tokenStore: TokenStore) {
        self.baseURL = baseURL
        self.tokenStore = tokenStore
    }

    private func applyAuth(_ req: inout URLRequest) {
        if let token = tokenStore.get(), !token.isEmpty {
            req.setValue(token, forHTTPHeaderField: "X-Auth-Token")
        }
    }

    func getJSON<Resp: Decodable>(_ path: String, query: [String: String] = [:], resp: Resp.Type) async throws -> Resp {
        var comps = URLComponents(url: baseURL.appendingPathComponent(path), resolvingAgainstBaseURL: false)!
        if !query.isEmpty {
            comps.queryItems = query.map { URLQueryItem(name: $0.key, value: $0.value) }
        }
        let url = comps.url!

        var req = URLRequest(url: url)
        req.httpMethod = "GET"
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        applyAuth(&req)

        let (data, response) = try await URLSession.shared.data(for: req)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            let txt = String(data: data, encoding: .utf8) ?? ""
            throw NSError(domain: "api", code: http.statusCode, userInfo: [NSLocalizedDescriptionKey: txt])
        }
        return try JSONDecoder().decode(Resp.self, from: data)
    }

    func postJSON<Body: Encodable, Resp: Decodable>(_ path: String, body: Body, resp: Resp.Type) async throws -> Resp {
        let url = baseURL.appendingPathComponent(path)
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        applyAuth(&req)
        req.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: req)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            let txt = String(data: data, encoding: .utf8) ?? ""
            throw NSError(domain: "api", code: http.statusCode, userInfo: [NSLocalizedDescriptionKey: txt])
        }
        return try JSONDecoder().decode(Resp.self, from: data)
    }
}
