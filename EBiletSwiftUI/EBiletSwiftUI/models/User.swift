//
//  User.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation

/*
    User

    Kullanıcı modelidir.

    Backend alanları:
    id
    full_name
    email
    phone
    role
    api_token
    created_at
*/
struct User: Codable, Identifiable {
    let id: Int
    let fullName: String
    let email: String
    let phone: String?
    let role: String
    let apiToken: String?
    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case id
        case fullName = "full_name"
        case email
        case phone
        case role
        case apiToken = "api_token"
        case createdAt = "created_at"
    }
}
