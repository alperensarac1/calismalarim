//
//  KayitViewModel.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//
import SwiftUI
import Foundation
@MainActor
final class KayitViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    @Published var registerSucceeded = false

    func register(username: String, password: String, email: String) {
        guard !username.isEmpty, !password.isEmpty, !email.isEmpty else {
            self.errorMessage = "Tüm alanları doldurun"
            return
        }
        isLoading = true
        errorMessage = nil
        SozlukDao.shared.register(username: username, password: password, email: email) { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let response):
                    if response.success {
                        self.registerSucceeded = true
                    } else {
                        self.errorMessage = response.message
                    }
                case .failure:
                    self.errorMessage = "Bağlantı hatası"
                }
            }
        }
    }
}
