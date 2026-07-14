//
//  LoginView.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    LoginView

    SwiftUI giriş ekranıdır.

    Görevleri:
    - E-posta almak
    - Şifre almak
    - Form kontrolü yapmak
    - auth/login.php API'sine istek atmak
    - Başarılı olursa AppState.login(user:) çağırmak
*/
struct LoginView: View {

    @EnvironmentObject private var appState: AppState

    @State private var email: String = ""
    @State private var password: String = ""

    @State private var isLoading: Bool = false
    @State private var errorMessage: String = ""
    @State private var showAlert: Bool = false

    @State private var showRegister: Bool = false

    var body: some View {
        NavigationStack {
            ZStack {
                Color(red: 245 / 255, green: 246 / 255, blue: 250 / 255)
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 22) {
                        Spacer(minLength: 40)

                        headerView

                        formCard

                        Spacer(minLength: 40)
                    }
                    .padding(24)
                }
            }
            .navigationBarHidden(true)
            .navigationDestination(isPresented: $showRegister) {
                RegisterView()
            }
            .alert("Uyarı", isPresented: $showAlert) {
                Button("Tamam", role: .cancel) {}
            } message: {
                Text(errorMessage)
            }
        }
    }

    /*
        Başlık alanı.
    */
    private var headerView: some View {
        VStack(spacing: 8) {
            Text("Etkinlik Bileti")
                .font(.largeTitle)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

            Text("Etkinlikleri keşfet, biletini QR kodla kullan.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
    }

    /*
        Form kartı.
    */
    private var formCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Giriş Yap")
                .font(.title2)
                .bold()
                .foregroundStyle(Color(red: 15 / 255, green: 23 / 255, blue: 42 / 255))

            AppTextField(
                title: "E-posta",
                text: $email,
                keyboardType: .emailAddress
            )

            AppTextField(
                title: "Şifre",
                text: $password,
                isSecure: true
            )

            AppButton(
                title: "Giriş Yap",
                backgroundColor: .blue,
                isLoading: isLoading
            ) {
                login()
            }
            .padding(.top, 6)

            Button {
                showRegister = true
            } label: {
                Text("Hesabın yok mu? Kayıt ol")
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

    /*
        Login işlemi.

        Task:
        SwiftUI içinde async fonksiyon çağırmak için kullanılır.

        @MainActor:
        UI state güncellemeleri ana thread üzerinde yapılmalı.
        Task içinde await sonrası bu zaten çoğu durumda korunur;
        yine de UI state yazarken dikkatli oluyoruz.
    */
    private func login() {
        let cleanEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanPassword = password.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !cleanEmail.isEmpty else {
            showError("E-posta zorunludur")
            return
        }

        guard cleanEmail.isValidEmail else {
            showError("Geçerli bir e-posta giriniz")
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
                let response = try await APIService.shared.login(
                    email: cleanEmail,
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
                    RootView otomatik olarak Home tarafına geçer.
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
