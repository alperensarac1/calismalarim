//
//  SocketManager.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation

final class SocketManager {

    var onConnected: (() -> Void)?
    var onDisconnected: (() -> Void)?
    var onMessageReceived: ((String) -> Void)?
    var onError: ((String) -> Void)?

    private var webSocketTask: URLSessionWebSocketTask?

    func connect(token: String) {
        guard let url = URL(string: "\(Constants.wsBaseURL)?token=\(token)") else {
            onError?("Geçersiz WebSocket URL")
            return
        }

        webSocketTask = URLSession.shared.webSocketTask(with: url)
        webSocketTask?.resume()

        onConnected?()
        listen()
    }

    func sendPing() {
        send(text: #"{"event":"PING","data":{}}"#)
    }

    func send(text: String) {
        webSocketTask?.send(.string(text)) { [weak self] error in
            if let error {
                DispatchQueue.main.async {
                    self?.onError?(error.localizedDescription)
                }
            }
        }
    }

    func disconnect() {
        webSocketTask?.cancel(with: .normalClosure, reason: nil)
        webSocketTask = nil
        onDisconnected?()
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
                        self.onMessageReceived?(String(data: data, encoding: .utf8) ?? "")
                    @unknown default:
                        self.onError?("Bilinmeyen socket mesaj tipi")
                    }

                    self.listen()

                case .failure(let error):
                    self.onError?(error.localizedDescription)
                    self.onDisconnected?()
                }
            }
        }
    }
}
