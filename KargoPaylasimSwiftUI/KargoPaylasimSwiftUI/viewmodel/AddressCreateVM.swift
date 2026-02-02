import Foundation
import SwiftUI

@MainActor
final class AddressCreateVM: ObservableObject {
    @Published var title = ""
    @Published var city = ""
    @Published var district = ""
    @Published var neighborhood = ""
    @Published var addressLine = ""
    @Published var postal = ""
    @Published var isLoading = false
    @Published var errorText: String?

    private let api: APIClient
    init(api: APIClient) { self.api = api }

    func save() async -> Bool {
        errorText = nil
        if title.isEmpty || city.isEmpty || district.isEmpty || addressLine.isEmpty {
            errorText = "Başlık, şehir, ilçe ve açık adres zorunlu."
            return false
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let body = AddressCreateReq(
                title: title,
                city: city,
                district: district,
                neighborhood: neighborhood,
                address_line: addressLine,
                postal_code: postal
            )

            let res: ApiResp<AddressCreateData> = try await api.postJSON(
                .addressCreate,
                body: body,
                as: ApiResp<AddressCreateData>.self
            )

            guard res.ok else { throw APIError.server(res.error ?? "Adres eklenemedi") }
            return true
        } catch {
            errorText = error.localizedDescription
            return false
        }
    }
}
