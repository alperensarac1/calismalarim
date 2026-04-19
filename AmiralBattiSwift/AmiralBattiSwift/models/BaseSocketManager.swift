//
//  BaseSocketManager.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 10.04.2026.
//

import Foundation
struct BaseSocketMessage: Decodable {
    let type: String
    let data: [String: AnyDecodable]?
}
