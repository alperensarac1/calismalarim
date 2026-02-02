import Foundation

@MainActor
final class RegisterFlowViewModel {

    enum State {
        case idle
        case loading
        case success
        case error(String)
    }

    var onState: ((State) -> Void)?

    private let api: APIClient
    private let tokenStore: TokenStore

    init(api: APIClient, tokenStore: TokenStore) {
        self.api = api
        self.tokenStore = tokenStore
    }

    struct ApiResp<T: Codable>: Codable {
        let ok: Bool
        let data: T?
        let error: String?
    }


    struct RegisterReq: Codable { let name: String; let phone: String; let password: String }
    struct RegisterData: Codable { } 


    struct LoginReq: Codable { let phone: String; let password: String }
    struct LoginData: Codable { let token: String; let user_id: Int }

    struct AddressCreateReq: Codable {
        let title: String
        let city: String
        let district: String
        let neighborhood: String?      // opsiyonel
        let address_line: String       // zorunlu
        let postal_code: String?       // opsiyonel
        // token göndermemize gerek yok; header'dan gidiyor
    }
    struct AddressCreateData: Codable { let id: Int }

    func registerThenCreateAddress(
        fullName: String,
        phone: String,
        password: String,
        addressTitle: String,
        city: String,
        district: String,
        neighborhood: String,
        addressLine: String,
        postal: String
    ) {
        Task {
            onState?(.loading)
            do {
                // 1) Register
                let reg: ApiResp<RegisterData> = try await api.postJSON(
                    "user_register.php",
                    body: RegisterReq(name: fullName, phone: phone, password: password),
                    resp: ApiResp<RegisterData>.self
                )
                guard reg.ok else { throw SimpleError(reg.error ?? "Kayıt başarısız") }

                // 2) Login (token almak için)
                let log: ApiResp<LoginData> = try await api.postJSON(
                    "user_login.php",
                    body: LoginReq(phone: phone, password: password),
                    resp: ApiResp<LoginData>.self
                )
                guard log.ok, let loginData = log.data else { throw SimpleError(log.error ?? "Giriş başarısız") }

                tokenStore.save(loginData.token)

                // 3) Address create (token header’dan gider)
                let addrReq = AddressCreateReq(
                    title: addressTitle,
                    city: city,
                    district: district,
                    neighborhood: neighborhood.isEmpty ? nil : neighborhood,
                    address_line: addressLine,
                    postal_code: postal.isEmpty ? nil : postal
                )

                let addr: ApiResp<AddressCreateData> = try await api.postJSON(
                    "address_create.php",
                    body: addrReq,
                    resp: ApiResp<AddressCreateData>.self
                )
                guard addr.ok else { throw SimpleError(addr.error ?? "Adres oluşturulamadı") }

                onState?(.success)
            } catch {
                onState?(.error(error.localizedDescription))
            }
        }
    }
}

private struct SimpleError: LocalizedError {
    let message: String
    init(_ message: String) { self.message = message }
    var errorDescription: String? { message }
}
