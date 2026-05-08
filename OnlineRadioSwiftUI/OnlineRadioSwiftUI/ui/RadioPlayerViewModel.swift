//
//  RadioPlayerViewModel.swift
//  OnlineRadioSwiftUI
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation
import AVFoundation

final class RadioPlayerViewModel: ObservableObject {

    @Published var musicTitle: String = "Çalan müzik bekleniyor..."
    @Published var statusText: String = "Odaya bağlanılıyor..."

    private let socket = RadioSocketManager.shared

    private var player: AVPlayer?
    private var currentMusicUrl: String?

    private var syncTimer: Timer?

    let roomId: Int

    init(roomId: Int) {
        self.roomId = roomId
    }

    func start() {
        socket.onMessage = { [weak self] message in
            self?.handleMessage(message)
        }

        socket.onError = { [weak self] error in
            self?.statusText = "Bağlantı hatası: \(error)"
        }

        socket.connect()
        socket.joinRoom(roomId: roomId)

        syncTimer = Timer.scheduledTimer(withTimeInterval: 5, repeats: true) { [weak self] _ in
            guard let self else { return }
            self.socket.requestSync(roomId: self.roomId)
        }
    }

    private func handleMessage(_ message: String) {
        guard let data = message.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let type = json["type"] as? String else {
            return
        }

        if type == "PLAYBACK_STATE" {
            guard let incomingRoomId = json["roomId"] as? Int,
                  incomingRoomId == roomId,
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
            musicTitle = "Bu odada şu an müzik yok"
            statusText = "Bekleniyor..."
            player?.pause()
        }
    }

    private func playOrSyncMusic(
        title: String,
        musicUrl: String,
        positionSeconds: Double
    ) {
        musicTitle = title
        statusText = "Dinleniyor..."

        guard let url = URL(string: musicUrl) else {
            statusText = "Müzik URL hatalı"
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

    func stop() {
        syncTimer?.invalidate()
        syncTimer = nil

        player?.pause()
        player = nil
    }
}
