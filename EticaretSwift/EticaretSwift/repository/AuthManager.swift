import Foundation

final class AuthManager {

    static let shared = AuthManager()
    private init() {}

    static let changedNotification = Notification.Name("AuthManagerChanged")

    private let tokenKey = "auth_token"
    private let userIdKey = "auth_user_id"

    var isLoggedIn: Bool {
        token != nil
    }

    var token: String? {
        UserDefaults.standard.string(forKey: tokenKey)
    }

    var userId: Int? {
        let v = UserDefaults.standard.integer(forKey: userIdKey)
        return v == 0 ? nil : v
    }

    func setSession(token: String, userId: Int) {
        UserDefaults.standard.set(token, forKey: tokenKey)
        UserDefaults.standard.set(userId, forKey: userIdKey)
        notify()
    }

    func logout() {
        UserDefaults.standard.removeObject(forKey: tokenKey)
        UserDefaults.standard.removeObject(forKey: userIdKey)
        notify()
    }

    private func notify() {
        NotificationCenter.default.post(name: AuthManager.changedNotification, object: nil)
    }
}
