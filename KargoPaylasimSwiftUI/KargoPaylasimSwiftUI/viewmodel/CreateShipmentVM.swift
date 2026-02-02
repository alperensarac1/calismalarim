import Foundation
import SwiftUI

@MainActor
final class CreateShipmentVM: ObservableObject {
    @Published var phone = ""
    @Published var lookupText: String?
    @Published var canConfirm = false
    @Published var isLoading = false
    @Published var errorText: String?

    private let api: APIClient
    private var confirmedPhoneE164: String?

    init(api: APIClient) { self.api = api }

    func reset() {
        lookupText = nil
        canConfirm = false
        confirmedPhoneE164 = nil
        errorText = nil
    }

    func lookup() async {
        reset()

        let normalized = PhoneUtil.normalizeTrToE164(phone)
        if !PhoneUtil.isLikelyTrPhoneE164(normalized) {
            errorText = "Telefon formatı hatalı. Örn: 05xx... veya +905xx..."
            return
        }
        phone = normalized

        isLoading = true
        defer { isLoading = false }

        do {
            let res: ApiResp<LookupReceiverData> = try await api.postJSON(
                .receiverLookup,
                body: LookupReceiverReq(phone: normalized),
                as: ApiResp<LookupReceiverData>.self
            )

            guard res.ok, let d = res.data else {
                throw APIError.server(res.error ?? "User not found")
            }

            lookupText = "Bulunan: \(d.masked_first_name) \(d.masked_last_name) • Onaylıyor musun?"
            canConfirm = true
            confirmedPhoneE164 = normalized

        } catch {
            errorText = error.localizedDescription
        }
    }

    func confirmCreate() async -> CreateShipmentData? {
        guard let p = confirmedPhoneE164 else {
            errorText = "Önce kişiyi bul."
            return nil
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let res: ApiResp<CreateShipmentData> = try await api.postJSON(
                .shipmentCreate,
                body: CreateShipmentReq(receiver_phone: p, sender_address_id: nil),
                as: ApiResp<CreateShipmentData>.self
            )

            guard res.ok, let d = res.data else {
                throw APIError.server(res.error ?? "Create shipment failed")
            }
            return d

        } catch {
            let msg = error.localizedDescription
            if msg.lowercased().contains("receiver address not found") || msg.uppercased().contains("RECEIVER_ADDRESS_MISSING") {
                errorText = "Bu kullanıcı henüz adresini kaydetmemiş."
            } else {
                errorText = msg
            }
            return nil
        }
    }
}
