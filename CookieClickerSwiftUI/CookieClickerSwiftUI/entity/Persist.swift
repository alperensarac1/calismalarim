import Foundation
enum Persist {
    private static let gameKey = "cc_game_v1"
    private static let perkKey = "cc_perks_v1"

    static func loadGame() -> GameState {
        if let d = UserDefaults.standard.data(forKey: gameKey),
           let s = try? JSONDecoder().decode(GameState.self, from: d) { return s }
        return GameState()
    }
    static func saveGame(_ s: GameState) {
        if let d = try? JSONEncoder().encode(s) { UserDefaults.standard.set(d, forKey: gameKey) }
    }

    static func loadPerks() -> PerkStore {
        if let d = UserDefaults.standard.data(forKey: perkKey),
           let s = try? JSONDecoder().decode(PerkStore.self, from: d) { return s }
        return PerkStore()
    }
    static func savePerks(_ s: PerkStore) {
        if let d = try? JSONEncoder().encode(s) { UserDefaults.standard.set(d, forKey: perkKey) }
    }
}
