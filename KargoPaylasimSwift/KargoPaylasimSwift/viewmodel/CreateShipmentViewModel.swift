//
//  CreateShipmentViewModel.swift
//  KargoPaylasimSwift
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
@MainActor
final class CreateShipmentViewModel {

    enum UiState<T> {
        case idle
        case loading
        case success(T)
        case error(String)
    }

    var onLookup: ((UiState<LookupReceiverData>) -> Void)?
    var onCreate: ((UiState<CreateShipmentData>) -> Void)?

    private let api: APIClient

    init(api: APIClient) {
        self.api = api
    }

    func lookupReceiver(phoneE164: String) {
        Task {
            onLookup?(.loading)
            do {
                // ✅ senin dosya: receiver_lookup.php
                let res: ApiResp<LookupReceiverData> = try await api.postJSON(
                    "receiver_lookup.php",
                    body: LookupReceiverReq(phone: phoneE164),
                    resp: ApiResp<LookupReceiverData>.self
                )
                guard res.ok, let data = res.data else {
                    onLookup?(.error(res.error ?? "User not found"))
                    return
                }
                onLookup?(.success(data))
            } catch {
                onLookup?(.error(error.localizedDescription))
            }
        }
    }

    func createShipment(receiverPhoneE164: String) {
        Task {
            onCreate?(.loading)
            do {
                let res: ApiResp<CreateShipmentData> = try await api.postJSON(
                    "shipment_create.php",
                    body: CreateShipmentReq(receiver_phone: receiverPhoneE164, sender_address_id: nil),
                    resp: ApiResp<CreateShipmentData>.self
                )
                guard res.ok, let data = res.data else {
                    onCreate?(.error(res.error ?? "Create shipment failed"))
                    return
                }
                onCreate?(.success(data))
            } catch {
                onCreate?(.error(error.localizedDescription))
            }
        }
    }
}
