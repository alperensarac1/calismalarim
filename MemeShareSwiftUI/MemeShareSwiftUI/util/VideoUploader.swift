//
//  VideoUploader.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
import UniformTypeIdentifiers

enum VideoUploaderError: Error, LocalizedError {
    case fileNotFound
    case invalidURL
    case invalidResponse
    case server(status: Int)
    case encodeFailed
    case unknown(Error)

    var errorDescription: String? {
        switch self {
        case .fileNotFound: return "Dosya bulunamadı."
        case .invalidURL: return "Geçersiz URL."
        case .invalidResponse: return "Geçersiz sunucu yanıtı."
        case .server(let code): return "Sunucu hatası: \(code)"
        case .encodeFailed: return "İstek gövdesi oluşturulamadı."
        case .unknown(let e): return e.localizedDescription
        }
    }
}

struct UploadResponseDTO: Decodable {
    let success: Bool
    let message: String
    let media_url: String?
}

final class VideoUploader {

    /// Android'deki `uploadVideo(...)`'ın Swift karşılığı
    /// - Parameters:
    ///   - videoName: Sunucuya gönderilecek dosya ismi (örn: "myvideo")
    ///   - fileURL: Cihaz içindeki .mp4 dosyası (Photos/Files'tan kopyalanmış)
    ///   - roomId / userId / caption: Form alanları
    ///   - uploadURL: `https://.../media-upload-video.php`
    ///   - completion: Ana thread'de çağrılır. (success, rawResponse / hata)
    static func uploadVideo(
        videoName: String,
        fileURL: URL,
        roomId: Int,
        userId: Int,
        caption: String,
        uploadURL: URL,
        completion: @escaping (_ success: Bool, _ responseString: String?) -> Void
    ) {
        // Dosya kontrolü
        guard FileManager.default.fileExists(atPath: fileURL.path) else {
            DispatchQueue.main.async { completion(false, "Dosya bulunamadı: \(fileURL.lastPathComponent)") }
            return
        }

        var request = URLRequest(url: uploadURL)
        request.httpMethod = "POST"

        let boundary = "Boundary-\(UUID().uuidString)"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        // Gövdeyi in-memory oluşturuyoruz (büyük dosyalarda streaming de yapılabilir)
        var body = Data()

        func appendField(name: String, value: String) {
            body.appendString("--\(boundary)\r\n")
            body.appendString("Content-Disposition: form-data; name=\"\(name)\"\r\n\r\n")
            body.appendString("\(value)\r\n")
        }

        func appendFile(name: String, fileName: String, mime: String, fileData: Data) {
            body.appendString("--\(boundary)\r\n")
            body.appendString("Content-Disposition: form-data; name=\"\(name)\"; filename=\"\(fileName)\"\r\n")
            body.appendString("Content-Type: \(mime)\r\n\r\n")
            body.append(fileData)
            body.appendString("\r\n")
        }

        // Text alanları
        appendField(name: "room_id", value: String(roomId))
        appendField(name: "user_id", value: String(userId))
        appendField(name: "caption", value: caption)

        // Dosya alanı
        do {
            let data = try Data(contentsOf: fileURL)
            let fileName = (videoName.isEmpty ? fileURL.deletingPathExtension().lastPathComponent : videoName) + ".mp4"
            appendFile(name: "video_file", fileName: fileName, mime: "video/mp4", fileData: data)
        } catch {
            DispatchQueue.main.async { completion(false, "Dosya okunamadı: \(error.localizedDescription)") }
            return
        }

        // Kapanış
        body.appendString("--\(boundary)--\r\n")
        request.httpBody = body

        // İstek
        URLSession.shared.dataTask(with: request) { data, response, error in
            func finish(_ ok: Bool, _ msg: String?) {
                DispatchQueue.main.async { completion(ok, msg) }
            }

            if let error = error {
                finish(false, "Bağlantı hatası: \(error.localizedDescription)")
                return
            }
            guard let http = response as? HTTPURLResponse, let data = data else {
                finish(false, "Geçersiz yanıt")
                return
            }
            guard (200..<300).contains(http.statusCode) else {
                let raw = String(data: data, encoding: .utf8)
                finish(false, "Sunucu hatası (\(http.statusCode)): \(raw ?? "")")
                return
            }

            // Android kodunda string içinde "success":true aranıyordu; burada decode etmeyi deniyoruz.
            let raw = String(data: data, encoding: .utf8)
            if let decoded = try? JSONDecoder().decode(UploadResponseDTO.self, from: data) {
                finish(decoded.success, raw)
            } else {
                // JSON beklenmedikse yine de "success":true var mı diye bak
                let ok = raw?.contains("\"success\":true") == true
                finish(ok, raw)
            }
        }.resume()
    }
}

// MARK: - Helper
private extension Data {
    mutating func appendString(_ s: String) {
        if let d = s.data(using: .utf8) { append(d) }
    }
}
