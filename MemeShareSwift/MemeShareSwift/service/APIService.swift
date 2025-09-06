//
//  APIService.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation

final class APIService {

    static let shared = APIService()

    /// Sunucu adresin
    private let baseURL = URL(string: "https://alperensaracdeneme.com/meme/")!

    private let session: URLSession = {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 60
        config.timeoutIntervalForResource = 180
        // Varsayılan Accept başlığı (tüm isteklerde JSON beklediğimizi belirtelim)
        config.httpAdditionalHeaders = [
            "Accept": "application/json"
        ]
        return URLSession(configuration: config)
    }()

    private let jsonDecoder: JSONDecoder = {
        let dec = JSONDecoder()
        // dec.keyDecodingStrategy = .convertFromSnakeCase // CodingKeys kullanıyorsan gerek yok
        return dec
    }()

    private init() {}

    // MARK: - Helpers

    private func makeURL(_ path: String, query: [URLQueryItem]? = nil) -> URL? {
        var comps = URLComponents(url: baseURL.appendingPathComponent(path), resolvingAgainstBaseURL: false)
        comps?.queryItems = query
        return comps?.url
    }

    private func urlEncodedBody(_ params: [String: String]) -> Data? {
        var comps = URLComponents()
        comps.queryItems = params.map { URLQueryItem(name: $0.key, value: $0.value) }
        return comps.query?.data(using: .utf8)
    }

    /// Teşhis amaçlı gelişmiş decode (status/ctype/raw body log’lanır)
    private func decode<T: Decodable>(_ type: T.Type, from data: Data, status: Int, response: URLResponse?) throws -> T {
        if let http = response as? HTTPURLResponse {
            let ctype = http.value(forHTTPHeaderField: "Content-Type") ?? "-"
            let raw = String(data: data, encoding: .utf8) ?? "<non-utf8>"
            // Teşhis log’u (gerekirse azalt ya da kaldır)
            print("🛰️ STATUS:", http.statusCode, "CT:", ctype)
            print("🛰️ BODY:", raw.prefix(2000))
        }

        guard (200..<300).contains(status) else {
            throw APIError.server(status: status)
        }
        do {
            return try jsonDecoder.decode(T.self, from: data)
        } catch {
            throw APIError.decodeFailed
        }
    }

    // MARK: - Endpoints

    /// GET media-get-all.php?room_id=...
    func getAllMedia(roomId: Int) async throws -> [GonderiModel] {
        guard let url = makeURL("media-get-all.php", query: [URLQueryItem(name: "room_id", value: "\(roomId)")])
        else { throw APIError.invalidURL }

        let (data, resp) = try await session.data(from: url)
        let status = (resp as? HTTPURLResponse)?.statusCode ?? -1
        return try decode([GonderiModel].self, from: data, status: status, response: resp)
    }

    /// GET rooms-join.php?user_id=&room_code=
    func joinRoom(userId: Int, roomCode: String) async throws -> SimpleResponse {
        let items = [
            URLQueryItem(name: "user_id", value: "\(userId)"),
            URLQueryItem(name: "room_code", value: roomCode)
        ]
        guard let url = makeURL("rooms-join.php", query: items) else { throw APIError.invalidURL }

        let (data, resp) = try await session.data(from: url)
        let status = (resp as? HTTPURLResponse)?.statusCode ?? -1
        return try decode(SimpleResponse.self, from: data, status: status, response: resp)
    }

    /// POST application/x-www-form-urlencoded  rooms-create.php
    func createRoom(userId: Int) async throws -> SimpleResponse {
        guard let url = makeURL("rooms-create.php") else { throw APIError.invalidURL }

        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/x-www-form-urlencoded; charset=utf-8", forHTTPHeaderField: "Content-Type")
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        req.httpBody = urlEncodedBody(["user_id": "\(userId)"])

        let (data, resp) = try await session.data(for: req)
        let status = (resp as? HTTPURLResponse)?.statusCode ?? -1
        return try decode(SimpleResponse.self, from: data, status: status, response: resp)
    }

