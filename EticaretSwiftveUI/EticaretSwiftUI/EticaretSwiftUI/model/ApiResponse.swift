

import Foundation
struct ApiResponse<T: Decodable>: Decodable {
    let ok: Bool
    let data: T?
    let error: String?
}
