import Foundation

struct OdaModel: Decodable, Identifiable, Hashable {
    // SwiftUI kimliği
    var id: Int { odaId }

    let odaId: Int
    let roomCode: String
    let createdBy: Int

    enum CodingKeys: String, CodingKey {
        case odaId     = "room_id"
        case roomCode  = "room_code"
        case createdBy = "created_by"
    }

    // Manuel init (UI tarafında elle oluştururken)
    init(odaId: Int, roomCode: String, createdBy: Int) {
        self.odaId = odaId
        self.roomCode = roomCode
        self.createdBy = createdBy
    }

    // Decoder init (sunucudan decode)
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        self.odaId    = try c.decode(Int.self, forKey: .odaId)
        self.roomCode = try c.decode(String.self, forKey: .roomCode)

        // created_by hem Int hem String gelebiliyor
        if let i = try? c.decode(Int.self, forKey: .createdBy) {
            self.createdBy = i
        } else if let s = try? c.decode(String.self, forKey: .createdBy),
                  let i = Int(s) {
            self.createdBy = i
        } else {
            self.createdBy = 0
        }
    }
}

