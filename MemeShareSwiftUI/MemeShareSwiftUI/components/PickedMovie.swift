//
//  PickedMovie.swift
//  MemeShareSwiftUI
//
//  Created by Alperen Saraç on 2.09.2025.
//

import Foundation
import SwiftUI
// MARK: - Transferable: Video’yu temp’e kopyala
struct PickedMovie: Transferable {
    let url: URL

    static var transferRepresentation: some TransferRepresentation {
        FileRepresentation(contentType: .movie) { movie in
            SentTransferredFile(movie.url)
        } importing: { received in
            let src = received.file
            let ext = src.pathExtension.isEmpty ? "mp4" : src.pathExtension
            let dst = FileManager.default.temporaryDirectory
                .appendingPathComponent("\(UUID().uuidString).\(ext)")
            try? FileManager.default.removeItem(at: dst)
            try FileManager.default.copyItem(at: src, to: dst)
            return PickedMovie(url: dst)
        }
    }
}
