//
//  RoomModel.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation
struct RoomModel {
    let roomId: String
    let title: String
    let broadcasterName: String
    let createdAt: String
    let viewerCount: Int

    init(json: [String: Any]) {
        self.roomId = json["room_id"] as? String ?? ""
        self.title = json["title"] as? String ?? ""
        self.broadcasterName = json["broadcaster_name"] as? String ?? ""
        self.createdAt = json["created_at"] as? String ?? ""
        self.viewerCount = json["viewer_count"] as? Int ?? 0
    }
}
