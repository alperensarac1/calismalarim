//
//  LobbyViewController.swift
//  AmiralBattiSwift
//
//  Created by Alperen Saraç on 11.04.2026.
//

import Foundation
import UIKit

final class LobbyViewController: UIViewController {

    @IBOutlet weak var txtPlayerName: UITextField!
    @IBOutlet weak var txtRoomCode: UITextField!
    @IBOutlet weak var lblRoomInfo: UILabel!
    @IBOutlet weak var lblPlayers: UILabel!
    @IBOutlet weak var lblStatus: UILabel!

    private let socketManager = SocketManager.shared
     private let decoder = JSONDecoder()

     private var currentRoomCode: String = ""
     private var currentPlayerId: String = ""

     // Aynı ekrana iki kez gitmeyi engelle
     private var didNavigateToPlacement: Bool = false
    override func viewDidLoad() {
        super.viewDidLoad()
        socketManager.delegate = self

             lblRoomInfo.text = "Oda: -"
             lblPlayers.text = "Oyuncular: -"
             lblStatus.text = "Durum: Hazır"

             print("[Lobby] viewDidLoad")
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        socketManager.delegate = self
        print("[Lobby] viewWillAppear -> delegate tekrar atandı")
    }


    @IBAction func btnConnectTapped(_ sender: UIButton) {
        print("[Lobby] Sunucuya bağlan butonu tıklandı")
              lblStatus.text = "Durum: Sunucuya bağlanılıyor..."
              socketManager.connect()
    }

    @IBAction func btnCreateRoomTapped(_ sender: UIButton) {
        let playerName = txtPlayerName.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

               print("[Lobby] Oda oluştur tıklandı, playerName=\(playerName)")

               guard !playerName.isEmpty else {
                   showAlert(message: "Oyuncu adı gir")
                   return
               }

               let message: [String: Any] = [
                   "type": "CREATE_ROOM",
                   "data": [
                       "playerName": playerName
                   ]
               ]

               socketManager.send(dictionary: message)
    }

    @IBAction func btnJoinRoomTapped(_ sender: UIButton) {
        let playerName = txtPlayerName.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
               let roomCode = txtRoomCode.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

               print("[Lobby] Odaya katıl tıklandı, playerName=\(playerName), roomCode=\(roomCode)")

               guard !playerName.isEmpty, !roomCode.isEmpty else {
                   showAlert(message: "Oyuncu adı ve oda kodu gir")
                   return
               }

               let message: [String: Any] = [
                   "type": "JOIN_ROOM",
                   "data": [
                       "playerName": playerName,
                       "roomCode": roomCode
                   ]
               ]

               socketManager.send(dictionary: message)
    }

