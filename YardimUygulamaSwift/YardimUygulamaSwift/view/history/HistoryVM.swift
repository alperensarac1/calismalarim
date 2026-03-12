//
//  HistoryVM.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation

final class HistoryVM {
    func fetchHistory(helperId: Int) async -> Result<[ConfirmedHelpItem], AppError> {
        do {
            let res: ApiOk<ConfirmedHelpItem> = try await ApiClient.shared.get(
                "help_my_confirmed.php",
                query: ["helper_id": "\(helperId)"]
            )

            if res.ok == true {
                return .success(res.items ?? [])
            } else {
                return .failure(.message(res.error ?? "Geçmiş alınamadı"))
            }
        } catch {
            return .failure(.message("Bağlantı hatası"))
        }
    }
}
