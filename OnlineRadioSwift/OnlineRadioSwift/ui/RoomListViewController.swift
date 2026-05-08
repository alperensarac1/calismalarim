//
//  RoomListViewController.swift
//  OnlineRadioSwift
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation
import UIKit

final class RoomListViewController: UIViewController {

    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!

    private let socket = RadioSocketManager.shared

    private var rooms: [RadioRoom] = []
    private var selectedRoom: RadioRoom?

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "SyncRadio Odaları"

        tableView.dataSource = self
        tableView.delegate = self

        setupSocket()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        socket.getRooms()
    }

    private func setupSocket() {
        socket.onConnected = { [weak self] in
            self?.statusLabel.text = "Sunucuya bağlandı"
            self?.socket.getRooms()
        }

        socket.onMessage = { [weak self] message in
            self?.handleSocketMessage(message)
        }

        socket.onError = { [weak self] error in
            self?.statusLabel.text = "Hata: \(error)"
        }

        socket.connect()
    }

    private func handleSocketMessage(_ message: String) {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let type = json["type"] as? String else {
            return
        }

        if type == "ROOM_LIST" || type == "ROOM_UPDATED" {
            guard let roomArray = json["rooms"] as? [[String: Any]] else {
                return
            }

            let parsedRooms: [RadioRoom] = roomArray.compactMap { item in
                guard let id = item["id"] as? Int,
                      let roomName = item["roomName"] as? String else {
                    return nil
                }

                return RadioRoom(
                    id: id,
                    roomName: roomName,
                    currentMusic: item["currentMusic"] as? String,
                    isPlaying: item["isPlaying"] as? Bool ?? false,
                    listenerCount: item["listenerCount"] as? Int ?? 0
                )
            }

            self.rooms = parsedRooms
            self.tableView.reloadData()
        }
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "goPlayer",
           let targetVC = segue.destination as? RadioPlayerViewController {
            targetVC.room = selectedRoom
        }
    }
}

extension RoomListViewController: UITableViewDataSource, UITableViewDelegate {

    func tableView(
        _ tableView: UITableView,
        numberOfRowsInSection section: Int
    ) -> Int {
        return rooms.count
    }

    func tableView(
        _ tableView: UITableView,
        cellForRowAt indexPath: IndexPath
    ) -> UITableViewCell {
        let cell = UITableViewCell(style: .subtitle, reuseIdentifier: nil)

        let room = rooms[indexPath.row]

        cell.textLabel?.text = room.roomName

        let musicText: String

        if let currentMusic = room.currentMusic, !currentMusic.isEmpty {
            musicText = "Şu an: \(currentMusic)"
        } else {
            musicText = "Şu an: Müzik yok"
        }

        cell.detailTextLabel?.text = "\(musicText) | Dinleyici: \(room.listenerCount)"
        cell.accessoryType = .disclosureIndicator

        return cell
    }

    func tableView(
        _ tableView: UITableView,
        didSelectRowAt indexPath: IndexPath
    ) {
        tableView.deselectRow(at: indexPath, animated: true)

        selectedRoom = rooms[indexPath.row]
        performSegue(withIdentifier: "goPlayer", sender: self)
    }
}
