//
//  RegisterVM.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 1.03.2026.
//

import Foundation

final class RegisterVM {
    func register(body: RegisterBody) async -> Result<Role, AppError> {
        do {
            let res: ApiOk<EmptyDTO> = try await ApiClient.shared.post("auth_register.php", body: body)
            if res.ok == true, let u = res.user {
                Session.save(id: u.id, role: u.role)
                return .success(u.role)
            }
            return .failure(.message(res.error ?? "Kayıt başarısız"))
        } catch {
            return .failure(.message("Bağlantı hatası"))
        }
    }
}
