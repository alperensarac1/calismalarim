//
//  LoginVM.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 1.03.2026.
//

import Foundation

final class LoginVM {
    func login(phone: String, pass: String) async -> Result<Role, AppError> {
        if phone.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || pass.isEmpty {
            return .failure(.message("Telefon ve şifre zorunlu"))
        }

        do {
            let res: ApiOk<EmptyDTO> = try await ApiClient.shared.post(
                "auth_login.php",
                body: LoginBody(telefon: phone, sifre: pass)
            )

            if res.ok == true, let u = res.user {
                Session.save(id: u.id, role: u.role)
                return .success(u.role)
            }

            return .failure(.message(res.error ?? "Giriş başarısız"))
        } catch {
            return .failure(.message("Bağlantı hatası"))
        }
    }
}
