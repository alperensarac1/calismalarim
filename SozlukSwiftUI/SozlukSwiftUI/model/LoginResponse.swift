//
//  LoginResponse.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
struct LoginResponse: Codable {
    let success: Bool
    let user_id: Int?
    let message: String?
}
