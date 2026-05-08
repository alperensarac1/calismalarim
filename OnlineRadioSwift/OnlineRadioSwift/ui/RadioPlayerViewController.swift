//
//  RadioPlayerViewController.swift
//  OnlineRadioSwift
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation
import UIKit
import AVFoundation

final class RadioPlayerViewController: UIViewController {

    @IBOutlet weak var roomNameLabel: UILabel!
    @IBOutlet weak var musicTitleLabel: UILabel!
    @IBOutlet weak var statusLabel: UILabel!

    var room: RadioRoom?

    private let socket = RadioSocketManager.shared

    private var player: AVPlayer?
    private var currentMusicUrl: String?

    private var syncTimer: Timer?

    override func viewDidLoad() {
        super.viewDidLoad()

        roomNameLabel.text = room?.roomName ?? "Oda"
        musicTitleLabel.text = "Çalan müzik bekleniyor..."
        statusLabel.text = "Odaya bağlanılıyor..."

        setupSocket()

        if let roomId = room?.id {
            socket.joinRoom(roomId: roomId)
            startSyncTimer(roomId: roomId)
        }
    }

    private func setupSocket() {
        socket.onMessage = { [weak self] message in
            self?.handleSocketMessage(message)
        }

        socket.onError = { [weak self] error in
            self?.statusLabel.text = "Bağlantı hatası: \(error)"
        }

        socket.connect()
    }

    private func startSyncTimer(roomId: Int) {
        syncTimer?.invalidate()

        syncTimer = Timer.scheduledTimer(
            withTimeInterval: 5,
            repeats: true
        ) { [weak self] _ in
            self?.socket.requestSync(roomId: roomId)
        }
    }

    private func handleSocketMessage(_ message: String) {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let type = json["type"] as? String else {
            return
        }

        if type == "PLAYBACK_STATE" {
            guard let currentRoomId = room?.id,
                  let incomingRoomId = json["roomId"] as? Int,
                  incomingRoomId == currentRoomId,
                  let title = json["title"] as? String,
                  let musicUrl = json["musicUrl"] as? String else {
                return
            }

            let positionSeconds = json["positionSeconds"] as? Double ?? 0.0

            playOrSyncMusic(
                title: title,
                musicUrl: musicUrl,
                positionSeconds: positionSeconds
            )
        }

        else if type == "NO_MUSIC" {
            musicTitleLabel.text = "Bu odada şu an müzik yok"
            statusLabel.text = "Bekleniyor..."
            player?.pause()
        }
    }

    private func playOrSyncMusic(
        title: String,
        musicUrl: String,
        positionSeconds: Double
    ) {
        musicTitleLabel.text = title
        statusLabel.text = "Dinleniyor..."

        guard let url = URL(string: musicUrl) else {
            statusLabel.text = "Müzik URL hatalı"
            return
        }

        let targetTime = CMTime(
            seconds: positionSeconds,
            preferredTimescale: 1000
        )

        if currentMusicUrl != musicUrl {
            currentMusicUrl = musicUrl

            player = AVPlayer(url: url)
            player?.seek(to: targetTime)
            player?.play()
            return
        }

        let currentSeconds = player?.currentTime().seconds ?? 0
        let difference = abs(currentSeconds - positionSeconds)

        if difference > 1.2 {
            player?.seek(to: targetTime)
        }

        player?.play()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        syncTimer?.invalidate()
        syncTimer = nil

        player?.pause()
        player = nil
    }
}
