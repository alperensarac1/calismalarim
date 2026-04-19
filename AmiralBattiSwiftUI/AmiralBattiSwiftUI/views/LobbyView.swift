//
//  LobbyView.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
import SwiftUI

struct LobbyView: View {

    @ObservedObject var viewModel: LobbyViewModel
    @Binding var path: [AppRoute]

    var body: some View {
        VStack(spacing: 14) {
            Text("Amiral Battı")
                .font(.largeTitle)
                .bold()

            TextField("Oyuncu adı", text: $viewModel.playerName)
                .textFieldStyle(.roundedBorder)

            TextField("Oda kodu", text: $viewModel.roomCode)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.numberPad)

            Button("Sunucuya Bağlan") {
                viewModel.connectToServer()
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)

            Button("Oda Oluştur") {
                viewModel.createRoom()
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)

            Button("Odaya Katıl") {
                viewModel.joinRoom()
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)

            VStack(alignment: .leading, spacing: 8) {
                Text(viewModel.roomInfoText)
                Text(viewModel.playersText)
                Text(viewModel.statusText)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
            .background(Color(.secondarySystemBackground))
            .cornerRadius(12)

            Spacer()
        }
        .padding()
        .navigationTitle("Lobi")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.activateSocketListener()
        }
        .onDisappear {
            viewModel.deactivateSocketListener()
        }
        .onChange(of: viewModel.shouldNavigateToPlacement) { newValue in
            if newValue {
                path.append(
                    .placement(
                        roomCode: viewModel.currentRoomCode,
                        playerId: viewModel.currentPlayerId,
                        playerName: viewModel.playerName.trimmingCharacters(in: .whitespacesAndNewlines)
                    )
                )
                viewModel.consumePlacementNavigation()
            }
        }
    }
}
