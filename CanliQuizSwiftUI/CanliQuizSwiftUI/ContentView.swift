import SwiftUI

struct ContentView: View {

    @EnvironmentObject var viewModel: QuizViewModel

    var body: some View {
        NavigationStack(path: $viewModel.path) {
            HomeView()
                .navigationDestination(for: AppScreen.self) { screen in
                    switch screen {
                    case .home:
                        HomeView()

                    case .createRoom:
                        CreateRoomView()

                    case .joinRoom:
                        JoinRoomView()

                    case .ownerRoom:
                        OwnerRoomView()

                    case .waitingRoom:
                        WaitingRoomView()

                    case .quiz:
                        QuizView()

                    case .winner:
                        WinnerView()
                    }
                }
        }
    }
}
