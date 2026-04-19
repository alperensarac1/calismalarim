//
//  SocketEnvelope.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 10.04.2026.
//

import Foundation
struct SocketEnvelope: Decodable {
    let type: String
    let data: DataContainer?
}

struct DataContainer: Decodable {
    let raw: [String: Any]

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let decoded = try container.decode([String: AnyDecodable].self)
        raw = decoded.mapValues { $0.value }
    }
}
