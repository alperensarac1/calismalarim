//
//  SocketManager.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

final class SocketManager: ObservableObject {
    @Published var isConnected: Bool = false

    var onMessageReceived: ((String) -> Void)?
    var onError: ((String) -> Void)?

    private var webSocketTask: URLSessionWebSocketTask?

    func connect(token: String) {
        guard let url = URL(string: "\(Constants.wsBaseURL)?token=\(token)") else {
            onError?("Geçersiz WebSocket URL")
            return
        }

        let session = URLSession(configuration: .default)
        webSocketTask = session.webSocketTask(with: url)
        webSocketTask?.resume()

        isConnected = true
        listen()
    }

    func disconnect() {
        webSocketTask?.cancel(with: .normalClosure, reason: nil)
        webSocketTask = nil
        isConnected = false
    }

    func sendPing() {
        let pingJson = """
        {"event":"PING","data":{}}
        """
        send(text: pingJson)
    }

    func send(text: String) {
        webSocketTask?.send(.string(text)) { [weak self] error in
            if let error = error {
                DispatchQueue.main.async {
                    self?.onError?(error.localizedDescription)
                }
            }
        }
    }

    private func listen() {
        webSocketTask?.receive { [weak self] result in
            guard let self else { return }

            DispatchQueue.main.async {
                switch result {
                case .success(let message):
                    switch message {
                    case .string(let text):
                        self.onMessageReceived?(text)
                    case .data(let data):
                        let text = String(data: data, encoding: .utf8) ?? ""
                        self.onMessageReceived?(text)
                    @unknown default:
                        self.onError?("Bilinmeyen socket mesaj tipi")
                    }

                    self.listen()

                case .failure(let error):
                    self.isConnected = false
                    self.onError?(error.localizedDescription)
                }
            }
        }
    }
}
