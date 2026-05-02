//
//  AuthViewModel.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

@MainActor
final class AuthViewModel: ObservableObject {
    @Published var loginState: ResourceState<String> = .idle
    @Published var registerState: ResourceState<String> = .idle

    private let authRepository: AuthRepository
    private let sessionManager: SessionManager

    init(authRepository: AuthRepository, sessionManager: SessionManager) {
        self.authRepository = authRepository
        self.sessionManager = sessionManager
    }

    func login(phone: String, password: String) async {
        loginState = .loading

        do {
            let response = try await authRepository.login(phone: phone, password: password)
            sessionManager.saveAuth(
                token: response.access_token,
                userId: response.user_id,
                fullName: response.full_name,
                role: response.role
            )
            loginState = .success(response.role)
        } catch {
            loginState = .failure(error.localizedDescription)
        }
    }

    func register(
        fullName: String,
        phone: String,
        email: String?,
        password: String
    ) async {
        registerState = .loading

        do {
            let response = try await authRepository.register(
                fullName: fullName,
                phone: phone,
                email: email,
                password: password,
                role: "customer"
            )

            sessionManager.saveAuth(
                token: response.access_token,
                userId: response.user_id,
                fullName: response.full_name,
                role: response.role
            )

            registerState = .success(response.role)
        } catch {
            registerState = .failure(error.localizedDescription)
        }
    }
}
