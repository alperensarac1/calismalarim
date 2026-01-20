//
//  CheckoutViewModel.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation

@MainActor
final class CheckoutVM: ObservableObject {
    @Published var name = ""
    @Published var phone = ""
    @Published var city = ""
    @Published var address = ""

    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var successMessage: String?

    private func isValidPhone(_ s: String) -> Bool {
        let digits = s.filter(\.isNumber)
        return digits.count >= 10
    }

    func submit() async {
        errorMessage = nil
        successMessage = nil

        let nm = name.trimmingCharacters(in: .whitespacesAndNewlines)
        let ph = phone.trimmingCharacters(in: .whitespacesAndNewlines)
        let ct = city.trimmingCharacters(in: .whitespacesAndNewlines)
        let ad = address.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !nm.isEmpty else { errorMessage = "Ad Soyad boş olamaz."; return }
        guard isValidPhone(ph) else { errorMessage = "Telefon numarası geçersiz."; return }
        guard !ct.isEmpty else { errorMessage = "Şehir boş olamaz."; return }
        guard ad.count >= 10 else { errorMessage = "Adres çok kısa."; return }

        isLoading = true
        do {
            let idem = UUID().uuidString
            let res = try await ApiClient.shared.checkout(
                addressLine1: ad,
                city: ct,
                addressName: nm,
                idempotencyKey: idem
            )
            isLoading = false
            successMessage = "Sipariş alındı ✅\nSipariş No: \(res.order_id)\nToplam: \(String(format: "₺ %.2f", res.total))"
        } catch {
            isLoading = false
            errorMessage = error.localizedDescription
        }
    }
}
