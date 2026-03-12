//
//  OpenListVM.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation

final class OpenListVM {
    func fetchOpen(helperId: Int) async -> Result<[OpenHelpItem], AppError> {
        do {
            let res: ApiOk<OpenHelpItem> = try await ApiClient.shared.get(
                "help_list_open.php",
                query: ["helper_id": "\(helperId)"]
            )

            if res.ok == true {
                return .success(res.items ?? [])
            } else {
                return .failure(.message(res.error ?? "Liste alınamadı"))
            }
        } catch {
            return .failure(.message("Bağlantı hatası"))
        }
    }

    func accept(requestId: Int, helperId: Int) async -> Result<Void, AppError> {
        do {
            let res: ApiOk<EmptyDTO> = try await ApiClient.shared.post(
                "help_accept.php",
                body: HelpAcceptBody(request_id: requestId, helper_id: helperId)
            )

            if res.ok == true {
                return .success(())
            } else {
                return .failure(.message(res.error ?? "Kabul edilemedi"))
            }
        } catch {
            return .failure(.message("Bağlantı hatası"))
        }
    }
}
