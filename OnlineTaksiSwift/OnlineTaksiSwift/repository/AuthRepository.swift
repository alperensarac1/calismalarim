//
//  AuthRepository.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation

final class AuthRepository {

    func login(phone: String, password: String, completion: @escaping (Result<AuthResponse, Error>) -> Void) {
        let request = LoginRequest(phone: phone, password: password)

        APIClient.shared.request(
            endpoint: .login,
            method: "POST",
            body: request,
            responseType: AuthResponse.self,
            completion: completion
        )
    }

    func register(
        fullName: String,
        phone: String,
        email: String?,
        password: String,
        role: String,
        completion: @escaping (Result<AuthResponse, Error>) -> Void
    ) {
        let request = RegisterRequest(
            full_name: fullName,
            phone: phone,
            email: email,
            password: password,
            role: role
        )

        APIClient.shared.request(
            endpoint: .register,
            method: "POST",
            body: request,
            responseType: AuthResponse.self,
            completion: completion
        )
    }
}
