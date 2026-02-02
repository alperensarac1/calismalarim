import Foundation

@MainActor
final class AuthViewModel {

    enum State {
        case idle
        case loading
        case success
        case error(String)
    }

    var onLoginState: ((State) -> Void)?

    private let api: APIClient
    private let tokenStore: TokenStore

    init(api: APIClient, tokenStore: TokenStore) {
        self.api = api
        self.tokenStore = tokenStore
    }

    struct LoginReq: Codable {
        let phone: String
        let password: String
    }

    struct LoginData: Codable {
        let token: String
        let user_id: Int
    }

    struct ApiResp<T: Codable>: Codable {
        let ok: Bool
        let message: String?
        let data: T?
    }

    func login(phone: String, password: String) {
        Task {
            onLoginState?(.loading)
            do {
                let res: ApiResp<LoginData> = try await api.postJSON(
                    "user_login.php",
                    body: LoginReq(phone: phone, password: password),
                    resp: ApiResp<LoginData>.self
                )

                guard res.ok, let data = res.data else {
                    onLoginState?(.error(res.message ?? "Giriş başarısız"))
                    return
                }

                tokenStore.save(data.token)

                onLoginState?(.success)
            } catch {
                onLoginState?(.error(error.localizedDescription))
            }
        }
    }
}
