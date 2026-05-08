//
//  RadioPlayerView.swift
//  OnlineRadioSwiftUI
//
//  Created by Alperen Saraç on 2.05.2026.
//

import Foundation
import SwiftUI

struct RadioPlayerView: View {

    let room: RadioRoom

    @StateObject private var viewModel: RadioPlayerViewModel

    init(room: RadioRoom) {
        self.room = room
        _viewModel = StateObject(
            wrappedValue: RadioPlayerViewModel(roomId: room.id)
        )
    }

    var body: some View {
        VStack(spacing: 18) {

            Text(room.roomName)
                .font(.largeTitle)
                .bold()

            Text(viewModel.musicTitle)
                .font(.title3)
                .multilineTextAlignment(.center)

            Text(viewModel.statusText)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Spacer()

            Image(systemName: "dot.radiowaves.left.and.right")
                .font(.system(size: 80))
                .foregroundStyle(.blue)

            Text("Bu ekran sadece dinleyici modudur.")
                .font(.caption)
                .foregroundStyle(.secondary)

            Spacer()
        }
        .padding()
        .onAppear {
            viewModel.start()
        }
        .onDisappear {
            viewModel.stop()
        }
    }
}
