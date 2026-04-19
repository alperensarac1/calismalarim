import SwiftUI

struct ContentView: View {

    @StateObject private var lobbyViewModel = LobbyViewModel()
    @State private var path: [AppRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            LobbyView(
                viewModel: lobbyViewModel,
                path: $path
            )
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .lobby:
                    LobbyView(
                        viewModel: lobbyViewModel,
                        path: $path
                    )

                case .placement(let roomCode, let playerId, let playerName):
                    ShipPlacementView(
                        roomCode: roomCode,
                        playerId: playerId,
                        playerName: playerName,
                        path: $path
                    )

                case .game(let roomCode, let playerId, let playerName, let firstTurnPlayerId, let ownBoardJson):
                    GameView(
                        roomCode: roomCode,
                        playerId: playerId,
                        playerName: playerName,
                        firstTurnPlayerId: firstTurnPlayerId,
                        ownBoardJson: ownBoardJson,
                        path: $path
                    )
                }
            }
        }
    }
}
