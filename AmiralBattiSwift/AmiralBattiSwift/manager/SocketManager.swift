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

    weak var delegate: SocketManagerDelegate?

    private(set) var isConnected: Bool = false

    // MARK: - CONNECT

    func connect() {
        if isConnected {
            print("[WS] zaten bağlı")
            delegate?.socketDidConnect()
            return
        }

        guard let url = AppConfig.webSocketURL else {
            delegate?.socketDidReceiveError("Geçersiz WebSocket URL")
            return
        }

        print("========== WS CONNECT ==========")
        print("URL: \(url.absoluteString)")
        print("================================")

        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 1000
        config.timeoutIntervalForResource = 1000

        session = URLSession(configuration: config)
        webSocketTask = session?.webSocketTask(with: url)

        webSocketTask?.resume()

        // ⚠️ BURADA CONNECT DEMİYORUZ!
        waitForConnection()

        listen()
    }

    // MARK: - WAIT CONNECTION (KRİTİK)

    private func waitForConnection() {
        // Ping at → başarılıysa bağlı
        webSocketTask?.sendPing { [weak self] error in
            DispatchQueue.main.async {
                if let error = error {
                    print("[WS CONNECT FAIL] \(error.localizedDescription)")
                    self?.isConnected = false
                    self?.delegate?.socketDidReceiveError("Bağlantı kurulamadı: \(error.localizedDescription)")
                    self?.delegate?.socketDidDisconnect()
                } else {
                    print("[WS CONNECT SUCCESS]")
                    self?.isConnected = true
                    self?.delegate?.socketDidConnect()
                }
            }
        }
    }

    // MARK: - DISCONNECT

    func disconnect() {
        print("[WS] disconnect")

        webSocketTask?.cancel(with: .goingAway, reason: nil)
        webSocketTask = nil
        session = nil
        isConnected = false

        delegate?.socketDidDisconnect()
    }

    // MARK: - SEND (DICT)

    func send(dictionary: [String: Any]) {
        do {
            let data = try JSONSerialization.data(withJSONObject: dictionary, options: [])
            guard let text = String(data: data, encoding: .utf8) else {
                throw NSError(domain: "JSON", code: 0)
            }

            print("[WS SEND DICT] \(text)")
            send(text: text)

        } catch {
            delegate?.socketDidReceiveError("JSON hatası: \(error.localizedDescription)")
        }
    }

    // MARK: - SEND (TEXT)

    func send(text: String) {
        guard isConnected else {
            print("[WS SEND FAIL] bağlı değil")
            delegate?.socketDidReceiveError("Socket bağlı değil")
            return
        }

        print("[WS SEND TEXT] \(text)")

        webSocketTask?.send(.string(text)) { [weak self] error in
            DispatchQueue.main.async {
                if let error = error {
                    print("[WS SEND ERROR] \(error.localizedDescription)")
                    self?.delegate?.socketDidReceiveError(error.localizedDescription)
                } else {
                    print("[WS SEND SUCCESS]")
                }
            }
        }
    }

    // MARK: - LISTEN

    private func listen() {
        webSocketTask?.receive { [weak self] result in
            guard let self else { return }

            switch result {

            case .failure(let error):
                print("[WS RECEIVE ERROR] \(error.localizedDescription)")

                self.isConnected = false

                DispatchQueue.main.async {
                    self.delegate?.socketDidReceiveError(error.localizedDescription)
                    self.delegate?.socketDidDisconnect()
                }

            case .success(let message):

                switch message {

                case .string(let text):
                    print("[WS RECEIVED TEXT] \(text)")
                    DispatchQueue.main.async {
                        self.delegate?.socketDidReceiveMessage(text)
                    }

                case .data(let data):
                    let text = String(data: data, encoding: .utf8) ?? ""
                    print("[WS RECEIVED DATA] \(text)")

                    DispatchQueue.main.async {
                        self.delegate?.socketDidReceiveMessage(text)
                    }

                @unknown default:
                    print("[WS UNKNOWN MESSAGE]")
                    DispatchQueue.main.async {
                        self.delegate?.socketDidReceiveError("Bilinmeyen mesaj tipi")
                    }
                }

                self.listen()
            }
        }
    }
}
