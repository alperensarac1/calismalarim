import SwiftUI

struct RegisterView: View {
    @EnvironmentObject var sessionManager: SessionManager
    @EnvironmentObject var router: AppRouter

    @StateObject private var viewModel: AuthViewModel

    @State private var fullName = ""
    @State private var phone = ""
    @State private var email = ""
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

            Text("Müşteri Kayıt")
                .font(.largeTitle)
                .bold()

            TextField("Ad Soyad", text: $fullName)
                .textFieldStyle(.roundedBorder)

            TextField("Telefon", text: $phone)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.phonePad)

            TextField("Email (opsiyonel)", text: $email)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.emailAddress)

            SecureField("Şifre", text: $password)
                .textFieldStyle(.roundedBorder)

            Button {
                Task {
                    await viewModel.register(
                        fullName: fullName.trimmingCharacters(in: .whitespaces),
                        phone: phone.trimmingCharacters(in: .whitespaces),
                        email: email.trimmingCharacters(in: .whitespaces).isEmpty ? nil : email.trimmingCharacters(in: .whitespaces),
                        password: password.trimmingCharacters(in: .whitespaces)
                    )
                    handleRegisterResult()
                }
            } label: {
                if case .loading = viewModel.registerState {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Kayıt Ol")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(fullName.isEmpty || phone.isEmpty || password.isEmpty)

            Button("Geri Dön") {
                router.route = .login
            }

            if case .failure(let error) = viewModel.registerState {
                Text(error)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }

            Spacer()
        }
        .padding(24)
    }

    private func handleRegisterResult() {
        switch viewModel.registerState {
        case .success:
            sessionManager.load()
            router.route = .customerHome
        default:
            break
        }
    }
}
