//
//  SessionManager.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation

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

    /*
        Kullanıcı bilgilerini kaydeder.
    */
    func saveUser(_ user: User) {
        defaults.set(user.id, forKey: Keys.userId)
        defaults.set(user.fullName, forKey: Keys.fullName)
        defaults.set(user.email, forKey: Keys.email)
        defaults.set(user.phone ?? "", forKey: Keys.phone)
        defaults.set(user.role, forKey: Keys.role)
        defaults.set(user.apiToken ?? "", forKey: Keys.apiToken)
        defaults.set(true, forKey: Keys.isLoggedIn)
    }

    /*
        Kullanıcı giriş yapmış mı?
    */
    var isLoggedIn: Bool {
        return defaults.bool(forKey: Keys.isLoggedIn)
    }

    var apiToken: String {
        return defaults.string(forKey: Keys.apiToken) ?? ""
    }

    var userId: Int {
        return defaults.integer(forKey: Keys.userId)
    }

    var fullName: String {
        return defaults.string(forKey: Keys.fullName) ?? ""
    }

    var email: String {
        return defaults.string(forKey: Keys.email) ?? ""
    }

    var phone: String {
        return defaults.string(forKey: Keys.phone) ?? ""
    }

    var role: String {
        return defaults.string(forKey: Keys.role) ?? "user"
    }

    /*
        QR kontrol ekranı sadece staff/admin kullanıcıya açık.
    */
    var isStaffOrAdmin: Bool {
        return role == "staff" || role == "admin"
    }

    /*
        Çıkış yapar.
    */
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
