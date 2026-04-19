//
//  GameViewController.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 11.04.2026.
//

import Foundation
import UIKit

final class GameViewController: UIViewController {

    @IBOutlet weak var lblTurnInfo: UILabel!
    @IBOutlet weak var lblStatus: UILabel!
    @IBOutlet weak var collectionViewOwnBoard: UICollectionView!
    @IBOutlet weak var collectionViewEnemyBoard: UICollectionView!

    var roomCode: String = ""
    var playerId: String = ""
    var playerName: String = ""
    var firstTurnPlayerId: String = ""
    var ownBoardJson: String = ""

    private let socketManager = SocketManager.shared
    private let decoder = JSONDecoder()

    private let boardSize = 10

    private var ownBoardCells: [BoardCell] = []
    private var enemyBoardCells: [BoardCell] = []

    private var currentTurnPlayerId: String = ""
    private var isFireRequestPending: Bool = false
    private var isRematchRequested: Bool = false
    private var isGameOverDialogShown: Bool = false

    override func viewDidLoad() {
        super.viewDidLoad()

        socketManager.delegate = self

        currentTurnPlayerId = firstTurnPlayerId

        setupCollectionViews()
        buildOwnBoardFromJson()
        buildEnemyBoard()
        updateTurnLabel()

        lblStatus.text = "Oyun başladı"
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        socketManager.delegate = self
    }

    private func setupCollectionViews() {
        collectionViewOwnBoard.delegate = self
        collectionViewOwnBoard.dataSource = self

        collectionViewEnemyBoard.delegate = self
        collectionViewEnemyBoard.dataSource = self
    }

    private func buildOwnBoardFromJson() {
        ownBoardCells.removeAll()

        guard let data = ownBoardJson.data(using: .utf8),
              let matrix = try? JSONSerialization.jsonObject(with: data, options: []) as? [[Int]] else {
            for row in 0..<boardSize {
                for col in 0..<boardSize {
                    ownBoardCells.append(BoardCell(row: row, col: col, state: .empty))
                }
            }
            return
        }

        for row in 0..<boardSize {
            for col in 0..<boardSize {
                let value = row < matrix.count && col < matrix[row].count ? matrix[row][col] : 0
                let state: CellState = value == 1 ? .ship : .empty
                ownBoardCells.append(BoardCell(row: row, col: col, state: state))
            }
        }
    }

    private func buildEnemyBoard() {
        enemyBoardCells.removeAll()

        for row in 0..<boardSize {
            for col in 0..<boardSize {
                enemyBoardCells.append(BoardCell(row: row, col: col, state: .empty))
            }
        }
    }

    private func updateTurnLabel() {
        lblTurnInfo.text = currentTurnPlayerId == playerId ? "Sıra sende" : "Rakibin sırası"
    }

    private func sendFire(row: Int, col: Int) {
        if currentTurnPlayerId != playerId {
            lblStatus.text = "Sıra sende değil"
            return
        }

        if isFireRequestPending {
            lblStatus.text = "Önce önceki atışın sonucunu bekle"
            return
        }

        let index = row * boardSize + col
        let state = enemyBoardCells[index].state

        if state == .hit || state == .miss {
            lblStatus.text = "Bu hücreye zaten ateş ettin"
            return
        }

        let message: [String: Any] = [
            "type": "FIRE",
            "data": [
                "roomCode": roomCode,
                "playerId": playerId,
                "row": row,
                "col": col
            ]
        ]

        isFireRequestPending = true
        socketManager.send(dictionary: message)
        lblStatus.text = "Atış gönderildi..."
    }

    private func handleFireResult(_ result: FireResultData) {
        isFireRequestPending = false

        let index = result.row * boardSize + result.col
        guard index >= 0 && index < boardSize * boardSize else { return }

        let shooterIsMe = result.shooterPlayerId == playerId

        if shooterIsMe {
            enemyBoardCells[index].state = result.hit ? .hit : .miss
            collectionViewEnemyBoard.reloadItems(at: [IndexPath(item: index, section: 0)])
        } else {
            ownBoardCells[index].state = result.hit ? .hit : .miss
            collectionViewOwnBoard.reloadItems(at: [IndexPath(item: index, section: 0)])
        }

        lblStatus.text = result.message

        if result.gameOver {
            let isWinner = result.winnerPlayerId == playerId
            lblTurnInfo.text = isWinner ? "Oyun bitti: Kazandın" : "Oyun bitti: Kaybettin"
            showGameOverDialog(isWinner: isWinner)
            return
        }

        currentTurnPlayerId = result.nextTurnPlayerId ?? ""
        updateTurnLabel()
    }

    private func requestRematch() {
        if isRematchRequested {
            lblStatus.text = "Zaten yeniden oyun isteği gönderdin"
            return
        }

        let message: [String: Any] = [
            "type": "REQUEST_REMATCH",
            "data": [
                "roomCode": roomCode,
                "playerId": playerId
            ]
        ]

        isRematchRequested = true
        socketManager.send(dictionary: message)
        lblStatus.text = "Yeniden oyun isteği gönderildi. Rakip bekleniyor..."
    }

