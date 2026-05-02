//
//  SessionManager.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation


final class SessionManager {

    static let shared = SessionManager()
    private init() {}

    func saveAuth(token: String, userId: Int, fullName: String, role: String) {
        UserDefaults.standard.set(token, forKey: "token")
        UserDefaults.standard.set(userId, forKey: "user_id")
        UserDefaults.standard.set(fullName, forKey: "full_name")
        UserDefaults.standard.set(role, forKey: "role")
    }

    var token: String? {
        UserDefaults.standard.string(forKey: "token")
    }

    var userId: Int {
        UserDefaults.standard.integer(forKey: "user_id")
    }

    var fullName: String? {
        UserDefaults.standard.string(forKey: "full_name")
    }

    var role: String? {
        UserDefaults.standard.string(forKey: "role")
    }

    var isLoggedIn: Bool {
        guard let token = token else { return false }
        return !token.trimmingCharacters(in: .whitespaces).isEmpty
    }

    func clear() {
        UserDefaults.standard.removeObject(forKey: "token")
        UserDefaults.standard.removeObject(forKey: "user_id")
        UserDefaults.standard.removeObject(forKey: "full_name")
        UserDefaults.standard.removeObject(forKey: "role")
    }
}
