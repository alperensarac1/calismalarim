//
//  AcceptedVM.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation

final class AcceptedVM {
    func fetchAccepted(helperId: Int) async -> AcceptedHelpItem? {
        do {
            let res: ApiOk<AcceptedHelpItem> = try await ApiClient.shared.get(
                "help_my_accepted.php",
                query: ["helper_id": "\(helperId)"]
            )
            return (res.ok == true) ? res.items?.first : nil
        } catch {
            return nil
        }
    }
}
