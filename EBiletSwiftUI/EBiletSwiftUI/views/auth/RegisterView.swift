//
//  RegisterView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    RegisterView

    SwiftUI kayıt ekranıdır.

    Görevleri:
    - Ad soyad almak
    - E-posta almak
    - Telefon almak
    - Şifre almak
    - auth/register.php API'sine istek atmak
    - Başarılı olursa AppState.login(user:) çağırmak
*/
struct RegisterView: View {

    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var fullName: String = ""
    @State private var email: String = ""
    @State private var phone: String = ""
    @State private var password: String = ""

    @State private var isLoading: Bool = false
    @State private var errorMessage: String = ""
    @State private var showAlert: Bool = false

    var body: some View {
        ZStack {
            Color(red: 245 / 255, green: 246 / 255, blue: 250 / 255)
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 22) {
                    Spacer(minLength: 20)

                    headerView

                    formCard

                    Spacer(minLength: 40)
                }
                .padding(24)
            }
        }
        .navigationTitle("Kayıt Ol")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Uyarı", isPresented: $showAlert) {
            Button("Tamam", role: .cancel) {}
        } message: {
            Text(errorMessage)
        }
    }

    private var headerView: some View {
        VStack(spacing: 8) {
            Text("Yeni Hesap Oluştur")
                .font(.largeTitle)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))
                .multilineTextAlignment(.center)

            Text("Etkinlik biletlerini kolayca satın almak için kayıt ol.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
    }

    private var formCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Kayıt Bilgileri")
                .font(.title2)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

            AppTextField(
                title: "Ad Soyad",
                text: $fullName
            )

            AppTextField(
                title: "E-posta",
                text: $email,
                keyboardType: .emailAddress
            )

            AppTextField(
                title: "Telefon",
                text: $phone,
                keyboardType: .phonePad
            )

            AppTextField(
                title: "Şifre",
                text: $password,
                isSecure: true
            )

            AppButton(
                title: "Kayıt Ol",
                backgroundColor: .green,
                isLoading: isLoading
            ) {
                register()
            }
            .padding(.top, 6)

            Button {
                dismiss()
            } label: {
                Text("Zaten hesabın var mı? Giriş yap")
                    .font(.subheadline)
                    .frame(maxWidth: .infinity)
            }
            .disabled(isLoading)
            .padding(.top, 4)
        }
        .padding(20)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 22))
        .shadow(color: .black.opacity(0.08), radius: 10, x: 0, y: 4)
    }

    private func register() {
        let cleanFullName = fullName.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanPhone = phone.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanPassword = password.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !cleanFullName.isEmpty else {
            showError("Ad soyad zorunludur")
            return
        }

        guard cleanFullName.count >= 3 else {
            showError("Ad soyad en az 3 karakter olmalıdır")
            return
        }

        guard !cleanEmail.isEmpty else {
            showError("E-posta zorunludur")
            return
        }

        guard cleanEmail.isValidEmail else {
            showError("Geçerli bir e-posta giriniz")
            return
        }

        if !cleanPhone.isEmpty && cleanPhone.count < 10 {
            showError("Telefon numarası eksik görünüyor")
            return
        }

        guard !cleanPassword.isEmpty else {
            showError("Şifre zorunludur")
            return
        }

        guard cleanPassword.count >= 6 else {
            showError("Şifre en az 6 karakter olmalıdır")
            return
        }

        isLoading = true

        Task {
            do {
                let response = try await APIService.shared.register(
                    fullName: cleanFullName,
                    email: cleanEmail,
                    phone: cleanPhone,
                    password: cleanPassword
                )

                isLoading = false

                guard response.success else {
                    showError(response.message)
                    return
                }

                guard let user = response.data else {
                    showError("Kullanıcı bilgisi alınamadı")
                    return
                }

                /*
                    Kayıt başarılı olunca direkt giriş yapmış kabul ediyoruz.
                    RootView otomatik Home tarafına geçer.
                */
                appState.login(user: user)

            } catch {
                isLoading = false
                showError(error.localizedDescription)
            }
        }
    }

    private func showError(_ message: String) {
        errorMessage = message
        showAlert = true
    }
}
