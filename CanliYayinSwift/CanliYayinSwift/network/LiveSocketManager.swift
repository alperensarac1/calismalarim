//
//  LiveSocketManager.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation

protocol LiveSocketManagerDelegate: AnyObject {
    func socketDidConnect()
    func socketDidReceiveMessage(_ message: String)
    func socketDidDisconnect()
    func socketDidReceiveError(_ error: String)
}

final class LiveSocketManager {

    weak var delegate: LiveSocketManagerDelegate?

    private var webSocketTask: URLSessionWebSocketTask?
    private let urlString: String

    init(urlString: String) {
        self.urlString = urlString
    }

    func connect() {
        guard let url = URL(string: urlString) else {
            delegate?.socketDidReceiveError("Geçersiz WebSocket URL")
            return
        }

        let session = URLSession(configuration: .default)
        webSocketTask = session.webSocketTask(with: url)
        webSocketTask?.resume()

        delegate?.socketDidConnect()

        listen()
    }

    private func listen() {
        webSocketTask?.receive { [weak self] result in
            guard let self else { return }

            switch result {
            case .success(let message):
                switch message {
                case .string(let text):
                    self.delegate?.socketDidReceiveMessage(text)

                case .data(let data):
                    if let text = String(data: data, encoding: .utf8) {
                        self.delegate?.socketDidReceiveMessage(text)
                    }

                @unknown default:
                    break
                }

                self.listen()

            case .failure(let error):
                self.delegate?.socketDidReceiveError(error.localizedDescription)
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
                self?.delegate?.socketDidReceiveError(error.localizedDescription)
            }
        }
    }

    func disconnect() {
        webSocketTask?.cancel(with: .normalClosure, reason: nil)
        webSocketTask = nil
        delegate?.socketDidDisconnect()
    }
}
