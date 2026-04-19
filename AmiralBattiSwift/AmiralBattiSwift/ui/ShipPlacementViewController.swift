//
//  ShipPlacementViewController.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 11.04.2026.
//

import Foundation
import UIKit

final class ShipPlacementViewController: UIViewController {

    @IBOutlet weak var lblPlacementInfo: UILabel!
    @IBOutlet weak var lblCurrentShip: UILabel!
    @IBOutlet weak var lblOrientation: UILabel!
    @IBOutlet weak var btnRotate: UIButton!
    @IBOutlet weak var btnResetBoard: UIButton!
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var lblStatus: UILabel!
    @IBOutlet weak var btnReady: UIButton!

    var roomCode: String = ""
    var playerId: String = ""
    var playerName: String = ""

    private let socketManager = SocketManager.shared
    private let decoder = JSONDecoder()

    private let boardSize = 10
    private var boardCells: [BoardCell] = []

    private var shipsToPlace: [Ship] = [
        Ship(size: 4, placed: false),
        Ship(size: 3, placed: false),
        Ship(size: 3, placed: false),
        Ship(size: 2, placed: false),
        Ship(size: 2, placed: false),
        Ship(size: 1, placed: false),
        Ship(size: 1, placed: false)
    ]

    private var currentShipIndex: Int = 0
    private var currentOrientation: ShipOrientation = .horizontal

    override func viewDidLoad() {
        super.viewDidLoad()

        print("[Placement] viewDidLoad")
        print("[Placement] roomCode = \(roomCode)")
        print("[Placement] playerId = \(playerId)")
        print("[Placement] playerName = \(playerName)")

        socketManager.delegate = self
        print("[Placement] delegate atandı -> viewDidLoad")

        setupCollectionView()
        createBoard()
        updateUI()

        lblPlacementInfo.text = "Oda: \(roomCode) | Oyuncu: \(playerName)"
        lblStatus.text = "Durum: Gemileri yerleştir"
        btnReady.isEnabled = false
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        socketManager.delegate = self
        print("[Placement] delegate atandı -> viewWillAppear")
    }

    @IBAction func btnRotateTapped(_ sender: UIButton) {
        currentOrientation = currentOrientation == .horizontal ? .vertical : .horizontal
        updateUI()
    }

    @IBAction func btnResetBoardTapped(_ sender: UIButton) {
        resetBoard()
    }

    @IBAction func btnReadyTapped(_ sender: UIButton) {
        sendBoardToServer()
    }

    private func setupCollectionView() {
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .clear

        if let layout = collectionView.collectionViewLayout as? UICollectionViewFlowLayout {
            layout.minimumLineSpacing = 2
            layout.minimumInteritemSpacing = 2
        }
    }
    private func createBoard() {
        boardCells.removeAll()

        for row in 0..<boardSize {
            for col in 0..<boardSize {
                boardCells.append(BoardCell(row: row, col: col, state: .empty))
            }
        }

        collectionView.reloadData()
    }

    private func resetBoard() {
        currentShipIndex = 0
        currentOrientation = .horizontal

        for index in shipsToPlace.indices {
            shipsToPlace[index].placed = false
        }

        createBoard()
        updateUI()

        lblStatus.text = "Durum: Gemileri yerleştir"
        btnReady.isEnabled = false
    }

    private func updateUI() {
        if currentShipIndex < shipsToPlace.count {
            lblCurrentShip.text = "Seçili gemi: \(shipsToPlace[currentShipIndex].size) hücrelik gemi"
        } else {
            lblCurrentShip.text = "Seçili gemi: Tüm gemiler yerleştirildi"
        }

        lblOrientation.text = currentOrientation == .horizontal ? "Yön: Yatay" : "Yön: Dikey"
    }

    private func placeCurrentShip(startRow: Int, startCol: Int) {
        if currentShipIndex >= shipsToPlace.count {
            showAlert(message: "Tüm gemiler zaten yerleştirildi")
            return
        }

        let ship = shipsToPlace[currentShipIndex]

        if !canPlaceShip(startRow: startRow, startCol: startCol, shipSize: ship.size, orientation: currentOrientation) {
            lblStatus.text = "Durum: Gemi burada konumlanamaz"
            return
        }

        for i in 0..<ship.size {
            let row = currentOrientation == .vertical ? startRow + i : startRow
            let col = currentOrientation == .horizontal ? startCol + i : startCol
            let index = row * boardSize + col
            boardCells[index].state = .ship
        }

        shipsToPlace[currentShipIndex].placed = true
        currentShipIndex += 1

        collectionView.reloadData()
        updateUI()

        if currentShipIndex >= shipsToPlace.count {
            lblStatus.text = "Durum: Tüm gemiler yerleştirildi. Hazırım butonuna bas."
            btnReady.isEnabled = true
        } else {
            lblStatus.text = "Durum: Gemileri yerleştir"
        }
    }

