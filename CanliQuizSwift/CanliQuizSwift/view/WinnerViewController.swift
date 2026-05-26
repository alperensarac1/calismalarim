//
//  WinnerViewController.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 23.05.2026.
//

import Foundation
import UIKit

final class WinnerViewController: UIViewController {

    /*
        Quiz bitince açılan sonuç ekranı.

        İlk 3 kazanan ve genel sıralama gösterilir.
    */

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var winnersLabel: UILabel!
    @IBOutlet weak var scoreboardLabel: UILabel!
    @IBOutlet weak var backHomeButton: UIButton!

    var winners: [[String: Any]] = []
    var scoreboard: [[String: Any]] = []

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()
        renderResults()
    }

    private func configureUI() {
        title = "Sonuç"

        view.backgroundColor = UIColor(
            red: 248 / 255,
            green: 250 / 255,
            blue: 252 / 255,
            alpha: 1
        )

        titleLabel.text = "Quiz Bitti"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 32)
        titleLabel.textAlignment = .center
        titleLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        subtitleLabel.text = "Bunlar Kazandı"
        subtitleLabel.font = UIFont.boldSystemFont(ofSize: 24)
        subtitleLabel.textAlignment = .center
        subtitleLabel.textColor = UIColor.systemPurple

        winnersLabel.numberOfLines = 0
        winnersLabel.textAlignment = .center
        winnersLabel.font = UIFont.boldSystemFont(ofSize: 20)
        winnersLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        scoreboardLabel.numberOfLines = 0
        scoreboardLabel.font = UIFont.systemFont(ofSize: 15)
        scoreboardLabel.textColor = UIColor(red: 55/255, green: 65/255, blue: 81/255, alpha: 1)

        backHomeButton.setTitle("Ana Sayfaya Dön", for: .normal)
        backHomeButton.backgroundColor = UIColor.systemPurple
        backHomeButton.tintColor = .white
        backHomeButton.layer.cornerRadius = 12
        backHomeButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 17)
    }

    private func renderResults() {
        winnersLabel.text = buildWinnersText()
        scoreboardLabel.text = buildScoreboardText()
    }

    private func buildWinnersText() -> String {
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

    private func buildScoreboardText() -> String {
        if scoreboard.isEmpty {
            return "Puan tablosu yok."
        }

        var text = "Genel Sıralama:\n\n"

        for (index, item) in scoreboard.enumerated() {
            let username = item["username"] as? String ?? "-"
            let score = item["score"] as? Int ?? 0

            text += "\(index + 1). \(username) - \(score) puan\n"
        }

        return text
    }

    @IBAction func backHomeButtonTapped(_ sender: UIButton) {
        WebSocketManager.shared.disconnect()

        let homeVC = HomeViewController(
            nibName: "HomeViewController",
            bundle: nil
        )

        navigationController?.setViewControllers([homeVC], animated: true)
    }
}
