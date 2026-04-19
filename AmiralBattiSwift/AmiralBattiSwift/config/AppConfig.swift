import Foundation

struct AppConfig {

    static var serverPort: Int = 8080

    static var serverIP: String {
        #if targetEnvironment(simulator)
        return "127.0.0.1"
        #else
        return "10.19.82.112"
        #endif
    }

    static var webSocketURL: URL? {
        URL(string: "ws://\(serverIP):\(serverPort)")
    }

    static var httpBaseURL: String {
        "http://\(serverIP):\(serverPort)"
    }

    static var healthCheckURL: URL? {
        URL(string: "\(httpBaseURL)/")
    }

    static var roomsURL: URL? {
        URL(string: "\(httpBaseURL)/rooms")
    }
}
