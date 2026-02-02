//
//  AuthVM.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation

@MainActor
final class AuthVM: ObservableObject {
    @Published var isLoading = false
    @Published var errorText: String?

    private let auth: AuthService
    private let tokenStore: TokenStore

    init(auth: AuthService, tokenStore: TokenStore) {
        self.auth = auth
        self.tokenStore = tokenStore
    }

    func doLogin(phoneRaw: String, password: String) async -> Bool {
        errorText = nil

        let phone = PhoneUtil.normalizeTrToE164(phoneRaw)
        guard PhoneUtil.isLikelyTrPhoneE164(phone) else {
            errorText = "Telefon formatı hatalı. Örn: 05xx... veya +905xx..."
            return false
        }
        guard !password.isEmpty else {
            errorText = "Şifre boş olamaz."
            return false
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let d = try await auth.login(phoneE164: phone, password: password)
            tokenStore.token = d.token
            return true
        } catch {
            errorText = error.localizedDescription
            return false
        }
    }

    func doRegister(
        first: String, last: String, phoneRaw: String, tc: String, password: String,
        addrTitle: String, city: String, district: String, neighborhood: String, addressLine: String, postal: String
    ) async -> Bool {
        errorText = nil

        let phone = PhoneUtil.normalizeTrToE164(phoneRaw)
        guard PhoneUtil.isLikelyTrPhoneE164(phone) else {
            errorText = "Telefon formatı hatalı. Örn: 05xx... veya +905xx..."
            return false
        }

        let tcDigits = tc.filter { $0.isNumber }
        guard tcDigits.count == 11 else {
            errorText = "TC 11 haneli olmalı."
            return false
        }

        // PHP register: kullanıcı alanları + adres alanları zorunlu
        guard !first.isEmpty, !last.isEmpty, !password.isEmpty else {
            errorText = "İsim, soyisim ve şifre zorunlu."
            return false
        }
        guard !addrTitle.isEmpty, !city.isEmpty, !district.isEmpty, !addressLine.isEmpty else {
            errorText = "Adres başlığı, şehir, ilçe ve açık adres zorunlu."
            return false
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let req = RegisterReq(
                phone: phone,
                first_name: first,
                last_name: last,
                tc_no: tcDigits,
                password: password,
                address_title: addrTitle,
                city: city,
                district: district,
                neighborhood: neighborhood,
                address_line: addressLine,
                postal_code: postal
            )
            _ = try await auth.register(req)
            return true
        } catch {
            errorText = error.localizedDescription
            return false
        }
    }

    func logout() {
        tokenStore.clear()
    }
}
