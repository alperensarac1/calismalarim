//
//  SocketManager.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation

protocol SocketManagerDelegate: AnyObject {
    func socketDidConnect()
    func socketDidDisconnect()
    func socketDidReceiveMessage(_ text: String)
    func socketDidReceiveError(_ errorMessage: String)
}

final class SocketManager {

    static let shared = SocketManager()

    private init() {}

    private var webSocketTask: URLSessionWebSocketTask?
    private var session: URLSession?

    // delegate weak kalsın
    weak var delegate: SocketManagerDelegate?

    private(set) var isConnected: Bool = false

    func setDelegate(_ newDelegate: SocketManagerDelegate?) {
        delegate = newDelegate
    }

    // Sadece aktif delegate gerçekten bu instance ise temizle
    func clearDelegate(_ owner: SocketManagerDelegate) {
        if delegate === owner {
            delegate = nil
        }
    }

    func connect() {
        if isConnected {
            delegate?.socketDidConnect()
            return
        }

        guard let url = AppConfig.webSocketURL else {
            delegate?.socketDidReceiveError("Geçersiz WebSocket URL")
            return
        }

        session = URLSession(configuration: .default)
        webSocketTask = session?.webSocketTask(with: url)
        webSocketTask?.resume()

        // İstersen bunu daha sonra onOpen benzeri doğrulamayla iyileştiririz.
        isConnected = true
        delegate?.socketDidConnect()

        listen()
    }

    func disconnect() {
        webSocketTask?.cancel(with: .goingAway, reason: nil)
        webSocketTask = nil
        session = nil
        isConnected = false
        delegate?.socketDidDisconnect()
    }

    func send(dictionary: [String: Any]) {
        guard let data = try? JSONSerialization.data(withJSONObject: dictionary, options: []),
              let text = String(data: data, encoding: .utf8) else {
            delegate?.socketDidReceiveError("Mesaj JSON'a çevrilemedi")
            return
        }

        send(text: text)
    }

    func send(text: String) {
        webSocketTask?.send(.string(text)) { [weak self] error in
            if let error = error {
                DispatchQueue.main.async {
                    self?.delegate?.socketDidReceiveError(error.localizedDescription)
                }
            }
        }
    }

    private func listen() {
        webSocketTask?.receive { [weak self] result in
            guard let self = self else { return }

            switch result {
            case .failure(let error):
                DispatchQueue.main.async {
                    self.isConnected = false
                    self.delegate?.socketDidReceiveError(error.localizedDescription)
                    self.delegate?.socketDidDisconnect()
                }

            case .success(let message):
                DispatchQueue.main.async {
                    switch message {
                    case .string(let text):
                        self.delegate?.socketDidReceiveMessage(text)
                    case .data(let data):
                        let text = String(data: data, encoding: .utf8) ?? ""
                        self.delegate?.socketDidReceiveMessage(text)
                    @unknown default:
                        self.delegate?.socketDidReceiveError("Bilinmeyen mesaj tipi")
                    }
                }

                self.listen()
            }
        }
    }
}
