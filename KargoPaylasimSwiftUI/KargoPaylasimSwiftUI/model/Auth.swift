//
//  Auth.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation


struct LoginReq: Encodable {
    let phone: String
    let password: String
}
struct LoginData: Decodable {
    let token: String
    let user_id: Int
}

struct RegisterReq: Encodable {
    let phone: String
    let first_name: String
    let last_name: String
    let tc_no: String
    let password: String

    let address_title: String
    let city: String
    let district: String
    let neighborhood: String
    let address_line: String
    let postal_code: String
}

struct RegisterData: Decodable {
    let user_id: Int
    let address_id: Int
}
