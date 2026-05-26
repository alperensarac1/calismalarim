//
//  Helpers.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import Foundation

func buildPlayersText(_ players: [String]) -> String {
    if players.isEmpty {
        return "Oyuncular bekleniyor..."
    }

    var text = "Oyuncular:\n\n"

    for (index, player) in players.enumerated() {
        text += "\(index + 1). \(player)\n"
    }

    return text
}

func buildScoreboardText(_ scoreboard: [[String: Any]]) -> String {
    if scoreboard.isEmpty {
        return "Puan tablosu bekleniyor..."
    }

    var text = "Puan Tablosu:\n\n"

    for (index, item) in scoreboard.enumerated() {
        let username = item["username"] as? String ?? "-"
        let score = item["score"] as? Int ?? 0

        text += "\(index + 1). \(username) - \(score) puan\n"
    }

    return text
}

func buildWinnersText(_ winners: [[String: Any]]) -> String {
    if winners.isEmpty {
        return "Kazanan bulunamadı."
    }

    var text = ""

    for (index, item) in winners.enumerated() {
        let username = item["username"] as? String ?? "-"
        let score = item["score"] as? Int ?? 0

        let medal: String

        if index == 0 {
            medal = "🥇"
        } else if index == 1 {
            medal = "🥈"
        } else if index == 2 {
            medal = "🥉"
        } else {
            medal = ""
        }

        text += "\(medal) \(username)\n\(score) puan\n\n"
    }

    return text.trimmingCharacters(in: .whitespacesAndNewlines)
}

func indexToLetter(_ index: Int) -> String {
    if index >= 0 && index < 26 {
        let scalar = UnicodeScalar(65 + index)!
        return String(Character(scalar))
    }

    return "\(index + 1)"
}
