//
//  SessionManager.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

final class SessionManager: ObservableObject {
    @Published var token: String?
    @Published var userId: Int?
    @Published var fullName: String?
    @Published var role: String?

    init() {
        load()
    }

    func saveAuth(token: String, userId: Int, fullName: String, role: String) {
        UserDefaults.standard.set(token, forKey: "token")
        UserDefaults.standard.set(userId, forKey: "user_id")
        UserDefaults.standard.set(fullName, forKey: "full_name")
        UserDefaults.standard.set(role, forKey: "role")
        load()
    }

    func load() {
        token = UserDefaults.standard.string(forKey: "token")
        let id = UserDefaults.standard.integer(forKey: "user_id")
        userId = id == 0 ? nil : id
        fullName = UserDefaults.standard.string(forKey: "full_name")
        role = UserDefaults.standard.string(forKey: "role")
    }

    func clear() {
        UserDefaults.standard.removeObject(forKey: "token")
        UserDefaults.standard.removeObject(forKey: "user_id")
        UserDefaults.standard.removeObject(forKey: "full_name")
        UserDefaults.standard.removeObject(forKey: "role")
        load()
    }

    var isLoggedIn: Bool {
        guard let token, !token.trimmingCharacters(in: .whitespaces).isEmpty else {
            return false
        }
        return true
    }
}
