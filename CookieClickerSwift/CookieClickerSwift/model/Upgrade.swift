//
//  Upgrade.swift
//  CookieClickerSwift
//
//  Created by Alperen Saraç on 14.09.2025.
//

import Foundation
import UIKit

struct Upgrade: Codable, Equatable {
    static func == (lhs: Upgrade, rhs: Upgrade) -> Bool { lhs.id == rhs.id }
    let id: Int
    let title: String
    let desc: String
    let iconName: String // SF Symbol veya asset adı
    let basePrice: Double
    let cpsGain: Double
    let tapGain: Int
    var level: Int
    let priceMultiplier: Double

    func currentPrice() -> Double {
        basePrice * pow(priceMultiplier, Double(level))
    }
}
