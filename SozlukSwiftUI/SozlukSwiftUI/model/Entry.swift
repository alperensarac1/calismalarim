//
//  Entry.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
struct Entry: Codable {
    let id: Int
    let user_id: Int?
    let username: String?   // list endpoint’inde bazen "1" dönüyor, nullable tutalım
    let title: String
    let content: String
    let created_at: String
}
