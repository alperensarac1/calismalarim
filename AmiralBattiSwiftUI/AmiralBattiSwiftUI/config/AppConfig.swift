//
//  AppConfig.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
struct AppConfig {
    static var serverIP: String = "10.19.82.112"
    static var serverPort: Int = 8080

    static var webSocketURL: URL? {
        URL(string: "ws://\(serverIP):\(serverPort)")
    }

    static var httpBaseURL: String {
        "http://\(serverIP):\(serverPort)/"
    }
}
