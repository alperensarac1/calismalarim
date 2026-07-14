import Foundation

struct Venue: Codable, Identifiable, Hashable {

    let id: Int
    let cityId: Int?
    let districtId: Int?
    let name: String
    let address: String?
    let capacity: Int?

    enum CodingKeys: String, CodingKey {
        case id
        case cityId = "city_id"
        case districtId = "district_id"
        case name
        case address
        case capacity
    }
}
