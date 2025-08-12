//
//  GirisYapViewModel.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//
import SwiftUI
import Foundation
@MainActor
final class GirisViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    @Published var loginSucceeded = false

    func login(username: String, password: String) {
        guard !username.isEmpty, !password.isEmpty else {
            self.errorMessage = "Kullanıcı adı ve şifre boş olamaz"
            return
        }
        isLoading = true
        errorMessage = nil
        SozlukDao.shared.login(username: username, password: password) { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let resp):
                    if resp.success, let uid = resp.user_id {
                        SessionManager.shared.saveUserSession(userId: uid, username: username)
                        self.loginSucceeded = true
                    } else {
                        self.errorMessage = resp.message ?? "Giriş başarısız"
                    }
                case .failure(let err):
                    self.errorMessage = err.localizedDescription
                }
            }
        }
    }
}
