//
//  RoomListViewController.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation
import UIKit

final class RoomListViewController: UIViewController {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!

    private var socketManager: LiveSocketManager?
    private var rooms: [RoomModel] = []

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()
        configureTableView()
        connectSocket()
    }

    private func configureUI() {
        view.backgroundColor = .systemBackground

        title = "Aktif Yayınlar"

        titleLabel.text = "Aktif Yayınlar"
        titleLabel.font = .boldSystemFont(ofSize: 24)

        statusLabel.text = "Sunucuya bağlanıyor..."
        statusLabel.font = .systemFont(ofSize: 14)
        statusLabel.textColor = .secondaryLabel
    }

    private func configureTableView() {
        tableView.dataSource = self
        tableView.delegate = self

        let nib = UINib(
            nibName: RoomTableViewCell.identifier,
            bundle: nil
        )

        tableView.register(
            nib,
            forCellReuseIdentifier: RoomTableViewCell.identifier
        )

        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 90
    }

    private func connectSocket() {
        let manager = LiveSocketManager(urlString: AppConfig.serverURL)
        manager.delegate = self
        manager.connect()

        socketManager = manager
    }

    private func requestRooms() {
        socketManager?.sendJson([
            "type": "get_rooms"
        ])
    }

    private func handleRoomsList(_ data: [String: Any]) {
        guard let roomArray = data["rooms"] as? [[String: Any]] else {
            return
        }

        rooms = roomArray.map { RoomModel(json: $0) }

        DispatchQueue.main.async {
            self.statusLabel.text = self.rooms.isEmpty
                ? "Aktif yayın yok"
                : "\(self.rooms.count) aktif yayın var"

            self.tableView.reloadData()
        }
    }

    deinit {
        socketManager?.disconnect()
    }
}

extension RoomListViewController: LiveSocketManagerDelegate {

    func socketDidConnect() {
        DispatchQueue.main.async {
            self.statusLabel.text = "Sunucuya bağlandı"
        }

        requestRooms()
    }

    func socketDidReceiveMessage(_ message: String) {
        guard let data = message.toJsonDictionary(),
              let type = data["type"] as? String else {
            return
        }

        if type == "rooms_list" {
            handleRoomsList(data)
        }

        if type == "error" {
            DispatchQueue.main.async {
                self.statusLabel.text = data["message"] as? String ?? "Bilinmeyen hata"
            }
        }
    }

    func socketDidDisconnect() {
        DispatchQueue.main.async {
            self.statusLabel.text = "Bağlantı kapandı"
        }
    }

    func socketDidReceiveError(_ error: String) {
        DispatchQueue.main.async {
            self.statusLabel.text = "Hata: \(error)"
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
        guard let cell = tableView.dequeueReusableCell(
            withIdentifier: RoomTableViewCell.identifier,
            for: indexPath
        ) as? RoomTableViewCell else {
            return UITableViewCell()
        }

        cell.configure(with: rooms[indexPath.row])
        return cell
    }

    func tableView(
        _ tableView: UITableView,
        didSelectRowAt indexPath: IndexPath
    ) {
        let selectedRoom = rooms[indexPath.row]

        let vc = ViewerViewController(
            nibName: "ViewerViewController",
            bundle: nil
        )

        vc.room = selectedRoom

        navigationController?.pushViewController(vc, animated: true)
    }
}
