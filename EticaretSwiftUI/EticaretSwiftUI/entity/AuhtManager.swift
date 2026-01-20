//
//  AuhtManager.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation

final class AuthManager: ObservableObject {
    static let shared = AuthManager()

    @Published private(set) var token: String?
    @Published private(set) var userId: Int?

    var isLoggedIn: Bool { (token?.isEmpty == false) }

    private init() {
        token = UserDefaults.standard.string(forKey: "token")
        userId = UserDefaults.standard.object(forKey: "userId") as? Int
    }

    func setSession(token: String, userId: Int) {
        self.token = token
        self.userId = userId
        UserDefaults.standard.set(token, forKey: "token")
        UserDefaults.standard.set(userId, forKey: "userId")
    }

    func logout() {
        token = nil
        userId = nil
        UserDefaults.standard.removeObject(forKey: "token")
        UserDefaults.standard.removeObject(forKey: "userId")
    }
}
