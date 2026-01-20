//
//  ApiClient.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 16.01.2026.
//

import Foundation


enum ApiError: Error, LocalizedError {
    case invalidURL
    case http(Int)
    case server(String)
    case decode
    case noData

    var errorDescription: String? {
        switch self {
        case .invalidURL: return "URL hatalı."
        case .http(let code): return "HTTP hata: \(code)"
        case .server(let msg): return msg
        case .decode: return "Yanıt çözümlenemedi."
        case .noData: return "Sunucudan veri gelmedi."
        }
    }
}

final class ApiClient {

    static let shared = ApiClient()
    private init() {}

    private let baseURL = URL(string: "https://alperensaracdeneme.com/eticaret/api/")!

    private let jsonDecoder: JSONDecoder = {
        let d = JSONDecoder()
        return d
    }()

    private func makeURL(_ path: String, query: [URLQueryItem] = []) throws -> URL {
        guard var comps = URLComponents(url: baseURL.appendingPathComponent(path), resolvingAgainstBaseURL: false)
        else { throw ApiError.invalidURL }
        if !query.isEmpty { comps.queryItems = query }
        guard let url = comps.url else { throw ApiError.invalidURL }
        return url
    }

    private func request<T: Decodable>(
        _ path: String,
        method: String,
        query: [URLQueryItem] = [],
        body: Data? = nil,
        auth: Bool = false
    ) async throws -> T {

        let url = try makeURL(path, query: query)
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Accept")

        if let body {
            req.httpBody = body
            req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }

        if auth, let token = AuthManager.shared.token, !token.isEmpty {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard let http = resp as? HTTPURLResponse else { throw ApiError.http(-1) }
        guard (200..<300).contains(http.statusCode) else { throw ApiError.http(http.statusCode) }

        // ApiResponse sarmalı ise:
        if T.self is ApiResponse<EmptyData>.Type {
            // bu yolu kullanmıyoruz; dursun
        }

        return try decode(data, as: T.self)
    }

    private func requestWrapped<T: Decodable>(
        _ path: String,
        method: String,
        query: [URLQueryItem] = [],
        body: Data? = nil,
        auth: Bool = false
    ) async throws -> T {

        let url = try makeURL(path, query: query)
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Accept")

        if let body {
            req.httpBody = body
            req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }

        if auth, let token = AuthManager.shared.token, !token.isEmpty {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        debugPrintRequest(req, auth: auth)

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard let http = resp as? HTTPURLResponse else { throw ApiError.http(-1) }

        debugPrintResponse(data: data, resp: http)

        guard (200..<300).contains(http.statusCode) else {
            let raw = String(data: data, encoding: .utf8) ?? ""
            if raw.contains("INVALID_CREDENTIALS") {
                throw ApiError.server("E-posta veya şifre hatalı.")
            }
            throw ApiError.server(raw.isEmpty ? "HTTP \(http.statusCode)" : raw)
        }

        let wrapped = try decode(data, as: ApiResponse<T>.self)
        if wrapped.ok, let d = wrapped.data { return d }
        throw ApiError.server(wrapped.error ?? "Bilinmeyen hata")
    }

    private func decode<T: Decodable>(_ data: Data, as type: T.Type) throws -> T {
        do {
            return try jsonDecoder.decode(T.self, from: data)
        } catch {
            let raw = String(data: data, encoding: .utf8) ?? "<utf8 değil>"
            print("❌ DECODE FAIL for:", T.self)
            print("RAW JSON:", raw)
            throw ApiError.decode
        }
    }


    func login(email: String, password: String) async throws -> LoginResponse {
        let body = try JSONEncoder().encode(LoginRequest(email: email, password: password))
        return try await requestWrapped(
            "auth.php",
            method: "POST",
            query: [URLQueryItem(name: "action", value: "login")],
            body: body,
            auth: false
        )
    }

    func register(name: String, email: String, password: String) async throws -> RegisterResponse {
        let body = try JSONEncoder().encode(RegisterRequest(name: name, email: email, password: password))
        return try await requestWrapped(
            "auth.php",
            method: "POST",
            query: [URLQueryItem(name: "action", value: "register")],
            body: body,
            auth: false
        )
    }


    func me() async throws -> UserDto {
        return try await requestWrapped("me", method: "GET", auth: true)
    }

    func getCategories() async throws -> [CategoryDto] {
        return try await requestWrapped("categories", method: "GET")
    }

    func getProducts(cat: Int? = nil, q: String? = nil, discount: Int? = nil,
                     sort: String? = nil, page: Int? = nil, per: Int? = nil) async throws -> ProductListPage {

        var items: [URLQueryItem] = []
        if let cat { items.append(.init(name: "cat", value: "\(cat)")) }
        if let q, !q.isEmpty { items.append(.init(name: "q", value: q)) }
        if let discount { items.append(.init(name: "discount", value: "\(discount)")) }
        if let sort, !sort.isEmpty { items.append(.init(name: "sort", value: sort)) }
        if let page { items.append(.init(name: "page", value: "\(page)")) }
        if let per { items.append(.init(name: "per", value: "\(per)")) }

        return try await requestWrapped("products", method: "GET", query: items)
    }

    func getProduct(id: Int) async throws -> ProductDto {
        return try await requestWrapped("products/\(id)", method: "GET")
    }

    // MARK: - Cart (php)
    func getCart() async throws -> CartDto {
        return try await requestWrapped("cart.php", method: "GET", auth: true)
    }

    func addToCart(productId: Int, quantity: Int) async throws -> AddToCartResponse {
        let body = try JSONEncoder().encode(AddToCartRequest(product_id: productId, quantity: quantity))
        return try await requestWrapped("cart_add.php", method: "POST", body: body, auth: true)
    }

    func updateCartItem(itemId: Int, quantity: Int) async throws -> BasicOk {
        let body = try JSONEncoder().encode(UpdateCartItemRequest(quantity: quantity))
        let q = [URLQueryItem(name: "id", value: "\(itemId)")]
        // Kotlin: POST cart_item.php?id=...
        return try await requestWrapped("cart_item.php", method: "POST", query: q, body: body, auth: true)
    }

    func deleteCartItem(itemId: Int) async throws -> BasicOk {
        let q = [URLQueryItem(name: "id", value: "\(itemId)")]
        return try await requestWrapped("cart_item.php", method: "DELETE", query: q, auth: true)
    }

    // MARK: - Checkout
    func checkout(addressLine1: String, city: String,
                  addressName: String? = nil,
                  addressLine2: String? = nil,
                  district: String? = nil,
                  postalCode: String? = nil,
                  idempotencyKey: String? = nil) async throws -> CheckoutResponse {

        let req = CheckoutRequest(
            idempotency_key: idempotencyKey,
            address_name: addressName,
            address_line1: addressLine1,
            address_line2: addressLine2,
            city: city,
            district: district,
            postal_code: postalCode
        )
        let body = try JSONEncoder().encode(req)
        return try await requestWrapped("checkout.php", method: "POST", body: body, auth: true)
    }
    private func debugPrintRequest(_ req: URLRequest, auth: Bool) {
        print("➡️ [API] \(req.httpMethod ?? "") \(req.url?.absoluteString ?? "")")
        print("   auth:", auth)

        if let headers = req.allHTTPHeaderFields {
            // token’ı maskeli bas
            if let authHeader = headers["Authorization"] {
                let masked = authHeader.prefix(20) + "..."  // "Bearer xxxxx..."
                print("   Authorization:", masked)
            } else {
                print("   Authorization: <yok>")
            }
            print("   Headers:", headers.filter { $0.key != "Authorization" })
        }

        if let body = req.httpBody, let s = String(data: body, encoding: .utf8) {
            print("   Body:", s)
        } else {
            print("   Body: <yok>")
        }
    }

    private func debugPrintResponse(data: Data, resp: HTTPURLResponse) {
        print("⬅️ [API] status:", resp.statusCode, "url:", resp.url?.absoluteString ?? "")
        let raw = String(data: data, encoding: .utf8) ?? "<utf8 değil>"
        print("   RAW:", raw)
    }

}

// yardımcı: ApiResponse<Empty> gibi şeyler gerekirse
struct EmptyData: Decodable {}
