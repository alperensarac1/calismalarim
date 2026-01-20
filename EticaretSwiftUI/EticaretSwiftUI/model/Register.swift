import Foundation
struct RegisterRequest: Encodable {
    let name: String
    let email: String
    let password: String
}

struct RegisterResponse: Decodable {
    let token: String
    let user_id: Int
}
