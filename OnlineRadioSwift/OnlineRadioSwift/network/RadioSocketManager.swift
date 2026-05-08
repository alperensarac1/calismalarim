//
//  RadioSocketManager.swift
//  OnlineRadioSwift
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation

final class RadioSocketManager {

    static let shared = RadioSocketManager()

    private var webSocketTask: URLSessionWebSocketTask?

    private let serverURL = URL(string: "ws://127.0.0.1:8765")!

    var onConnected: (() -> Void)?
    var onMessage: ((String) -> Void)?
    var onError: ((String) -> Void)?

    private init() {}

    func connect() {
        if webSocketTask != nil { return }

        let session = URLSession(configuration: .default)
        webSocketTask = session.webSocketTask(with: serverURL)
        webSocketTask?.resume()

        DispatchQueue.main.async {
            self.onConnected?()
        }

        listen()
    }

    private func listen() {
        webSocketTask?.receive { [weak self] result in
            guard let self else { return }

            switch result {
            case .success(let message):
                switch message {
                case .string(let text):
                    DispatchQueue.main.async {
                        self.onMessage?(text)
                    }

                case .data(let data):
                    if let text = String(data: data, encoding: .utf8) {
                        DispatchQueue.main.async {
                            self.onMessage?(text)
                        }
                    }

                @unknown default:
                    break
                }

                self.listen()

            case .failure(let error):
                DispatchQueue.main.async {
                    self.onError?(error.localizedDescription)
                }
                self.webSocketTask = nil
            }
        }
    }

    func getRooms() {
        send("""
        {"type":"GET_ROOMS"}
        """)
    }

    func joinRoom(roomId: Int) {
        send("""
        {"type":"JOIN_ROOM","roomId":\(roomId)}
        """)
    }

    func requestSync(roomId: Int) {
        send("""
        {"type":"SYNC_REQUEST","roomId":\(roomId)}
        """)
    }

    private func send(_ text: String) {
        webSocketTask?.send(.string(text)) { error in
            if let error {
                DispatchQueue.main.async {
                    self.onError?(error.localizedDescription)
                }
            }
        }
    }
}
