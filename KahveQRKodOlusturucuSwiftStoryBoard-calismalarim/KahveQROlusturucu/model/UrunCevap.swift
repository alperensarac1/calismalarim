import Foundation


struct UrunCevap: Codable {
    let kahveUrun: [Urun]
    let success: Int

    enum CodingKeys: String, CodingKey {
        case kahveUrun = "kahve_urun"
        case success
    }
}
