//
//  TokenStore.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation

final class TokenStore: ObservableObject {
    @Published var token: String? {
        didSet { UserDefaults.standard.set(token, forKey: "cargo_token") }
    }

    init() {
        self.token = UserDefaults.standard.string(forKey: "cargo_token")
    }

    func clear() { token = nil }
}
