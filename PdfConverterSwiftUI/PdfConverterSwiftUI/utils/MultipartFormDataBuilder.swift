import Foundation

final class MultipartFormDataBuilder {
    private let boundary = "Boundary-\(UUID().uuidString)"
    private var body = Data()

    var contentType: String {
        "multipart/form-data; boundary=\(boundary)"
    }

    func addTextField(named name: String, value: String) {
        body.appendString("--\(boundary)\r\n")
        body.appendString("Content-Disposition: form-data; name=\"\(name)\"\r\n\r\n")
        body.appendString("\(value)\r\n")
    }

    func addFileField(
        named name: String,
        fileName: String,
        mimeType: String,
        fileData: Data
    ) {
        body.appendString("--\(boundary)\r\n")
        body.appendString("Content-Disposition: form-data; name=\"\(name)\"; filename=\"\(fileName)\"\r\n")
        body.appendString("Content-Type: \(mimeType)\r\n\r\n")
        body.append(fileData)
        body.appendString("\r\n")
    }

    func build() -> Data {
        var finalData = body
        finalData.appendString("--\(boundary)--\r\n")
        return finalData
    }

    func writeToTemporaryFile() throws -> URL {
        let tempURL = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString + ".tmp")

        try build().write(to: tempURL, options: .atomic)
        return tempURL
    }
}

private extension Data {
    mutating func appendString(_ string: String) {
        if let data = string.data(using: .utf8) {
            append(data)
        }
    }
}
