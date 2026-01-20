//
//  CartViewModel.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI

@MainActor
final class CartVM: ObservableObject {
    @Published var cart: CartDto?
    @Published var isLoading = false
    @Published var errorMessage: String?

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            cart = try await ApiClient.shared.getCart()
            isLoading = false
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    func update(itemId: Int, qty: Int) async {
        isLoading = true
        do {
            _ = try await ApiClient.shared.updateCartItem(itemId: itemId, quantity: qty)
            cart = try await ApiClient.shared.getCart()
            isLoading = false
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }

    func delete(itemId: Int) async {
        isLoading = true
        do {
            _ = try await ApiClient.shared.deleteCartItem(itemId: itemId)
            cart = try await ApiClient.shared.getCart()
            isLoading = false
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }
}
