//
//  AuthModels.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

struct RegisterRequest: Encodable {
    let full_name: String
    let phone: String
    let email: String?
    let password: String
    let role: String
}

struct LoginRequest: Encodable {
    let phone: String
    let password: String
}

struct AuthResponse: Decodable {
    let access_token: String
    let token_type: String
    let user_id: Int
    let full_name: String
    let role: String
}