    /// POST application/x-www-form-urlencoded users-login.php
    func login(username: String, password: String) async throws -> KullaniciResponse {
        guard let url = makeURL("users-login.php") else { throw APIError.invalidURL }

        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/x-www-form-urlencoded; charset=utf-8", forHTTPHeaderField: "Content-Type")
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        req.httpBody = urlEncodedBody(["username": username, "password": password])

        let (data, resp) = try await session.data(for: req)
        let status = (resp as? HTTPURLResponse)?.statusCode ?? -1
        return try decode(KullaniciResponse.self, from: data, status: status, response: resp)
    }

    /// POST application/x-www-form-urlencoded users-register.php
    func register(username: String, password: String) async throws -> KullaniciResponse {
        guard let url = makeURL("users-register.php") else { throw APIError.invalidURL }

        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/x-www-form-urlencoded; charset=utf-8", forHTTPHeaderField: "Content-Type")
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        req.httpBody = urlEncodedBody(["username": username, "password": password])

        let (data, resp) = try await session.data(for: req)
        let status = (resp as? HTTPURLResponse)?.statusCode ?? -1
        return try decode(KullaniciResponse.self, from: data, status: status, response: resp)
    }

    /// POST application/json media-upload-image.php (Base64)
    func uploadImageBase64(_ body: ImageUploadRequest) async throws -> UploadResponse {
        guard let url = makeURL("media-upload-image.php") else { throw APIError.invalidURL }

        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        req.httpBody = try JSONEncoder().encode(body)

        let (data, resp) = try await session.data(for: req)
        let status = (resp as? HTTPURLResponse)?.statusCode ?? -1
        return try decode(UploadResponse.self, from: data, status: status, response: resp)
    }

    /// POST multipart/form-data  media-upload-video.php
    /// - Parameter fileURL: cihazdaki .mp4 dosyasının URL’i (Photos’tan export edebilirsin)
    func uploadVideo(roomId: Int, userId: Int, caption: String, fileURL: URL) async throws -> UploadResponse {
        guard let url = makeURL("media-upload-video.php") else { throw APIError.invalidURL }
        guard FileManager.default.fileExists(atPath: fileURL.path) else { throw APIError.fileNotFound }

        var req = URLRequest(url: url)
        req.httpMethod = "POST"

        let boundary = "Boundary-\(UUID().uuidString)"
        req.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        req.setValue("application/json", forHTTPHeaderField: "Accept")

        // Body
        var body = Data()
        func appendFormField(name: String, value: String) {
            body.appendString("--\(boundary)\r\n")
            body.appendString("Content-Disposition: form-data; name=\"\(name)\"\r\n\r\n")
            body.appendString("\(value)\r\n")
        }
        func appendFileField(name: String, fileName: String, mime: String, fileData: Data) {
            body.appendString("--\(boundary)\r\n")
            body.appendString("Content-Disposition: form-data; name=\"\(name)\"; filename=\"\(fileName)\"\r\n")
            body.appendString("Content-Type: \(mime)\r\n\r\n")
            body.append(fileData)
            body.appendString("\r\n")
        }

        // text fields
        appendFormField(name: "room_id", value: String(roomId))
        appendFormField(name: "user_id", value: String(userId))
        appendFormField(name: "caption", value: caption)

        // file field
        let fileData = try Data(contentsOf: fileURL)
        appendFileField(name: "video_file", fileName: fileURL.lastPathComponent, mime: "video/mp4", fileData: fileData)

        // close
        body.appendString("--\(boundary)--\r\n")
        req.httpBody = body

        let (data, resp) = try await session.data(for: req)
        let status = (resp as? HTTPURLResponse)?.statusCode ?? -1
        return try decode(UploadResponse.self, from: data, status: status, response: resp)
    }

    /// GET rooms-get-joined.php?user_id=...
    func getJoinedRooms(userId: Int) async throws -> [OdaModel] {
        let items = [URLQueryItem(name: "user_id", value: "\(userId)")]
        guard let url = makeURL("rooms-get-joined.php", query: items) else { throw APIError.invalidURL }

        let (data, resp) = try await session.data(from: url)
        let status = (resp as? HTTPURLResponse)?.statusCode ?? -1
        return try decode([OdaModel].self, from: data, status: status, response: resp)
    }
}

// MARK: - Tiny helper
private extension Data {
    mutating func appendString(_ s: String) {
        if let d = s.data(using: .utf8) { append(d) }
    }
}
