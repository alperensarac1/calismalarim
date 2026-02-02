//
//  Shipment.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
struct Shipment: Decodable, Identifiable {
    let id: Int
    let pickup_code: String
    let status: String
    let code_expires_at: String

    let cargo_company_id: Int?
    let cargo_company_name: String?

    let role: String
    let visible: Bool?
    let sender_initials: String?
    let receiver_address_title: String?
}

struct ShipmentListData: Decodable {
    let items: [Shipment]
}
struct CreateShipmentReq: Encodable {
    let receiver_phone: String
    let sender_address_id: Int?
}
struct CreateShipmentData: Decodable {
    let shipment_id: Int
    let pickup_code: String
    let status: String
    let code_expires_at: String
}
