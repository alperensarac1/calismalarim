//
//  ApiService.swift
//  DosyaPaylasimSwift
//
//  Created by Alperen Saraç on 7.09.2025.
//

import Foundation

class ApiService {
    static let shared = ApiService()
    private let baseURL = URL(string: "https://alperensaracdeneme.com/api/")!
    
    private init() {}
    
    // MARK: - Upload (multipart/form-data)
    func uploadFile(fileURL: URL, completion: @escaping (Result<UploadResponse, Error>) -> Void) {
        let url = baseURL.appendingPathComponent("upload.php")
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let boundary = "Boundary-\(UUID().uuidString)"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        var body = Data()
        
        // Dosya adı ve MIME type
        let filename = fileURL.lastPathComponent
        let mimeType = mimeTypeForPath(path: filename)
        
        // Part: file
        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: \(mimeType)\r\n\r\n".data(using: .utf8)!)
        if let fileData = try? Data(contentsOf: fileURL) {
            body.append(fileData)
        }
        body.append("\r\n".data(using: .utf8)!)
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)
        
        request.httpBody = body
        
        // İstek
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            guard let data = data else {
                completion(.failure(NSError(domain: "ApiService", code: -1, userInfo: [NSLocalizedDescriptionKey: "No data"])))
                return
            }
            do {
                let decoded = try JSONDecoder().decode(UploadResponse.self, from: data)
                completion(.success(decoded))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
    
    // MARK: - Get Link
    func getLink(code: String, completion: @escaping (Result<LinkResponse, Error>) -> Void) {
        var components = URLComponents(url: baseURL.appendingPathComponent("get-link.php"), resolvingAgainstBaseURL: false)!
        components.queryItems = [URLQueryItem(name: "code", value: code)]
        
        guard let url = components.url else {
            completion(.failure(NSError(domain: "ApiService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"])))
            return
        }
        
        URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            guard let data = data else {
                completion(.failure(NSError(domain: "ApiService", code: -1, userInfo: [NSLocalizedDescriptionKey: "No data"])))
                return
            }
            do {
                let decoded = try JSONDecoder().decode(LinkResponse.self, from: data)
                completion(.success(decoded))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
    
    // Yardımcı: MIME type
    private func mimeTypeForPath(path: String) -> String {
        if path.hasSuffix(".jpg") || path.hasSuffix(".jpeg") { return "image/jpeg" }
        if path.hasSuffix(".png") { return "image/png" }
        if path.hasSuffix(".mp4") { return "video/mp4" }
        if path.hasSuffix(".pdf") { return "application/pdf" }
        return "application/octet-stream"
    }
}
