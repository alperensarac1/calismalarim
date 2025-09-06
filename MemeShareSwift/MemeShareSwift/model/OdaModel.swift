//
//  OdaModel.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
struct OdaModel: Decodable {
    let odaId: Int
    let roomCode: String
    let createdBy: Int

    enum CodingKeys: String, CodingKey {
        case odaId     = "room_id"
        case roomCode  = "room_code"
        case createdBy = "created_by"
    }

    // ✅ Manuel init: view/controller tarafından elle oluştururken kullanacağız
    init(odaId: Int, roomCode: String, createdBy: Int) {
        self.odaId = odaId
        self.roomCode = roomCode
        self.createdBy = createdBy
    }

    // Decoder init: sunucu JSON'unu parse etmek için
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        self.odaId    = try c.decode(Int.self, forKey: .odaId)
        self.roomCode = try c.decode(String.self, forKey: .roomCode)

        // created_by bazen "2" (String), bazen 2 (Int) gelebiliyor:
        if let i = try? c.decode(Int.self, forKey: .createdBy) {
            self.createdBy = i
        } else if let s = try? c.decode(String.self, forKey: .createdBy),
                  let i = Int(s) {
            self.createdBy = i
        } else {
            self.createdBy = 0
        }
    }
}

