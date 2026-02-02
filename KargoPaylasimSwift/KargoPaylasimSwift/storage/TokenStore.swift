import Foundation

final class TokenStore {
    private let key = "cargo_auth_token"

    func save(_ token: String) {
        UserDefaults.standard.set(token, forKey: key)
    }

    func get() -> String? {
        UserDefaults.standard.string(forKey: key)
    }

    func isLoggedIn() -> Bool {
        (get() ?? "").isEmpty == false
    }

    func clear() {
        UserDefaults.standard.removeObject(forKey: key)
    }
}
