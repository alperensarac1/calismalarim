import Foundation

/*
    User

    Kullanıcı modelidir.

    Backend JSON alanları:
    id
    full_name
    email
    phone
    role
    api_token
    created_at
*/
struct User: Codable {

    let id: Int
    let fullName: String
    let email: String
    let phone: String?
    let role: String
    let apiToken: String?
    let createdAt: String?

    /*
        Swift tarafında camelCase kullanıyoruz.
        Backend snake_case döndürüyor.

        Bu yüzden CodingKeys ile eşleştirme yapıyoruz.
    */
    enum CodingKeys: String, CodingKey {
        case id
        case fullName = "full_name"
        case email
        case phone
        case role
        case apiToken = "api_token"
        case createdAt = "created_at"
    }
}
