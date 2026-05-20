//
//  ChatMessageModel.swift
//  CanliYayinSwiftUI
//
//  Created by Alperen Saraç on 14.05.2026.
//

import Foundation

struct ChatMessageModel: Identifiable {
    let id = UUID()
    let roomId: String
    let username: String
    let message: String
    let createdAt: String

    init(json: [String: Any]) {
        self.roomId = json["room_id"] as? String ?? ""
        self.username = json["username"] as? String ?? ""
        self.message = json["message"] as? String ?? ""
        self.createdAt = json["created_at"] as? String ?? ""
    }
}
