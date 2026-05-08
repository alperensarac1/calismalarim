//
//  RadioSocketManager.swift
//  OnlineRadioSwiftUI
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation

final class RadioSocketManager: NSObject, ObservableObject {

    static let shared = RadioSocketManager()

    private var webSocketTask: URLSessionWebSocketTask?

    // Python server çalışan bilgisayarın IP adresini yaz.
    private let serverUrl = URL(string: "ws://192.168.1.10:8765")!

    var onConnected: (() -> Void)?
    var onMessage: ((String) -> Void)?
    var onError: ((String) -> Void)?

    private override init() {
        super.init()
    }

    func connect() {
        if webSocketTask != nil {
            return
        }

        let session = URLSession(configuration: .default)
        webSocketTask = session.webSocketTask(with: serverUrl)

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

    func send(_ json: String) {
        let message = URLSessionWebSocketTask.Message.string(json)

        webSocketTask?.send(message) { error in
            if let error {
                DispatchQueue.main.async {
                    self.onError?(error.localizedDescription)
                }
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

    func close() {
        webSocketTask?.cancel(with: .goingAway, reason: nil)
        webSocketTask = nil
    }
}
