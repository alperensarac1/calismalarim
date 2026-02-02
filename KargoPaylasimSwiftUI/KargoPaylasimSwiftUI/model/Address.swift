//
//  Address.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
struct Address: Decodable, Identifiable {
    let id: Int
    let title: String
    let city: String
    let district: String
    let address_line: String
    let is_default: Int
}

struct AddressListData: Decodable {
    let items: [Address]
}

struct AddressCreateReq: Encodable {
    let title: String
    let city: String
    let district: String
    let neighborhood: String
    let address_line: String
    let postal_code: String
}

struct AddressCreateData: Decodable { let id: Int }
struct IdBody: Encodable { let id: Int }
