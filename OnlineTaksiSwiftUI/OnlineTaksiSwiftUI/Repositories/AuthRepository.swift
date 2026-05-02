//
//  AuthRepository.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

final class AuthRepository {
    private let apiClient: APIClient

    init(apiClient: APIClient) {
        self.apiClient = apiClient
    }

    func login(phone: String, password: String) async throws -> AuthResponse {
        let request = LoginRequest(phone: phone, password: password)
        return try await apiClient.request(
            endpoint: .login,
            method: "POST",
            body: request,
            responseType: AuthResponse.self
        )
    }

    func register(
        fullName: String,
        phone: String,
        email: String?,
        password: String,
        role: String
    ) async throws -> AuthResponse {
        let request = RegisterRequest(
            full_name: fullName,
            phone: phone,
            email: email,
            password: password,
            role: role
        )

        return try await apiClient.request(
            endpoint: .register,
            method: "POST",
            body: request,
            responseType: AuthResponse.self
        )
    }
}
