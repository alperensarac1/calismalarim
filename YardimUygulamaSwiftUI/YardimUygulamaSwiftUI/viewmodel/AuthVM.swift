//
//  AuthVM.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation

@MainActor
final class AuthVM: ObservableObject {
    @Published var phone = ""
    @Published var pass = ""
    @Published var loading = false
    @Published var error: String = ""

    func login() async -> Role? {
        error = ""
        if phone.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || pass.isEmpty {
            error = "Telefon ve şifre zorunlu"
            return nil
        }

        loading = true
        defer { loading = false }

        do {
            let res: ApiOk<AnyCodable> = try await ApiClient.shared.post(
                "auth_login.php",
                body: LoginBody(telefon: phone, sifre: pass),
                response: ApiOk<AnyCodable>.self
            )
            if res.ok == true, let u = res.user {
                Session.save(id: u.id, role: u.role)
                return u.role
            }
            error = res.error ?? "Giriş başarısız"
            return nil
        } catch {
            self.error = error.localizedDescription
            return nil
        }
    }
}

/// JSON'da items/active gibi generic boş kalabilsin diye küçük helper
struct AnyCodable: Codable {}
