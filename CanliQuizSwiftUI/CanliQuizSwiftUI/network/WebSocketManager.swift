//
//  WebSocketManager.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation
protocol WebSocketManagerDelegate: AnyObject {
    func didConnect()
    func didReceiveMessage(_ message: String)
    func didDisconnect()
    func didReceiveError(_ error: String)
}

final class WebSocketManager {

    static let shared = WebSocketManager()

    private let serverURL = "ws://127.0.0.1:8765"

    private var task: URLSessionWebSocketTask?
    private(set) var isConnected = false

    weak var delegate: WebSocketManagerDelegate?

    private init() {}

    func connect() {
        if isConnected {
            DispatchQueue.main.async {
                self.delegate?.didConnect()
            }
            return
        }

        guard let url = URL(string: serverURL) else {
            delegate?.didReceiveError("WebSocket URL hatalı.")
            return
        }

        task = URLSession.shared.webSocketTask(with: url)
        task?.resume()

        isConnected = true

        DispatchQueue.main.async {
            self.delegate?.didConnect()
        }

        listen()
    }

    private func listen() {
        task?.receive { [weak self] result in
            guard let self else { return }

            switch result {
            case .success(let message):
                switch message {
                case .string(let text):
                    DispatchQueue.main.async {
                        self.delegate?.didReceiveMessage(text)
                    }

                case .data(let data):
                    let text = String(data: data, encoding: .utf8) ?? ""
                    DispatchQueue.main.async {
                        self.delegate?.didReceiveMessage(text)
                    }

                @unknown default:
                    break
                }

                self.listen()

            case .failure(let error):
                self.isConnected = false

                DispatchQueue.main.async {
                    self.delegate?.didReceiveError(error.localizedDescription)
                    self.delegate?.didDisconnect()
                }
            }
        }
    }

    func send(_ text: String) {
        guard isConnected else {
            delegate?.didReceiveError("WebSocket bağlı değil.")
            return
        }

        task?.send(.string(text)) { [weak self] error in
            if let error {
                DispatchQueue.main.async {
                    self?.delegate?.didReceiveError(error.localizedDescription)
                }
            }
        }
    }

    func disconnect() {
        isConnected = false
        task?.cancel(with: .goingAway, reason: nil)
        task = nil

        DispatchQueue.main.async {
            self.delegate?.didDisconnect()
        }
    }
}
