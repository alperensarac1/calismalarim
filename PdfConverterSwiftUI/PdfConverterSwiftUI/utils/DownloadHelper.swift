//
//  DownloadHelper.swift
//  PdfConverterSwiftUI
//
//  Created by Alperen Saraç on 26.03.2026.
//

import Foundation
final class DownloadHelper: NSObject, ObservableObject, URLSessionDownloadDelegate {
    @Published var progress: Double = 0.0
    @Published var isDownloading = false
    @Published var downloadedFileURL: URL?
    @Published var errorText: String?

    private var destinationFileName: String = "downloaded_file"
    private lazy var session: URLSession = {
        let configuration = URLSessionConfiguration.default
        return URLSession(configuration: configuration, delegate: self, delegateQueue: .main)
    }()

    func downloadFile(from urlString: String) {
        guard let url = URL(string: urlString) else {
            errorText = "Geçersiz URL"
            return
        }

        destinationFileName = url.lastPathComponent
        progress = 0.0
        downloadedFileURL = nil
        errorText = nil
        isDownloading = true

        let task = session.downloadTask(with: url)
        task.resume()
    }

    func urlSession(
        _ session: URLSession,
        downloadTask: URLSessionDownloadTask,
        didWriteData bytesWritten: Int64,
        totalBytesWritten: Int64,
        totalBytesExpectedToWrite: Int64
    ) {
        guard totalBytesExpectedToWrite > 0 else { return }
        progress = Double(totalBytesWritten) / Double(totalBytesExpectedToWrite)
    }

    func urlSession(
        _ session: URLSession,
        downloadTask: URLSessionDownloadTask,
        didFinishDownloadingTo location: URL
    ) {
        let fileManager = FileManager.default

        do {
            let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!
            let destinationURL = documentsURL.appendingPathComponent(destinationFileName)

            if fileManager.fileExists(atPath: destinationURL.path) {
                try fileManager.removeItem(at: destinationURL)
            }

            try fileManager.moveItem(at: location, to: destinationURL)

            downloadedFileURL = destinationURL
            isDownloading = false
            progress = 1.0

        } catch {
            errorText = error.localizedDescription
            isDownloading = false
        }
    }

    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        if let error = error {
            errorText = error.localizedDescription
            isDownloading = false
        }
    }
}
