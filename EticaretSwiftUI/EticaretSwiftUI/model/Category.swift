import Foundation
struct CategoryDto: Decodable {
    let id: Int
    let name: String
    let slug: String

    enum CodingKeys: String, CodingKey { case id, name, slug }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)

        if let v = try? c.decode(Int.self, forKey: .id) {
            id = v
        } else {
            let s = try c.decode(String.self, forKey: .id)
            id = Int(s) ?? 0
        }

        name = try c.decode(String.self, forKey: .name)
        slug = try c.decode(String.self, forKey: .slug)
    }
}

