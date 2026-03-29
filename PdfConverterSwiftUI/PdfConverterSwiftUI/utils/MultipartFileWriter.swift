//
//  MultipartFileWriter.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 28.03.2026.
//

import Foundation

final class MultipartFileWriter {
    let boundary = "Boundary-\(UUID().uuidString)"

    var contentType: String {
        "multipart/form-data; boundary=\(boundary)"
    }

    func createMultipartFile(
        textFields: [String: String],
        fileFields: [(name: String, fileURL: URL, mimeType: String)]
    ) throws -> URL {
        let tempURL = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString + ".multipart")

        FileManager.default.createFile(atPath: tempURL.path, contents: nil)

        let handle = try FileHandle(forWritingTo: tempURL)
        defer {
            try? handle.close()
        }

        for (key, value) in textFields {
            try handle.write(contentsOf: Data("--\(boundary)\r\n".utf8))
            try handle.write(contentsOf: Data("Content-Disposition: form-data; name=\"\(key)\"\r\n\r\n".utf8))
            try handle.write(contentsOf: Data("\(value)\r\n".utf8))
        }

        for field in fileFields {
            try handle.write(contentsOf: Data("--\(boundary)\r\n".utf8))
            try handle.write(contentsOf: Data("Content-Disposition: form-data; name=\"\(field.name)\"; filename=\"\(field.fileURL.lastPathComponent)\"\r\n".utf8))
            try handle.write(contentsOf: Data("Content-Type: \(field.mimeType)\r\n\r\n".utf8))

            let fileData = try Data(contentsOf: field.fileURL)
            try handle.write(contentsOf: fileData)
            try handle.write(contentsOf: Data("\r\n".utf8))
        }

        try handle.write(contentsOf: Data("--\(boundary)--\r\n".utf8))

        return tempURL
    }
}
