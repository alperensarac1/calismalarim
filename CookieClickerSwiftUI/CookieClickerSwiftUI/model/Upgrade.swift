//
//  Upgrade.swift
//  CookieClickerSwiftUI
//
//  Created by Alperen Saraç on 16.09.2025.
//

import Foundation
struct Upgrade: Identifiable, Codable, Equatable {
    let id: Int
    let title: String
    let desc: String
    let icon: String      // SF Symbol adı
    let basePrice: Double
    let cpsGain: Double
    let tapGain: Int
    var level: Int
    let priceMultiplier: Double

    func currentPrice() -> Double {
        basePrice * pow(priceMultiplier, Double(level))
    }
}
