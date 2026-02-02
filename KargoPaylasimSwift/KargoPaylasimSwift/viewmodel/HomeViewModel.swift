import Foundation

@MainActor
final class HomeViewModel {

    enum State {
        case idle
        case loading
        case loaded
        case error(String)
    }

    var onState: ((State) -> Void)?

    private let api: APIClient

    private(set) var shipments: [Shipment] = []
    private(set) var addresses: [Address] = []

    init(api: APIClient) {
        self.api = api
    }

    func refresh() {
        Task {
            onState?(.loading)
            do {
                async let ship: ApiResp<ShipmentListData> = api.getJSON(
                    "shipment_list.php",
                    query: ["type": "all"],
                    resp: ApiResp<ShipmentListData>.self
                )

                async let addr: ApiResp<AddressListData> = api.getJSON(
                    "address_list.php",
                    resp: ApiResp<AddressListData>.self
                )

                let shipRes = try await ship
                let addrRes = try await addr

                guard shipRes.ok else { throw SimpleError(shipRes.error ?? "Gönderiler alınamadı") }
                guard addrRes.ok else { throw SimpleError(addrRes.error ?? "Adresler alınamadı") }

                self.shipments = shipRes.data?.items ?? []
                self.addresses = addrRes.data?.items ?? []

                onState?(.loaded)
            } catch {
                onState?(.error(error.localizedDescription))
            }
        }
    }

    // Address actions
    struct IdBody: Encodable { let id: Int }

    func setDefaultAddress(id: Int) async throws {
        let res: ApiResp<Bool> = try await api.postJSON("address_set_default.php", body: IdBody(id: id), resp: ApiResp<Bool>.self)
        guard res.ok else { throw SimpleError(res.error ?? "Varsayılan yapılamadı") }
    }

    func deleteAddress(id: Int) async throws {
        let res: ApiResp<Bool> = try await api.postJSON("address_delete.php", body: IdBody(id: id), resp: ApiResp<Bool>.self)
        guard res.ok else { throw SimpleError(res.error ?? "Adres silinemedi") }
    }
}

private struct SimpleError: LocalizedError {
    let message: String
    init(_ message: String) { self.message = message }
    var errorDescription: String? { message }
}
