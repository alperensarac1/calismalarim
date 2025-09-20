import Foundation
import SwiftUI

struct PrestigeShopView: View {
    @EnvironmentObject var store: GameStore
    @State private var items: [PrestigePerk] = []

    var body: some View {
        List {
            Section(header: Text("Prestige Puanı: \(store.perks.points)")) {
                ForEach(items) { p in
                    PerkRow(perk: p,
                            canAfford: store.perks.points >= p.costForNext(),
                            onBuy: { buy(p) })
                }
            }
        }
        .navigationTitle("Prestige Mağazası")
        .onAppear { loadItems() }
    }

    private func loadItems() {
        items = [
            .init(key: "gprod", title: "Altın Çırpıcı",
                  desc: "%5 üretim çarpanı / seviye (CPS & tap)",
                  baseCost: 1, costScaling: 1.6, level: store.perks.gprod, maxLevel: .max),
            .init(key: "crit", title: "Uğurlu Tılsım",
                  desc: "%1 pasif crit şansı / seviye (tap x3)",
                  baseCost: 2, costScaling: 1.7, level: store.perks.crit, maxLevel: .max),
            .init(key: "discount", title: "Toplu Alım",
                  desc: "Upgrade fiyatlarında %2 indirim / seviye (maks %50)",
                  baseCost: 3, costScaling: 1.8, level: store.perks.discount, maxLevel: 25),
            .init(key: "tapTop", title: "Turbo Tap",
                  desc: "Kalıcı +1 tap gücü / seviye",
                  baseCost: 2, costScaling: 1.5, level: store.perks.tapTop, maxLevel: .max)
        ]
    }

    private func buy(_ p: PrestigePerk) {
        guard let idx = items.firstIndex(where: { $0.id == p.id }) else { return }
        let cost = p.costForNext()
        guard store.perks.points >= cost, p.level < p.maxLevel else { return }
        store.perks.points -= cost
        var new = p
        new.level += 1
        items[idx] = new

        switch p.key {
        case "gprod": store.perks.gprod = new.level
        case "crit": store.perks.crit = new.level
        case "discount": store.perks.discount = new.level
        case "tapTop": store.perks.tapTop = new.level
        default: break
        }
        let _ = { Persist.savePerks(store.perks) }()
    }
}

struct PerkRow: View {
    let perk: PrestigePerk
    let canAfford: Bool
    let onBuy: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(perk.title).font(.headline)
                Text(perk.desc).font(.subheadline).foregroundStyle(.secondary)
                Text("Lv \(perk.level) • Maliyet: \(perk.costForNext())")
                    .font(.caption).foregroundStyle(.secondary)
            }
            Spacer()
            Button("Satın Al", action: onBuy)
                .buttonStyle(.borderedProminent)
                .disabled(!canAfford || perk.level >= perk.maxLevel)
        }
    }
}
