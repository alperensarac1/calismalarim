//
//  Address.swift
//  KargoPaylasimSwift
//
//  Created by Alperen Saraç on 29.01.2026.
//

import Foundation
import Foundation


struct AddressListData: Decodable {
    let items: [Address]
}

struct Address: Decodable {
    let id: Int
    let title: String
    let city: String
    let district: String
    let address_line: String
    let is_default: Int
}
struct AddressCreateReq: Encodable {
    let title: String
    let city: String
    let district: String
    let neighborhood: String
    let address_line: String
    let postal_code: String
}

struct AddressCreateData: Decodable {
    let id: Int
}
