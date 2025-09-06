import Foundation
import Combine

@MainActor
final class RegisterViewModel: ObservableObject {

    @Published var registerResult: KullaniciResponse? = nil
    @Published var isLoading: Bool = false

    func registerUser(username: String, password: String) {
        isLoading = true
        Task {
            defer { isLoading = false }
            do {
                let res = try await APIService.shared.register(username: username, password: password)
                registerResult = res
            } catch {
                registerResult = KullaniciResponse(success: false,
                                                   message: "Bağlantı hatası: \(error.localizedDescription)",
                                                   userId: -1)
            }
        }
    }
}
