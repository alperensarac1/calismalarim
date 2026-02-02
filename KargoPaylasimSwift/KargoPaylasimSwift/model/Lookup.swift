//
//  Lookup.swift
//  KargoPaylasimSwift
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
struct LookupReceiverReq: Encodable {
    let phone: String
}

struct LookupReceiverData: Decodable {
    let receiver_user_id: Int
    let masked_first_name: String
    let masked_last_name: String
}
