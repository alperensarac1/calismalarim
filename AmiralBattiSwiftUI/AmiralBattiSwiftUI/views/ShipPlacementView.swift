//
//  ShipPlacementView.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
import SwiftUI

struct ShipPlacementView: View {
    let roomCode: String
    let playerId: String
    let playerName: String

    @StateObject private var viewModel = ShipPlacementViewModel()
    @Binding var path: [AppRoute]

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 2), count: 10)

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                Text("Gemi Yerleştirme")
                    .font(.largeTitle)
                    .bold()

                VStack(alignment: .leading, spacing: 8) {
                    Text("Oda: \(roomCode)")
                    Text("Oyuncu: \(playerName)")
                    Text(viewModel.currentShipText)
                    Text(viewModel.orientationText)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(12)

                Button("Yönü Değiştir") {
                    viewModel.rotateShip()
                }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)

                Button("Tahtayı Sıfırla") {
                    viewModel.resetBoard()
                }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)

                LazyVGrid(columns: columns, spacing: 2) {
                    ForEach(viewModel.boardCells) { cell in
                        BoardCellView(cell: cell)
                            .onTapGesture {
                                viewModel.onCellTapped(row: cell.row, col: cell.col)
                            }
                    }
                }
                .padding(4)
                .background(Color.gray.opacity(0.15))
                .cornerRadius(10)

                Text(viewModel.statusText)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(12)

                Button("Hazırım") {
                    viewModel.sendBoardToServer()
                }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
                .disabled(!viewModel.readyEnabled)
            }
            .padding()
        }
        .navigationTitle("Placement")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.initialize(
                roomCode: roomCode,
                playerId: playerId,
                playerName: playerName
            )
            viewModel.activateSocketListener()
        }
        .onDisappear {
            viewModel.deactivateSocketListener()
        }
        .onChange(of: viewModel.shouldNavigateToGame) { newValue in
            if newValue {
                path.append(
                    .game(
                        roomCode: viewModel.roomCode,
                        playerId: viewModel.playerId,
                        playerName: viewModel.playerName,
                        firstTurnPlayerId: viewModel.firstTurnPlayerId,
                        ownBoardJson: viewModel.ownBoardJson
                    )
                )
                viewModel.consumeGameNavigation()
            }
        }
    }
}

struct BoardCellView: View {
    let cell: BoardCell

    var body: some View {
        Rectangle()
            .fill(cellColor)
            .aspectRatio(1, contentMode: .fit)
            .cornerRadius(4)
    }

    private var cellColor: Color {
        switch cell.state {
        case .empty:
            return Color(red: 217/255, green: 234/255, blue: 247/255)
        case .ship:
            return Color(red: 91/255, green: 124/255, blue: 153/255)
        case .hit:
            return .red
        case .miss:
            return .white
        }
    }
}
