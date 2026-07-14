import SwiftUI

/*
    RootView

    Uygulamanın giriş durumuna göre root ekranı seçer.
*/
struct RootView: View {

    @EnvironmentObject private var appState: AppState

    var body: some View {
        Group {
            if appState.isLoggedIn {
                HomeView()
            } else {
                LoginView()
            }
        }
    }
}
