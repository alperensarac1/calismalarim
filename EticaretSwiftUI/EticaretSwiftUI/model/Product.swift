import Foundation
struct ProductListPage: Decodable {
    let page: Int
    let per: Int
    let total: Int
    let items: [ProductListDto]
}

struct ProductListDto: Decodable {
    let id: Int
    let name: String
    let sku: String
    let image_url: String?
    let category_id: Int
    let stock_qty: Int

    let price: Double
    let discount_percent: Double?
    let sale_price: Double

    enum CodingKeys: String, CodingKey {
        case id, name, sku, image_url, category_id, stock_qty, price, discount_percent, sale_price
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)

        id = try c.decode(Int.self, forKey: .id)
        name = try c.decode(String.self, forKey: .name)
        sku = try c.decode(String.self, forKey: .sku)
        image_url = try? c.decode(String.self, forKey: .image_url)
        category_id = try c.decode(Int.self, forKey: .category_id)
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

struct ProductDto: Decodable {
    let id: Int
    let name: String
    let slug: String
    let sku: String
    let image_url: String?
    let category_id: Int
    let category_name: String?

    let price: Double
    let discount_percent: Double?
    let sale_price: Double

    let stock_qty: Int
    let is_active: Int

    let description: String?

    enum CodingKeys: String, CodingKey {
        case id, name, slug, sku, image_url, category_id, category_name
        case price, discount_percent, sale_price
        case stock_qty, is_active
        case description
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)

        id = try c.decode(Int.self, forKey: .id)
        name = try c.decode(String.self, forKey: .name)
        slug = try c.decode(String.self, forKey: .slug)
        sku = try c.decode(String.self, forKey: .sku)
        image_url = try? c.decode(String.self, forKey: .image_url)
        category_id = try c.decode(Int.self, forKey: .category_id)
        category_name = try? c.decode(String.self, forKey: .category_name)

        stock_qty = try c.decode(Int.self, forKey: .stock_qty)
        is_active = try c.decode(Int.self, forKey: .is_active)

        // price/sale_price string veya double olabilir
        price = Self.decodeDouble(c, key: .price) ?? 0
        sale_price = Self.decodeDouble(c, key: .sale_price) ?? 0
        discount_percent = Self.decodeDouble(c, key: .discount_percent)

        description = try? c.decode(String.self, forKey: .description)
    }

    private static func decodeDouble(_ c: KeyedDecodingContainer<CodingKeys>, key: CodingKeys) -> Double? {
        if let v = try? c.decode(Double.self, forKey: key) { return v }
        if let s = try? c.decode(String.self, forKey: key) {
            return Double(s.replacingOccurrences(of: ",", with: "."))
        }
        return nil
    }
}

