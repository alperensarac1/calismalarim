//
//  Session.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation

enum Session {
    private static let kId = "yardim_user_id"
    private static let kRole = "yardim_role"

    static func save(id: Int, role: Role) {
        UserDefaults.standard.set(id, forKey: kId)
        UserDefaults.standard.set(role.rawValue, forKey: kRole)
    }

    static func clear() {
        UserDefaults.standard.removeObject(forKey: kId)
        UserDefaults.standard.removeObject(forKey: kRole)
    }

    static func isLoggedIn() -> Bool {
        userId() > 0 && (role() != nil)
    }

    static func userId() -> Int {
        UserDefaults.standard.integer(forKey: kId)
    }

    static func role() -> Role? {
        guard let s = UserDefaults.standard.string(forKey: kRole) else { return nil }
        return Role(rawValue: s)
    }
}