    private func handleSocketMessage(_ text: String) {
            print("[Lobby] Gelen ham mesaj: \(text)")

            guard let jsonData = text.data(using: .utf8),
                  let jsonObject = try? JSONSerialization.jsonObject(with: jsonData, options: []),
                  let jsonDict = jsonObject as? [String: Any],
                  let type = jsonDict["type"] as? String,
                  let dataDict = jsonDict["data"] as? [String: Any],
                  let data = try? JSONSerialization.data(withJSONObject: dataDict, options: []) else {
                lblStatus.text = "Mesaj ayrıştırılamadı"
                print("[Lobby] JSON ayrıştırma başarısız")
                return
            }

            print("[Lobby] Mesaj tipi: \(type)")

            switch type {

            case "ROOM_CREATED":
                if let decoded = try? decoder.decode(RoomCreatedData.self, from: data) {
                    print("[Lobby] ROOM_CREATED -> roomCode=\(decoded.roomCode), playerId=\(decoded.playerId)")

                    currentRoomCode = decoded.roomCode
                    currentPlayerId = decoded.playerId

                    txtRoomCode.text = decoded.roomCode
                    lblRoomInfo.text = "Oda: \(decoded.roomCode)"
                    lblPlayers.text = "Oyuncular: \(formatPlayers(decoded.players))"
                    lblStatus.text = decoded.message

                    // Android ile aynı davranış:
                    // Oda kuran oyuncu direkt placement ekranına geçsin
                    navigateToPlacementIfNeeded(source: "ROOM_CREATED")
                }

            case "JOINED_ROOM":
                if let decoded = try? decoder.decode(JoinedRoomData.self, from: data) {
                    print("[Lobby] JOINED_ROOM -> roomCode=\(decoded.roomCode), playerId=\(decoded.playerId), players=\(decoded.players.count)")

                    currentRoomCode = decoded.roomCode
                    currentPlayerId = decoded.playerId

                    txtRoomCode.text = decoded.roomCode
                    lblRoomInfo.text = "Oda: \(decoded.roomCode)"
                    lblPlayers.text = "Oyuncular: \(formatPlayers(decoded.players))"
                    lblStatus.text = decoded.message

                    // Android ile aynı davranış:
                    // Odaya katılan oyuncu da direkt placement ekranına geçsin
                    navigateToPlacementIfNeeded(source: "JOINED_ROOM")
                }

            case "PLAYER_JOINED":
                if let decoded = try? decoder.decode(PlayerJoinedData.self, from: data) {
                    print("[Lobby] PLAYER_JOINED -> roomCode=\(decoded.roomCode), players=\(decoded.players.count)")

                    lblRoomInfo.text = "Oda: \(decoded.roomCode)"
                    lblPlayers.text = "Oyuncular: \(formatPlayers(decoded.players))"
                    lblStatus.text = decoded.message

                    // Burada artık geçiş yapmıyoruz.
                    // Çünkü iki taraf da zaten placement ekranına geçmiş olacak.
                }

            case "PLAYER_LEFT":
                if let decoded = try? decoder.decode(PlayerJoinedData.self, from: data) {
                    print("[Lobby] PLAYER_LEFT -> players=\(decoded.players.count)")

                    lblPlayers.text = "Oyuncular: \(formatPlayers(decoded.players))"
                    lblStatus.text = decoded.message
                }

            case "ERROR":
                if let decoded = try? decoder.decode(ErrorData.self, from: data) {
                    print("[Lobby] ERROR -> \(decoded.message)")
                    lblStatus.text = "Hata: \(decoded.message)"
                    showAlert(message: decoded.message)
                }

            default:
                print("[Lobby] Bilinmeyen mesaj tipi -> \(type)")
                lblStatus.text = "Bilinmeyen mesaj: \(type)"
            }
        }
    private func checkAndNavigateIfRoomReady(players: [PlayerInfo], source: String) {
            print("[Lobby] checkAndNavigateIfRoomReady source=\(source), playersCount=\(players.count), currentRoomCode=\(currentRoomCode), currentPlayerId=\(currentPlayerId)")

            guard players.count == 2 else {
                print("[Lobby] Henüz 2 oyuncu yok, geçiş yapılmayacak")
                return
            }

            guard !currentRoomCode.isEmpty else {
                print("[Lobby] roomCode boş, geçiş yapılamadı")
                return
            }

            guard !currentPlayerId.isEmpty else {
                print("[Lobby] playerId boş, geçiş yapılamadı")
                return
            }

            guard !didNavigateToPlacement else {
                print("[Lobby] Zaten geçiş yapıldı, tekrar yapılmayacak")
                return
            }

            didNavigateToPlacement = true

            // Küçük gecikme bazen UI güncellenmesi için iyi olur
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) { [weak self] in
                self?.goToPlacementScreen()
            }
        }
    private func navigateToPlacementIfNeeded(source: String) {
        print("[Lobby] navigateToPlacementIfNeeded source=\(source), roomCode=\(currentRoomCode), playerId=\(currentPlayerId)")

        guard !currentRoomCode.isEmpty else {
            print("[Lobby] roomCode boş, geçiş yapılamadı")
            return
        }

        guard !currentPlayerId.isEmpty else {
            print("[Lobby] playerId boş, geçiş yapılamadı")
            return
        }

        guard !didNavigateToPlacement else {
            print("[Lobby] zaten placement ekranına geçildi")
            return
        }

        didNavigateToPlacement = true

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) { [weak self] in
            self?.goToPlacementScreen()
        }
    }
    private func formatPlayers(_ players: [PlayerInfo]) -> String {
            guard !players.isEmpty else { return "-" }
            return players.map { $0.name }.joined(separator: " | ")
        }

        private func goToPlacementScreen() {
            print("[Lobby] goToPlacementScreen çağrıldı")

            guard let storyboard = storyboard else {
                print("[Lobby] storyboard nil")
                lblStatus.text = "Hata: Storyboard bulunamadı"
                didNavigateToPlacement = false
                return
            }

            guard let vc = storyboard.instantiateViewController(withIdentifier: "ShipPlacementViewController") as? ShipPlacementViewController else {
                print("[Lobby] ShipPlacementViewController identifier hatalı veya class eşleşmiyor")
                lblStatus.text = "Hata: ShipPlacementViewController açılamadı"
                didNavigateToPlacement = false
                return
            }

            vc.roomCode = currentRoomCode
            vc.playerId = currentPlayerId
            vc.playerName = txtPlayerName.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

            print("[Lobby] Placement ekranına gidiliyor -> roomCode=\(vc.roomCode), playerId=\(vc.playerId), playerName=\(vc.playerName)")

            guard let nav = navigationController else {
                print("[Lobby] navigationController nil -> push yapılamadı")
                lblStatus.text = "Hata: Navigation controller yok"
                didNavigateToPlacement = false
                return
            }

            nav.pushViewController(vc, animated: true)
        }

        private func showAlert(message: String) {
            let alert = UIAlertController(title: "Bilgi", message: message, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Tamam", style: .default))
            present(alert, animated: true)
        }
}
extension LobbyViewController: SocketManagerDelegate {

    func socketDidConnect() {
        print("[Lobby] socketDidConnect")
        lblStatus.text = "Durum: Sunucuya bağlandı"
    }

    func socketDidDisconnect() {
        print("[Lobby] socketDidDisconnect")
        lblStatus.text = "Durum: Bağlantı kesildi"
    }

    func socketDidReceiveMessage(_ text: String) {
        print("[Lobby] socketDidReceiveMessage")
        handleSocketMessage(text)
    }

    func socketDidReceiveError(_ errorMessage: String) {
        print("[Lobby] socketDidReceiveError -> \(errorMessage)")
        lblStatus.text = "Hata: \(errorMessage)"
    }
}

