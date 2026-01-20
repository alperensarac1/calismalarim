//
//  Cart.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 16.01.2026.
//

import Foundation
struct CartDto: Decodable {
    let cart_id: Int?
    let items: [CartItemDto]
    let total: Double
    let total_items: Int
}

struct CartItemDto: Decodable {
    let item_id: Int
    let quantity: Int
    let product_id: Int
    let name: String
    let sku: String?
    let image_url: String?
    let stock_qty: Int

    let price: Double
    let discount_percent: Double?
    let sale_price: Double

    enum CodingKeys: String, CodingKey {
        case item_id, quantity, product_id, name, sku, image_url, stock_qty
        case price, discount_percent, sale_price
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)

        item_id = try c.decode(Int.self, forKey: .item_id)
        quantity = try c.decode(Int.self, forKey: .quantity)
        product_id = try c.decode(Int.self, forKey: .product_id)
        name = try c.decode(String.self, forKey: .name)
        sku = try? c.decode(String.self, forKey: .sku)
        image_url = try? c.decode(String.self, forKey: .image_url)
        stock_qty = try c.decode(Int.self, forKey: .stock_qty)

        price = Self.decodeDouble(c, key: .price) ?? 0
        sale_price = Self.decodeDouble(c, key: .sale_price) ?? 0
        discount_percent = Self.decodeDouble(c, key: .discount_percent)
    }

    private static func decodeDouble(_ c: KeyedDecodingContainer<CodingKeys>, key: CodingKeys) -> Double? {
        if let v = try? c.decode(Double.self, forKey: key) { return v }
        if let s = try? c.decode(String.self, forKey: key) {
            return Double(s.replacingOccurrences(of: ",", with: "."))
        }
        return nil
    }
}


struct AddToCartRequest: Encodable {
    let product_id: Int
    let quantity: Int
}

struct AddToCartResponse: Decodable {
    let cart_id: Int
    let item_id: Int
    let quantity: Int
}

struct UpdateCartItemRequest: Encodable {
    let quantity: Int
}
