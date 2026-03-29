//
//  DownloadHelper.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation

final class DownloadHelper: NSObject {

    var onProgressChanged: ((Double) -> Void)?
    var onCompleted: ((URL) -> Void)?
    var onError: ((String) -> Void)?
    var onStateChanged: ((Bool) -> Void)?

    private var destinationFileName: String = "downloaded_file"
    private lazy var session: URLSession = {
        let configuration = URLSessionConfiguration.default
        return URLSession(configuration: configuration, delegate: self, delegateQueue: .main)
    }()

    func downloadFile(from urlString: String) {
        guard let url = URL(string: urlString) else {
            onError?("Geçersiz URL")
            return
        }

        destinationFileName = url.lastPathComponent
        onProgressChanged?(0.0)
        onStateChanged?(true)

        let task = session.downloadTask(with: url)
        task.resume()
    }
}

extension DownloadHelper: URLSessionDownloadDelegate {

    func urlSession(
        _ session: URLSession,
        downloadTask: URLSessionDownloadTask,
        didWriteData bytesWritten: Int64,
        totalBytesWritten: Int64,
        totalBytesExpectedToWrite: Int64
    ) {
        guard totalBytesExpectedToWrite > 0 else { return }

        let progress = Double(totalBytesWritten) / Double(totalBytesExpectedToWrite)
        onProgressChanged?(progress)
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

            onStateChanged?(false)
            onProgressChanged?(1.0)
            onCompleted?(destinationURL)

        } catch {
            onStateChanged?(false)
            onError?(error.localizedDescription)
        }
    }

    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        if let error = error {
            onStateChanged?(false)
            onError?(error.localizedDescription)
        }
    }
}
