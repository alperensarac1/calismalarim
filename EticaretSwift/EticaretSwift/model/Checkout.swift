//
//  Checkout.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 16.01.2026.
//

import Foundation
struct CheckoutRequest: Encodable {
    let idempotency_key: String?
    let address_name: String?
    let address_line1: String
    let address_line2: String?
    let city: String
    let district: String?
    let postal_code: String?
}

struct CheckoutResponse: Decodable {
    let order_id: Int
    let total: Double
    let currency: String
}
