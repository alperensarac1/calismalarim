//
//  HomeVMv.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation

@MainActor
final class HomeVM: ObservableObject {
    @Published var shipments: [Shipment] = []
    @Published var addresses: [Address] = []
    @Published var isLoading = false
    @Published var errorText: String?

    private let api: APIClient
    init(api: APIClient) { self.api = api }

    func refresh() async {
        isLoading = true
        defer { isLoading = false }
        errorText = nil

        do {
            async let s: ApiResp<ShipmentListData> = api.get(
                .shipmentList(type: "all"),
                query: ["type": "all"],
                as: ApiResp<ShipmentListData>.self
            )

            async let a: ApiResp<AddressListData> = api.get(
                .addressList,
                as: ApiResp<AddressListData>.self
            )

            let ship = try await s
            let addr = try await a

            guard ship.ok else { throw APIError.server(ship.error ?? "shipment_list failed") }
            guard addr.ok else { throw APIError.server(addr.error ?? "address_list failed") }

            shipments = ship.data?.items ?? []
            addresses = addr.data?.items ?? []
        } catch {
            errorText = error.localizedDescription
        }
    }

    func setDefaultAddress(_ id: Int) async {
        isLoading = true
        defer { isLoading = false }
        errorText = nil

        do {
            struct Req: Encodable { let id: Int }

            let res: ApiResp<Bool> = try await api.postJSON(
                .addressSetDefault,
                body: Req(id: id),
                as: ApiResp<Bool>.self
            )

            guard res.ok else { throw APIError.server(res.error ?? "Varsayılan ayarlanamadı") }

            // UI'yi anında güncelle
            addresses = addresses.map { a in
                Address(
                    id: a.id,
                    title: a.title,
                    city: a.city,
                    district: a.district,
                    address_line: a.address_line,
                    is_default: (a.id == id ? 1 : 0)
                )
            }
        } catch {
            errorText = error.localizedDescription
        }
    }

    func deleteAddress(_ id: Int) async {
        isLoading = true
        defer { isLoading = false }
        errorText = nil

        do {
            struct Req: Encodable { let id: Int }

            let res: ApiResp<Bool> = try await api.postJSON(
                .addressDelete,
                body: Req(id: id),
                as: ApiResp<Bool>.self
            )

            guard res.ok else { throw APIError.server(res.error ?? "Adres silinemedi") }

            // UI'den kaldır
            addresses.removeAll { $0.id == id }
        } catch {
            errorText = error.localizedDescription
        }
    }
}
