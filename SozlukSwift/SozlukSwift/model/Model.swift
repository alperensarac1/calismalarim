//
//  Model.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

// Models.swift
import Foundation
struct SimpleResponse: Codable {
    let success: Bool
    let message: String?
}

struct LoginResponse: Codable {
    let success: Bool
    let user_id: Int?
    let message: String?
}

struct Entry: Codable {
    let id: Int
    let user_id: Int?
    let username: String?   // list endpoint’inde bazen "1" dönüyor, nullable tutalım
    let title: String
    let content: String
    let created_at: String
}

struct Comment: Codable {
    let id: Int
    let entry_id: Int
    let user_id: Int
    let username: String
    let comment_text: String
    let likes: Int
    let dislikes: Int
    let created_at: String
}
