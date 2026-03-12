//
//  PatientVM.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation


@MainActor
final class PatientVM: ObservableObject {
    @Published var servis = ""
    @Published var oda = ""
    @Published var statusText = ""
    @Published var active: HelpActive? = nil
    @Published var loading = false

    private var pollTask: Task<Void, Never>?

    func startPolling(patientId: Int) {
        if pollTask != nil { return }
        pollTask = Task {
            while !Task.isCancelled {
                await fetchActive(patientId: patientId)
                try? await Task.sleep(nanoseconds: 2_500_000_000)
            }
        }
    }

    func stopPolling() {
        pollTask?.cancel()
        pollTask = nil
    }

    func fetchActive(patientId: Int) async {
        do {
            let res: ApiOk<HelpActive> = try await ApiClient.shared.get(
                "help_my_active.php",
                query: ["patient_id": "\(patientId)"],
                response: ApiOk<HelpActive>.self
            )
            self.active = (res.ok == true) ? res.active : nil
        } catch {
            // aktif yoksa sessiz geçiyoruz
        }
    }

    func createHelp(patientId: Int, lat: Double, lng: Double) async {
        statusText = ""
        if servis.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || oda.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            statusText = "Servis ve oda zorunlu"
            return
        }

        loading = true
        defer { loading = false }

        do {
            let res: ApiOk<AnyCodable> = try await ApiClient.shared.post(
                "help_create.php",
                body: HelpCreateBody(patient_id: patientId, servis_adi: servis, oda_no: oda, lat: lat, lng: lng),
                response: ApiOk<AnyCodable>.self
            )
            statusText = (res.ok == true) ? "Durum: OPEN (yardımcı bekleniyor)" : (res.error ?? "İstek gönderilemedi")
        } catch {
            statusText = error.localizedDescription
        }
    }

    func confirm(requestId: Int, patientId: Int) async {
        loading = true
        defer { loading = false }
        do {
            let res: ApiOk<AnyCodable> = try await ApiClient.shared.post(
                "help_confirm.php",
                body: HelpConfirmBody(request_id: requestId, patient_id: patientId),
                response: ApiOk<AnyCodable>.self
            )
            statusText = (res.ok == true) ? "Durum: CONFIRMED" : (res.error ?? "Onaylanamadı")
        } catch {
            statusText = error.localizedDescription
        }
    }

    func cancel(requestId: Int, patientId: Int) async {
        loading = true
        defer { loading = false }
        do {
            let res: ApiOk<AnyCodable> = try await ApiClient.shared.post(
                "help_cancel.php",
                body: HelpCancelBody(request_id: requestId, patient_id: patientId),
                response: ApiOk<AnyCodable>.self
            )
            statusText = (res.ok == true) ? "İstek iptal edildi" : (res.error ?? "İptal edilemedi")
        } catch {
            statusText = error.localizedDescription
        }
    }
}
