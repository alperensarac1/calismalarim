import SwiftUI

struct AppRoot: View {
    @StateObject private var tokenStore = TokenStore()

    var body: some View {
        let api = APIClient(tokenStore: tokenStore)
        let authVM = AuthVM(auth: AuthService(api: api), tokenStore: tokenStore)

        if tokenStore.token == nil {
            LoginView(vm: authVM)
        } else {
            HomeView(vm: HomeVM(api: api), api: api)
        }
    }
}
