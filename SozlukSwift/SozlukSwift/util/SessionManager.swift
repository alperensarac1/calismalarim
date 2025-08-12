//
//  SessionManager.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation

final class SessionManager {

    static let shared = SessionManager()
    private init() {}

    private let defaults = UserDefaults.standard

    private enum Keys {
        static let userId = "user_id"
        static let username = "username"
        static let isLoggedIn = "is_logged_in"
    }

    func saveUserSession(userId: Int, username: String) {
        defaults.set(userId, forKey: Keys.userId)
        defaults.set(username, forKey: Keys.username)
        defaults.set(true, forKey: Keys.isLoggedIn)
        defaults.synchronize()
    }

    func isLoggedIn() -> Bool {
        return defaults.bool(forKey: Keys.isLoggedIn)
    }

    func getUserId() -> Int {
        let value = defaults.integer(forKey: Keys.userId)
        return value == 0 && defaults.object(forKey: Keys.userId) == nil ? -1 : value
    }

    func getUsername() -> String? {
        return defaults.string(forKey: Keys.username)
    }

    func clearSession() {
        defaults.removeObject(forKey: Keys.userId)
        defaults.removeObject(forKey: Keys.username)
        defaults.set(false, forKey: Keys.isLoggedIn)
        defaults.synchronize()
    }
}