    private func handleSocketMessage(_ text: String) {
        guard let jsonData = text.data(using: .utf8),
              let jsonObject = try? JSONSerialization.jsonObject(with: jsonData, options: []),
              let jsonDict = jsonObject as? [String: Any],
              let type = jsonDict["type"] as? String,
              let dataDict = jsonDict["data"] as? [String: Any],
              let data = try? JSONSerialization.data(withJSONObject: dataDict, options: []) else {
            lblStatus.text = "Mesaj ayrıştırılamadı"
            return
        }

        switch type {
        case "FIRE_RESULT":
            if let decoded = try? decoder.decode(FireResultData.self, from: data) {
                handleFireResult(decoded)
            }

        case "REMATCH_STATUS":
            if let decoded = try? decoder.decode(RematchStatusData.self, from: data) {
                lblStatus.text = "\(decoded.message)\n\(formatRematchPlayers(decoded.players))"
            }

        case "REMATCH_STARTED":
            if let decoded = try? decoder.decode(RematchStartedData.self, from: data) {
                lblStatus.text = decoded.message
                goToPlacementForRematch()
            }

        case "PLAYER_LEFT":
            lblStatus.text = "Rakip oyundan ayrıldı"
            showPlayerLeftDialog()

        case "ERROR":
            if let decoded = try? decoder.decode(ErrorData.self, from: data) {
                isFireRequestPending = false
                lblStatus.text = "Hata: \(decoded.message)"
            }

        default:
            break
        }
    }

    private func formatRematchPlayers(_ players: [RematchPlayerInfo]) -> String {
        guard !players.isEmpty else { return "-" }

        return players.map {
            "\($0.name): \($0.wantsRematch ? "hazır" : "bekleniyor")"
        }.joined(separator: " | ")
    }

    private func showGameOverDialog(isWinner: Bool) {
        if isGameOverDialogShown { return }
        isGameOverDialogShown = true

        let title = isWinner ? "Tebrikler" : "Oyun Bitti"
        let message = isWinner
            ? "Rakibin tüm gemilerini batırdın.\n\nYeniden oynamak ister misin?"
            : "Tüm gemilerin batırıldı.\n\nYeniden oynamak ister misin?"

        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)

        alert.addAction(UIAlertAction(title: "Yeniden Oyna", style: .default, handler: { [weak self] _ in
            self?.requestRematch()
        }))

        alert.addAction(UIAlertAction(title: "Lobiye Dön", style: .cancel, handler: { [weak self] _ in
            self?.goToLobby()
        }))

        present(alert, animated: true)
    }

    private func showPlayerLeftDialog() {
        let alert = UIAlertController(
            title: "Rakip Ayrıldı",
            message: "Rakip oyundan çıktı. Lobiye dönmek ister misin?",
            preferredStyle: .alert
        )

        alert.addAction(UIAlertAction(title: "Lobiye Dön", style: .default, handler: { [weak self] _ in
            self?.goToLobby()
        }))

        present(alert, animated: true)
    }

    private func goToLobby() {
        navigationController?.popToRootViewController(animated: true)
    }

    private func goToPlacementForRematch() {
        guard let vc = storyboard?.instantiateViewController(withIdentifier: "ShipPlacementViewController") as? ShipPlacementViewController else {
            return
        }

        vc.roomCode = roomCode
        vc.playerId = playerId
        vc.playerName = playerName

        navigationController?.pushViewController(vc, animated: true)
    }
}
extension GameViewController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        if collectionView == collectionViewOwnBoard {
            return ownBoardCells.count
        } else {
            return enemyBoardCells.count
        }
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let item: BoardCell

        if collectionView == collectionViewOwnBoard {
            item = ownBoardCells[indexPath.item]
        } else {
            item = enemyBoardCells[indexPath.item]
        }

        guard let cell = collectionView.dequeueReusableCell(
            withReuseIdentifier: "BoardCellCollectionViewCell",
            for: indexPath
        ) as? BoardCellCollectionViewCell else {
            return UICollectionViewCell()
        }

        cell.configure(with: item)
        return cell
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard collectionView == collectionViewEnemyBoard else { return }

        let cell = enemyBoardCells[indexPath.item]
        sendFire(row: cell.row, col: cell.col)
    }

    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize {
        let spacing: CGFloat = 2
        let totalSpacing = spacing * 9
        let width = (collectionView.bounds.width - totalSpacing) / 10
        return CGSize(width: width, height: width)
    }

    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        2
    }

    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        2
    }
}
extension GameViewController: SocketManagerDelegate {
    func socketDidConnect() {
        lblStatus.text = "Bağlantı aktif"
    }

    func socketDidDisconnect() {
        lblStatus.text = "Bağlantı kesildi"
    }

    func socketDidReceiveMessage(_ text: String) {
        handleSocketMessage(text)
    }

    func socketDidReceiveError(_ errorMessage: String) {
        lblStatus.text = "Hata: \(errorMessage)"
        isFireRequestPending = false
    }
}
