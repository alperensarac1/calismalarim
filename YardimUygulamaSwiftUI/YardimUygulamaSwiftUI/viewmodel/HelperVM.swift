//
//  HelperVM.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation

@MainActor
final class HelperVM: ObservableObject {
    @Published var openItems: [OpenHelpItem] = []
    @Published var accepted: AcceptedHelpItem? = nil
    @Published var history: [ConfirmedHelpItem] = []
    @Published var info = ""

    private var pollOpenTask: Task<Void, Never>?
    private var pollAcceptedTask: Task<Void, Never>?

    func startOpenPolling(helperId: Int) {
        if pollOpenTask != nil { return }
        pollOpenTask = Task {
            while !Task.isCancelled {
                await fetchOpen(helperId: helperId)
                try? await Task.sleep(nanoseconds: 4_000_000_000)
            }
        }
    }

    func stopOpenPolling() { pollOpenTask?.cancel(); pollOpenTask = nil }

    func startAcceptedPolling(helperId: Int) {
        if pollAcceptedTask != nil { return }
        pollAcceptedTask = Task {
            while !Task.isCancelled {
                await fetchAccepted(helperId: helperId)
                try? await Task.sleep(nanoseconds: 2_000_000_000)
            }
        }
    }

    func stopAcceptedPolling() { pollAcceptedTask?.cancel(); pollAcceptedTask = nil }

    func fetchOpen(helperId: Int) async {
        do {
            let res: ApiOk<OpenHelpItem> = try await ApiClient.shared.get(
                "help_list_open.php",
                query: ["helper_id":"\(helperId)"],
                response: ApiOk<OpenHelpItem>.self
            )
            if res.ok == true { openItems = res.items ?? [] }
            else { info = res.error ?? "Liste alınamadı" }
        } catch { info = error.localizedDescription }
    }

    func accept(requestId: Int, helperId: Int) async -> Bool {
        do {
            let res: ApiOk<AnyCodable> = try await ApiClient.shared.post(
                "help_accept.php",
                body: HelpAcceptBody(request_id: requestId, helper_id: helperId),
                response: ApiOk<AnyCodable>.self
            )
            if res.ok == true { return true }
            info = res.error ?? "Kabul edilemedi"
            return false
        } catch {
            info = error.localizedDescription
            return false
        }
    }

    func fetchAccepted(helperId: Int) async {
        do {
            let res: ApiOk<AcceptedHelpItem> = try await ApiClient.shared.get(
                "help_my_accepted.php",
                query: ["helper_id":"\(helperId)"],
                response: ApiOk<AcceptedHelpItem>.self
            )
            accepted = (res.ok == true) ? res.items?.first : nil
        } catch {
            accepted = nil
        }
    }

    func fetchHistory(helperId: Int) async {
        do {
            let res: ApiOk<ConfirmedHelpItem> = try await ApiClient.shared.get(
                "help_my_confirmed.php",
                query: ["helper_id":"\(helperId)"],
                response: ApiOk<ConfirmedHelpItem>.self
            )
            if res.ok == true { history = res.items ?? [] }
            else { info = res.error ?? "Geçmiş alınamadı" }
        } catch {
            info = error.localizedDescription
        }
    }
}
