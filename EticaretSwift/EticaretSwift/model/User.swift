import Foundation
struct UserDto: Decodable {
    let id: Int
    let name: String
    let email: String
    let created_at: String
    let updated_at: String?
}
