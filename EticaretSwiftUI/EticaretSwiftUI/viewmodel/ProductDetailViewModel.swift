//
//  ProductDetailViewModel.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation

@MainActor
final class ProductDetailViewModel: ObservableObject {
    @Published var product: ProductDto?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var qty: Int = 1
    @Published var toast: String?

    func load(id: Int) async {
        isLoading = true
        errorMessage = nil
        do {
            let dto = try await ApiClient.shared.getProduct(id: id)
            product = dto
            isLoading = false
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    func addToCart(requireLogin: Bool) async -> Bool {
        guard let p = product else { return false }
        if requireLogin { return false }

        isLoading = true
        do {
            _ = try await ApiClient.shared.addToCart(productId: p.id, quantity: qty)
            isLoading = false
            toast = "Sepete eklendi ✅"
            return true
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
            return false
        }
    }
}
