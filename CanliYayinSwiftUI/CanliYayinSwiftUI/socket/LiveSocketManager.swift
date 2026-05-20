//
//  LiveSocketManager.swift
//  CanliYayinSwiftUI
//
//  Created by Alperen Saraç on 14.05.2026.
//

import Foundation

final class LiveSocketManager: ObservableObject {

    private var webSocketTask: URLSessionWebSocketTask?

    var onConnected: (() -> Void)?
    var onMessage: ((String) -> Void)?
    var onDisconnected: (() -> Void)?
    var onError: ((String) -> Void)?

    func connect(urlString: String) {
        guard let url = URL(string: urlString) else {
            onError?("Geçersiz WebSocket URL")
            return
        }

        let session = URLSession(configuration: .default)
        webSocketTask = session.webSocketTask(with: url)
        webSocketTask?.resume()

        onConnected?()
        listen()
    }

    private func listen() {
        webSocketTask?.receive { [weak self] result in
            guard let self else { return }

            switch result {
            case .success(let message):
                switch message {
                case .string(let text):
                    self.onMessage?(text)

                case .data(let data):
                    if let text = String(data: data, encoding: .utf8) {
                        self.onMessage?(text)
                    }

                @unknown default:
                    break
                }

                self.listen()

            case .failure(let error):
                self.onError?(error.localizedDescription)
            }
        }
    }

    func sendJson(_ dictionary: [String: Any]) {
        guard let data = try? JSONSerialization.data(withJSONObject: dictionary),
              let text = String(data: data, encoding: .utf8) else {
            return
        }

        webSocketTask?.send(.string(text)) { [weak self] error in
            if let error {
                self?.onError?(error.localizedDescription)
            }
        }
    }

    func disconnect() {
        webSocketTask?.cancel(with: .normalClosure, reason: nil)
        webSocketTask = nil
        onDisconnected?()
    }
}
