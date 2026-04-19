//
//  GameView.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
import SwiftUI

struct GameView: View {
    let roomCode: String
    let playerId: String
    let playerName: String
    let firstTurnPlayerId: String
    let ownBoardJson: String

    @StateObject private var viewModel = GameViewModel()
    @Binding var path: [AppRoute]

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 2), count: 10)

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                Text("Oyun Ekranı")
                    .font(.largeTitle)
                    .bold()

                VStack(alignment: .leading, spacing: 8) {
                    Text(viewModel.turnText)
                    Text(viewModel.statusText)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(12)

                Text("Kendi Tahtan")
                    .font(.title3)
                    .bold()
                    .frame(maxWidth: .infinity, alignment: .leading)

                LazyVGrid(columns: columns, spacing: 2) {
                    ForEach(viewModel.ownBoardCells) { cell in
                        BoardCellView(cell: cell)
                    }
                }
                .padding(4)
                .background(Color.gray.opacity(0.15))
                .cornerRadius(10)

                Text("Rakip Tahtası")
                    .font(.title3)
                    .bold()
                    .frame(maxWidth: .infinity, alignment: .leading)

                LazyVGrid(columns: columns, spacing: 2) {
                    ForEach(viewModel.enemyBoardCells) { cell in
                        BoardCellView(cell: cell)
                            .onTapGesture {
                                viewModel.onEnemyCellTapped(row: cell.row, col: cell.col)
                            }
                    }
                }
                .padding(4)
                .background(Color.gray.opacity(0.15))
                .cornerRadius(10)
            }
            .padding()
        }
        .navigationTitle("Oyun")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.initialize(
                roomCode: roomCode,
                playerId: playerId,
                playerName: playerName,
                firstTurnPlayerId: firstTurnPlayerId,
                ownBoardJson: ownBoardJson
            )
            viewModel.activateSocketListener()
        }
        .onDisappear {
            viewModel.deactivateSocketListener()
        }
        .onChange(of: viewModel.shouldNavigateToPlacement) { newValue in
            if newValue {
                path.append(
                    .placement(
                        roomCode: viewModel.roomCode,
                        playerId: viewModel.playerId,
                        playerName: viewModel.playerName
                    )
                )
                viewModel.consumePlacementNavigation()
            }
        }
        .alert(
            viewModel.gameOverWinner ? "Tebrikler" : "Oyun Bitti",
            isPresented: $viewModel.showGameOverDialog
        ) {
            Button("Yeniden Oyna") {
                viewModel.requestRematch()
            }
            Button("Lobiye Dön", role: .cancel) {
                viewModel.dismissGameOverDialog()
                path.removeAll()
            }
        } message: {
            Text(
                viewModel.gameOverWinner
                ? "Rakibin tüm gemilerini batırdın.\n\nYeniden oynamak ister misin?"
                : "Tüm gemilerin batırıldı.\n\nYeniden oynamak ister misin?"
            )
        }
        .alert("Rakip Ayrıldı", isPresented: $viewModel.showPlayerLeftDialog) {
            Button("Lobiye Dön") {
                viewModel.dismissPlayerLeftDialog()
                path.removeAll()
            }
        } message: {
            Text("Rakip oyundan çıktı. Lobiye dönmek ister misin?")
        }
    }
}
