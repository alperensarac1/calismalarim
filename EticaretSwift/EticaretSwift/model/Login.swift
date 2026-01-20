
import Foundation
struct LoginRequest: Encodable {
    let email: String
    let password: String
}
struct LoginResponse: Decodable {
    let token: String
    let user_id: Int
}
