import Foundation
import SwiftUI
import Combine

final class GameStore: ObservableObject {
    @Published var state: GameState = Persist.loadGame()
    @Published var perks: PerkStore  = Persist.loadPerks()

    @Published var upgrades: [Upgrade] = [
        .init(id: 1, title: "Otomatik Tıklayıcı", desc: "Saniyede +1", icon: "bolt.fill", basePrice: 50,   cpsGain: 1,   tapGain: 0, level: 0, priceMultiplier: 1.15),
        .init(id: 2, title: "Hızlı Karıştırıcı",   desc: "Tıklama +1",  icon: "goforward", basePrice: 75,   cpsGain: 0,   tapGain: 1, level: 0, priceMultiplier: 1.15),
        .init(id: 3, title: "Minik Fırın",         desc: "Saniyede +5", icon: "flame.fill", basePrice: 250, cpsGain: 5,   tapGain: 0, level: 0, priceMultiplier: 1.15),
        .init(id: 4, title: "Çikolata Parçaları",  desc: "Tıklama +3",  icon: "square.grid.2x2.fill", basePrice: 300, cpsGain: 0, tapGain: 3, level: 0, priceMultiplier: 1.15),
        .init(id: 5, title: "Pastane",             desc: "Saniyede +25",icon: "building.2.fill", basePrice: 1200, cpsGain: 25, tapGain: 0, level: 0, priceMultiplier: 1.15),
        .init(id: 6, title: "Fabrika",             desc: "Saniyede +120",icon: "gearshape.2.fill", basePrice: 6000, cpsGain: 120, tapGain: 0, level: 0, priceMultiplier: 1.15),
        .init(id: 7, title: "Araştırma Lab.",      desc: "Tıklama +10", icon: "testtube.2", basePrice: 8000, cpsGain: 0, tapGain: 10, level: 0, priceMultiplier: 1.15),
        .init(id: 8, title: "Roket Fırın",         desc: "Saniyede +750",icon: "rocket.fill", basePrice: 42000, cpsGain: 750, tapGain: 0, level: 0, priceMultiplier: 1.15)
    ]

   
    private var timer: AnyCancellable?
    private let tickInterval: TimeInterval = 0.1

  
    @Published var critReady = true
    @Published var critCooldownLeft = 0
    private var critTimer: AnyCancellable?

 
    struct Floating: Identifiable { let id = UUID(); let text: String; var pos: CGPoint; let isCrit: Bool }
    @Published var floaters: [Floating] = []

    init() {
        startTimer()
    }


    var totalMultiplier: Double {
        let prestige = 1.0 + Double(state.prestigeLevel) * 0.10
        let gprod = 1.0 + Double(perks.gprod) * 0.05
        return prestige * gprod
    }
    var discountPct: Double { min(Double(perks.discount) * 0.02, 0.50) }
    var passiveCritChance: Int { perks.crit }
    var tapPower: Int { state.baseTap + state.extraTap + perks.tapTop }

    func startTimer() {
        timer?.cancel()
        timer = Timer.publish(every: tickInterval, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                guard let self = self else { return }
                let eff = self.state.cps * self.totalMultiplier
                self.state.score += eff * self.tickInterval
                self.persist()
            }
    }

    func tapCookie(center: CGPoint, geo: GeometryProxy) {
        var gain = Int(Double(tapPower) * totalMultiplier)
        if passiveCritChance > 0 && Int.random(in: 0..<100) < passiveCritChance {
            gain *= 3
            spawnFloating(text: "CRIT +\(gain)", at: center, in: geo, isCrit: true)
        } else {
            spawnFloating(text: "+\(gain)", at: center, in: geo, isCrit: false)
        }
        state.score += Double(gain)
        persist()
    }

    func buyUpgrade(_ u: Upgrade) {
        guard let idx = upgrades.firstIndex(of: u) else { return }
        let price = u.currentPrice() * (1.0 - discountPct)
        guard state.score >= price else { return }
        state.score -= price
        upgrades[idx].level += 1
        state.cps += u.cpsGain
        state.extraTap += u.tapGain
        persist()
    }

    func doCrit(from center: CGPoint, geo: GeometryProxy) {
        guard critReady else { return }
        critReady = false
        critCooldownLeft = 30
        let gain = tapPower * 10
        state.score += Double(gain)
        spawnFloating(text: "CRIT +\(gain)", at: center, in: geo, isCrit: true)
        persist()
        critTimer?.cancel()
        critTimer = Timer.publish(every: 1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                guard let self = self else { return }
                self.critCooldownLeft -= 1
                if self.critCooldownLeft <= 0 {
                    self.critReady = true
                    self.critTimer?.cancel()
                }
            }
    }

    func reset() {
        state.score = 0; state.cps = 0; state.extraTap = 0
        upgrades = upgrades.map { var m = $0; m.level = 0; return m }
        persist()
    }

    func prestige() {
        let gain = Int(sqrt(state.score / 1000.0))
        guard gain > 0 else { return }
        perks.points += gain
        state.prestigeLevel += gain
        state.score = 0; state.cps = 0; state.extraTap = 0
        upgrades = upgrades.map { var m = $0; m.level = 0; return m }
        persist()
    }

    private func spawnFloating(text: String, at center: CGPoint, in geo: GeometryProxy, isCrit: Bool) {
        var p = center
        // global koordinat yerine view içinde pozisyon
        p.x = max(20, min(geo.size.width - 20, p.x))
        p.y = max(20, min(geo.size.height - 20, p.y))
        let f = Floating(text: text, pos: p, isCrit: isCrit)
        floaters.append(f)
        withAnimation(.easeOut(duration: 0.7)) {
            if let idx = floaters.firstIndex(where: { $0.id == f.id }) {
                floaters[idx].pos.y -= 120
            }
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.7) { [weak self] in
            self?.floaters.removeAll { $0.id == f.id }
        }
    }

    private func persist() {
        Persist.saveGame(state)
        Persist.savePerks(perks)
    }
}
