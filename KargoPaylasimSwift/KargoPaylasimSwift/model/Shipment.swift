//
//  Shipment.swift
//  KargoPaylasimSwift
//
//  Created by Alperen Saraç on 29.01.2026.
//

import Foundation


struct ShipmentListData: Decodable {
    let items: [Shipment]
}

struct Shipment: Decodable {
    let id: Int
    let sender_user_id: Int
    let receiver_user_id: Int
    let sender_address_id: Int
    let receiver_address_id: Int
    let pickup_code: String
    let status: String
    let code_expires_at: String
    let created_at: String
    let updated_at: String

    let cargo_company_id: Int?
    let cargo_company_name: String?

    let role: String              // "SENDER" | "RECEIVER"
    let visible: Bool
    let sender_initials: String?
    let receiver_address_title: String?
}
struct CreateShipmentReq: Encodable {
    let receiver_phone: String
    let sender_address_id: Int? // nil => backend default
}

struct CreateShipmentData: Decodable {
    let shipment_id: Int
    let pickup_code: String
    let status: String
    let code_expires_at: String
}
