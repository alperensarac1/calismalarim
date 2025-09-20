//
//  PrestigePerk.swift
//  CookieClickerSwiftUI
//
//  Created by Alperen Saraç on 16.09.2025.
//

import Foundation
struct PrestigePerk: Identifiable, Codable {
    var id: String { key }
    let key: String              // "gprod","crit","discount","tapTop"
    let title: String
    let desc: String
    let baseCost: Int
    let costScaling: Double
    var level: Int
    let maxLevel: Int

    func costForNext() -> Int {
        max(baseCost, Int(Double(baseCost) * pow(costScaling, Double(level))))
    }
}
