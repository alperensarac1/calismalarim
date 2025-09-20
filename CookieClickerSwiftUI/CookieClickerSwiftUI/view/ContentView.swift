import SwiftUI

struct ContentView: View {
    @EnvironmentObject var store: GameStore
    @State private var cookieScale: CGFloat = 1.0

    var body: some View {
        NavigationStack {
            ZStack {
                LinearGradient(colors: [.orange.opacity(0.15), .yellow.opacity(0.2)],
                               startPoint: .top, endPoint: .bottom)
                    .ignoresSafeArea()

                GeometryReader { geo in
                    VStack(spacing: 12) {
                        // Üst kart
                        VStack(alignment: .leading, spacing: 4) {
                            Text(format(store.state.score))
                                .font(.system(size: 28, weight: .bold))
                            Text("\(format(store.state.cps * store.totalMultiplier)) / sn  (x\(String(format: "%.2f", store.totalMultiplier)))")
                                .foregroundStyle(.secondary)
                        }
                        .padding()
                        .background(RoundedRectangle(cornerRadius: 16).fill(.ultraThinMaterial))
                        .padding(.horizontal)

                        // Cookie
                        Button {
                            withAnimation(.easeOut(duration: 0.1)) { cookieScale = 0.92 }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                                withAnimation(.spring(response: 0.25, dampingFraction: 0.6)) { cookieScale = 1.0 }
                            }
                            let center = CGPoint(x: geo.size.width/2, y: 230)
                            store.tapCookie(center: center, geo: geo)
                        } label: {
                            Image(systemName: "circle.fill")
                                .resizable().scaledToFit()
                                .frame(width: 220, height: 220)
                                .overlay { Text("🍪").font(.system(size: 80)) }
                        }
                        .scaleEffect(cookieScale)
                        .padding(.vertical, 8)

                        // Upgrade listesi
                        List {
                            Section("Yükseltmeler") {
                                ForEach(store.upgrades) { u in
                                    UpgradeRow(u: u,
                                               price: u.currentPrice() * (1.0 - store.discountPct),
                                               canAfford: store.state.score >= u.currentPrice() * (1.0 - store.discountPct)) {
                                        store.buyUpgrade(u)
                                    }
                                }
                            }
                        }
                        .listStyle(.insetGrouped)

                        // Bottom bar
                        HStack(spacing: 10) {
                            NavigationLink {
                                PrestigeShopView()
                            } label: {
                                Text("Shop")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.borderedProminent)

                            Button(store.critReady ? "Crit" : "\(store.critCooldownLeft)s") {
                                let center = CGPoint(x: geo.size.width/2, y: 230)
                                store.doCrit(from: center, geo: geo)
                            }
                            .disabled(!store.critReady)
                            .buttonStyle(.bordered)

                            Button("Prestige") { store.prestige() }
                                .buttonStyle(.bordered)

                            Button("Reset") { store.reset() }
                                .buttonStyle(.bordered)
                        }
                        .padding(.horizontal)
                        .padding(.bottom, 6)
                    }
                    // Flying texts overlay
                    .overlay {
                        ZStack {
                            ForEach(store.floaters) { f in
                                Text(f.text)
                                    .font(.system(size: f.isCrit ? 26 : 22, weight: .bold))
                                    .foregroundStyle(f.isCrit ? .red : .white)
                                    .shadow(color: .black.opacity(0.5), radius: 4, x: 0, y: 2)
                                    .position(f.pos)
                                    .transition(.opacity)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Cookie Clicker")
        }
    }

    private func format(_ v: Double) -> String {
        if v >= 1_000_000 { return String(format: "%.2fM", v/1_000_000) }
        if v >= 1_000 { return String(format: "%.1fk", v/1_000) }
        return String(format: "%.0f", v)
    }
}

struct UpgradeRow: View {
    let u: Upgrade
    let price: Double
    let canAfford: Bool
    let onBuy: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: u.icon).frame(width: 28).font(.title3)
            VStack(alignment: .leading, spacing: 2) {
                Text(u.level > 0 ? "\(u.title) (Lv \(u.level))" : u.title)
                    .font(.headline)
                Text(u.desc).font(.subheadline).foregroundStyle(.secondary)
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 4) {
                Text(format(price))
                    .monospacedDigit()
                    .foregroundStyle(canAfford ? .primary : .secondary)
                Button("Buy", action: onBuy)
                    .buttonStyle(.bordered)
                    .disabled(!canAfford)
            }
        }
    }
    private func format(_ v: Double) -> String {
        if v >= 1_000_000 { return String(format: "%.2fM", v/1_000_000) }
        if v >= 1_000 { return String(format: "%.1fk", v/1_000) }
        return String(format: "%.0f", v)
    }
}
