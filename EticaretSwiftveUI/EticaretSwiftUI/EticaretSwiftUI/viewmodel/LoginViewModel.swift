//
//  LoginViewModel.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI

@MainActor
final class LoginVM: ObservableObject {
    @Published var email = ""
    @Published var password = ""
    @Published var isLoading = false
    @Published var errorMessage: String?

    func login() async -> LoginResponse? {
        isLoading = true
        errorMessage = nil
        do {
            let res = try await ApiClient.shared.login(email: email, password: password)
            isLoading = false
            return res
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
            return nil
        }
    }
}
