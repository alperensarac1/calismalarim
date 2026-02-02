//
//  AuthModel.swift
//  KargoPaylasimSwift
//
//  Created by Alperen Saraç on 28.01.2026.
//

import Foundation


struct LoginReq: Codable { let phone: String; let password: String }
struct LoginResp: Codable { let token: String }

struct RegisterReq: Codable {
    let phone: String
    let first_name: String
    let last_name: String
    let tc_no: String
    let password: String

    let address_title: String
    let city: String
    let district: String
    let neighborhood: String?
    let address_line: String
    let postal_code: String?
}
struct RegisterData: Codable {
    let user_id: Int
    let address_id: Int
}

struct RegisterResp: Codable { let userId: Int? }

struct UserMeResp: Codable { let id: Int; let name: String; let phone: String }
