import Foundation


struct Comment: Codable {
    let id: Int
    let entry_id: Int
    let user_id: Int
    let username: String
    let comment_text: String
    let likes: Int
    let dislikes: Int
    let created_at: String
}
