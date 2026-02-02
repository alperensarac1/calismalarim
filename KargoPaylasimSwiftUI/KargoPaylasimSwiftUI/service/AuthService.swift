//
//  AuthService.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
import Foundation

final class AuthService {
    private let api: APIClient
    init(api: APIClient) { self.api = api }

    func login(phoneE164: String, password: String) async throws -> LoginData {
        let res: ApiResp<LoginData> = try await api.postJSON(
            .login, // -> user_login.php
            body: LoginReq(phone: phoneE164, password: password),
            as: ApiResp<LoginData>.self
        )
        guard res.ok, let d = res.data else {
            throw APIError.server(res.error ?? "Invalid credentials")
        }
        return d
    }

    func register(_ req: RegisterReq) async throws -> RegisterData {
        let res: ApiResp<RegisterData> = try await api.postJSON(
            .register, // -> user_register.php
            body: req,
            as: ApiResp<RegisterData>.self
        )
        guard res.ok, let d = res.data else {
            throw APIError.server(res.error ?? "Register failed")
        }
        return d
    }
}