    private func canPlaceShip(startRow: Int, startCol: Int, shipSize: Int, orientation: ShipOrientation) -> Bool {
        var targetCells: [(Int, Int)] = []

        for i in 0..<shipSize {
            let row = orientation == .vertical ? startRow + i : startRow
            let col = orientation == .horizontal ? startCol + i : startCol

            if row >= boardSize || col >= boardSize {
                return false
            }

            targetCells.append((row, col))
        }

        for (row, col) in targetCells {
            for r in (row - 1)...(row + 1) {
                for c in (col - 1)...(col + 1) {
                    if r < 0 || r >= boardSize || c < 0 || c >= boardSize {
                        continue
                    }

                    let neighborIndex = r * boardSize + c
                    if boardCells[neighborIndex].state == .ship {
                        return false
                    }
                }
            }
        }

        return true
    }

    private func buildBoardMatrix() -> [[Int]] {
        var matrix = Array(
            repeating: Array(repeating: 0, count: boardSize),
            count: boardSize
        )

        for cell in boardCells {
            matrix[cell.row][cell.col] = cell.state == .ship ? 1 : 0
        }

        return matrix
    }

    private func sendBoardToServer() {
        let matrix = buildBoardMatrix()

        let message: [String: Any] = [
            "type": "SET_BOARD",
            "data": [
                "roomCode": roomCode,
                "playerId": playerId,
                "board": matrix
            ]
        ]

        print("[Placement] SET_BOARD gönderilecek")
        print("[Placement] roomCode = \(roomCode)")
        print("[Placement] playerId = \(playerId)")
        print("[Placement] matrix = \(matrix)")

        lblStatus.text = "Durum: Tahta gönderiliyor..."
        btnReady.isEnabled = false

        socketManager.send(dictionary: message)
    }

    private func handleSocketMessage(_ text: String) {
        print("[Placement] Gelen ham mesaj = \(text)")

        guard let jsonData = text.data(using: .utf8),
              let jsonObject = try? JSONSerialization.jsonObject(with: jsonData, options: []),
              let jsonDict = jsonObject as? [String: Any],
              let type = jsonDict["type"] as? String,
              let dataDict = jsonDict["data"] as? [String: Any],
              let data = try? JSONSerialization.data(withJSONObject: dataDict, options: []) else {
            lblStatus.text = "Mesaj ayrıştırılamadı"
            print("[Placement] Mesaj parse edilemedi")
            return
        }

        print("[Placement] Mesaj tipi = \(type)")

        switch type {
        case "BOARD_SET":
            if let decoded = try? decoder.decode(BoardSetData.self, from: data) {
                print("[Placement] BOARD_SET geldi -> \(decoded.message)")
                lblStatus.text = decoded.message
            }

        case "GAME_STARTED":
            if let decoded = try? decoder.decode(GameStartedData.self, from: data) {
                print("[Placement] GAME_STARTED geldi -> firstTurnPlayerId = \(decoded.firstTurnPlayerId)")
                goToGameScreen(firstTurnPlayerId: decoded.firstTurnPlayerId)
            }

        case "ERROR":
            if let decoded = try? decoder.decode(ErrorData.self, from: data) {
                print("[Placement] ERROR -> \(decoded.message)")
                lblStatus.text = "Hata: \(decoded.message)"
                btnReady.isEnabled = true
            }

        default:
            print("[Placement] Şu an için işlenmeyen mesaj tipi = \(type)")
        }
    }

    private func goToGameScreen(firstTurnPlayerId: String) {
        guard let vc = storyboard?.instantiateViewController(withIdentifier: "GameViewController") as? GameViewController else {
            return
        }

        vc.roomCode = roomCode
        vc.playerId = playerId
        vc.playerName = playerName
        vc.firstTurnPlayerId = firstTurnPlayerId

        if let data = try? JSONSerialization.data(withJSONObject: buildBoardMatrix(), options: []),
           let jsonString = String(data: data, encoding: .utf8) {
            vc.ownBoardJson = jsonString
        }

        navigationController?.pushViewController(vc, animated: true)
    }

    private func showAlert(message: String) {
        let alert = UIAlertController(title: "Bilgi", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(alert, animated: true)
    }
}
extension ShipPlacementViewController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        boardCells.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let item = boardCells[indexPath.item]

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
        let cell = boardCells[indexPath.item]
        placeCurrentShip(startRow: cell.row, startCol: cell.col)
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
extension ShipPlacementViewController: SocketManagerDelegate {
    func socketDidConnect() {
        lblStatus.text = "Durum: Bağlantı hazır"
    }

    func socketDidDisconnect() {
        lblStatus.text = "Durum: Bağlantı kesildi"
    }

    func socketDidReceiveMessage(_ text: String) {
        handleSocketMessage(text)
    }

    func socketDidReceiveError(_ errorMessage: String) {
        lblStatus.text = "Hata: \(errorMessage)"
    }
}
