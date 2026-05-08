//
//  RoomListView.swift
//  OnlineRadioSwiftUI
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation
import SwiftUI

struct RoomListView: View {

    @StateObject private var viewModel = RoomListViewModel()

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 12) {

                Text(viewModel.statusText)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .padding(.horizontal)

                List(viewModel.rooms) { room in
                    NavigationLink {
                        RadioPlayerView(room: room)
                    } label: {
                        RoomRow(room: room)
                    }
                }
                .listStyle(.plain)
                .refreshable {
                    viewModel.refreshRooms()
                }
            }
            .navigationTitle("SyncRadio Odaları")
            .onAppear {
                viewModel.start()
            }
        }
    }
}

struct RoomRow: View {

    let room: RadioRoom

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(room.roomName)
                .font(.headline)

            Text(room.currentMusic?.isEmpty == false
                 ? "Şu an: \(room.currentMusic!)"
                 : "Şu an: Müzik yok")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Text("Dinleyici: \(room.listenerCount)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 6)
    }
}
