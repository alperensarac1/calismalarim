import Foundation

final class UploadClient {

    static let endpoint = URL(string: "https://alperensaracdeneme.com/deneme/upload_csv.php")!

    struct UploadResponse: Decodable {
        let ok: Bool
        let id: String?
        let download_url: String?
        let error: String?
    }

    // ✅ PHP: $_FILES['file'] bekliyor -> fieldName = "file"
    static func uploadCsv(fileUrl: URL) async throws -> URL {
        var req = URLRequest(url: endpoint)
        req.httpMethod = "POST"

        let boundary = "Boundary-\(UUID().uuidString)"
        req.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        let fileData = try Data(contentsOf: fileUrl)
        let filename = fileUrl.lastPathComponent

        var body = Data()
        func append(_ s: String) { body.append(Data(s.utf8)) }

        append("--\(boundary)\r\n")
        append("Content-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\n")
        append("Content-Type: text/csv\r\n\r\n")
        body.append(fileData)
        append("\r\n")
        append("--\(boundary)--\r\n")

        req.httpBody = body

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard let http = resp as? HTTPURLResponse else {
            throw NSError(domain: "upload", code: -1, userInfo: [NSLocalizedDescriptionKey: "No HTTP response"])
        }

        // Sunucu JSON dönüyor; hata da JSON ile geliyor
        let decoded = (try? JSONDecoder().decode(UploadResponse.self, from: data))
        if !(200..<300).contains(http.statusCode) {
            let msg = decoded?.error ?? (String(data: data, encoding: .utf8) ?? "Upload failed")
            throw NSError(domain: "upload", code: http.statusCode, userInfo: [NSLocalizedDescriptionKey: msg])
        }

        guard let res = decoded, res.ok == true else {
            let msg = decoded?.error ?? "Unknown error"
            throw NSError(domain: "upload", code: -2, userInfo: [NSLocalizedDescriptionKey: msg])
        }

        guard let urlStr = res.download_url, let url = URL(string: urlStr) else {
            throw NSError(domain: "upload", code: -3, userInfo: [NSLocalizedDescriptionKey: "download_url missing"])
        }

        return url
    }
}
