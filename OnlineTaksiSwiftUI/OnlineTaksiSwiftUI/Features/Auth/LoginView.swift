import SwiftUI

struct LoginView: View {
    @EnvironmentObject var sessionManager: SessionManager
    @EnvironmentObject var router: AppRouter

    @StateObject private var viewModel: AuthViewModel

    @State private var phone = ""
    @State private var password = ""

    init() {
        let tempSession = SessionManager()
        let apiClient = APIClient(sessionManager: tempSession)
        let repo = AuthRepository(apiClient: apiClient)
        _viewModel = StateObject(wrappedValue: AuthViewModel(authRepository: repo, sessionManager: tempSession))
    }

    var body: some View {
        VStack(spacing: 16) {
            Spacer()

            Text("onlinetaksi Giriş")
                .font(.largeTitle)
                .bold()

            TextField("Telefon", text: $phone)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.phonePad)

            SecureField("Şifre", text: $password)
                .textFieldStyle(.roundedBorder)

            Button {
                Task {
                    await viewModel.login(
                        phone: phone.trimmingCharacters(in: .whitespaces),
                        password: password.trimmingCharacters(in: .whitespaces)
                    )
                    handleLoginResult()
                }
            } label: {
                if case .loading = viewModel.loginState {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Giriş Yap")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(phone.isEmpty || password.isEmpty)

            Button("Hesabın yok mu? Kayıt ol") {
                router.route = .register
            }

            if case .failure(let error) = viewModel.loginState {
                Text(error)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }

            Spacer()
        }
        .padding(24)
    }

    private func handleLoginResult() {
        switch viewModel.loginState {
        case .success(let role):
            sessionManager.load()
            if role == "driver" {
                router.route = .driverHome
            } else {
                router.route = .customerHome
            }
        default:
            break
        }
    }
}
