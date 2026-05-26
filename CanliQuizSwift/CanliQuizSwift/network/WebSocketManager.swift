//
//  WebSocketManager.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 21.05.2026.
//

import Foundation

// MARK: - WebSocket Event Delegate

protocol WebSocketManagerDelegate: AnyObject {
    func webSocketDidConnect()
    func webSocketDidReceiveMessage(_ message: String)
    func webSocketDidDisconnect()
    func webSocketDidReceiveError(_ error: String)
}

// MARK: - WebSocket Manager

final class WebSocketManager {

    /*
        Bu sınıf uygulamanın WebSocket bağlantısını yönetir.

        Android tarafında OkHttp kullanmıştık.
        Swift tarafında URLSessionWebSocketTask kullanıyoruz.

        Xcode Simulator ile Mac'teki Python server'a bağlanırken:
            ws://127.0.0.1:8765

        Fiziksel iPhone ile bağlanırken:
            ws://MAC_IP_ADRESI:8765
    */

    static let shared = WebSocketManager()

    private let serverURLString = "ws://127.0.0.1:8765"

    private var webSocketTask: URLSessionWebSocketTask?

    weak var delegate: WebSocketManagerDelegate?

    private(set) var isConnected: Bool = false

    private init() {}

    func connect() {
        if isConnected {
            DispatchQueue.main.async {
                self.delegate?.webSocketDidConnect()
            }
            return
        }

        guard let url = URL(string: serverURLString) else {
            DispatchQueue.main.async {
                self.delegate?.webSocketDidReceiveError("WebSocket URL geçersiz.")
            }
            return
        }

        let session = URLSession(configuration: .default)

        webSocketTask = session.webSocketTask(with: url)

        webSocketTask?.resume()

        isConnected = true

        DispatchQueue.main.async {
            self.delegate?.webSocketDidConnect()
        }

        listen()
    }

    private func listen() {
        /*
            URLSessionWebSocketTask mesajları tek tek dinler.

            Bir mesaj alındıktan sonra tekrar listen() çağırmak gerekir.
            Aksi halde sadece ilk mesajı alırız.
        */

        webSocketTask?.receive { [weak self] result in
            guard let self = self else { return }

            switch result {
            case .success(let message):
                switch message {
                case .string(let text):
                    DispatchQueue.main.async {
                        self.delegate?.webSocketDidReceiveMessage(text)
                    }

                case .data(let data):
                    let text = String(data: data, encoding: .utf8) ?? ""
                    DispatchQueue.main.async {
                        self.delegate?.webSocketDidReceiveMessage(text)
                    }

                @unknown default:
                    break
                }

                self.listen()

            case .failure(let error):
                self.isConnected = false

                DispatchQueue.main.async {
                    self.delegate?.webSocketDidReceiveError(error.localizedDescription)
                    self.delegate?.webSocketDidDisconnect()
                }
            }
        }
    }

    func send(_ text: String) {
        guard isConnected else {
            DispatchQueue.main.async {
                self.delegate?.webSocketDidReceiveError("WebSocket bağlı değil.")
            }
            return
        }

        let message = URLSessionWebSocketTask.Message.string(text)

        webSocketTask?.send(message) { [weak self] error in
            if let error = error {
                DispatchQueue.main.async {
                    self?.delegate?.webSocketDidReceiveError(error.localizedDescription)
                }
            }
        }
    }

    func disconnect() {
        isConnected = false

        webSocketTask?.cancel(
            with: .goingAway,
            reason: "Kullanıcı çıkış yaptı".data(using: .utf8)
        )

        webSocketTask = nil

        DispatchQueue.main.async {
            self.delegate?.webSocketDidDisconnect()
        }
    }
}
