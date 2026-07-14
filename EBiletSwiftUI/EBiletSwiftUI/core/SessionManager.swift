//
//  SessionManager.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

/*
    SessionManager

    Android'deki SharedPreferences karşılığı iOS'ta UserDefaults'tur.

    Kullanıcı giriş yaptıktan sonra:
    - api_token
    - user_id
    - full_name
    - email
    - phone
    - role
    - is_logged_in

    bilgilerini saklarız.
*/
final class SessionManager {

    static let shared = SessionManager()

    private let defaults = UserDefaults.standard

    private init() {}

    private enum Keys {
        static let userId = "user_id"
        static let fullName = "full_name"
        static let email = "email"
        static let phone = "phone"
        static let role = "role"
        static let apiToken = "api_token"
        static let isLoggedIn = "is_logged_in"
    }

    func saveUser(_ user: User) {
        defaults.set(user.id, forKey: Keys.userId)
        defaults.set(user.fullName, forKey: Keys.fullName)
        defaults.set(user.email, forKey: Keys.email)
        defaults.set(user.phone ?? "", forKey: Keys.phone)
        defaults.set(user.role, forKey: Keys.role)
        defaults.set(user.apiToken ?? "", forKey: Keys.apiToken)
        defaults.set(true, forKey: Keys.isLoggedIn)
    }

    var isLoggedIn: Bool {
        defaults.bool(forKey: Keys.isLoggedIn)
    }

    var apiToken: String {
        defaults.string(forKey: Keys.apiToken) ?? ""
    }

    var userId: Int {
        defaults.integer(forKey: Keys.userId)
    }

    var fullName: String {
        defaults.string(forKey: Keys.fullName) ?? ""
    }

    var email: String {
        defaults.string(forKey: Keys.email) ?? ""
    }

    var phone: String {
        defaults.string(forKey: Keys.phone) ?? ""
    }

    var role: String {
        defaults.string(forKey: Keys.role) ?? "user"
    }

    var isStaffOrAdmin: Bool {
        role == "staff" || role == "admin"
    }

    func logout() {
        defaults.removeObject(forKey: Keys.userId)
        defaults.removeObject(forKey: Keys.fullName)
        defaults.removeObject(forKey: Keys.email)
        defaults.removeObject(forKey: Keys.phone)
        defaults.removeObject(forKey: Keys.role)
        defaults.removeObject(forKey: Keys.apiToken)
        defaults.removeObject(forKey: Keys.isLoggedIn)
    }
}
