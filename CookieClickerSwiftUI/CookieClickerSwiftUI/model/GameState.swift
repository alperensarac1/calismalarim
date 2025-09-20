import Foundation
struct GameState: Codable {
    var score: Double = 0
    var cps: Double = 0
    var baseTap: Int = 1
    var extraTap: Int = 0
    var prestigeLevel: Int = 0
}
