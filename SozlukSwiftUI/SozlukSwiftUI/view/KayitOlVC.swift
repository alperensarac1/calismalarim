//
//  KayitOlVC.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI
struct KayitOlView: View {
    @Environment(\.dismiss) private var dismiss

    @State private var username = ""
    @State private var email = ""
    @State private var password = ""

    @State private var showAlert = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""

    @StateObject private var vm = KayitViewModel()

    var body: some View {
        VStack(spacing: 16) {
            Text("Kayıt Ol").font(.largeTitle).bold()

            TextField("Kullanıcı adı", text: $username)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .textFieldStyle(.roundedBorder)

            TextField("E‑posta", text: $email)
                .keyboardType(.emailAddress)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .textFieldStyle(.roundedBorder)

            SecureField("Şifre", text: $password)
                .textFieldStyle(.roundedBorder)

            Button {
                vm.register(username: username, password: password, email: email)
            } label: {
                if vm.isLoading { ProgressView() } else { Text("Kayıt Ol").bold() }
            }
            .buttonStyle(.borderedProminent)
            .disabled(vm.isLoading)

            Button("Girişe Dön") { dismiss() }
                .buttonStyle(.bordered)
        }
        .padding()
        .onChange(of: vm.registerSucceeded) { new in
            if new {
                alertTitle = "Başarılı"
                alertMessage = "Kayıt tamamlandı"
                showAlert = true
            }
        }
        .onChange(of: vm.errorMessage) { new in
            if let msg = new { alertTitle = "Hata"; alertMessage = msg; showAlert = true }
        }
        .alert(alertTitle, isPresented: $showAlert) {
            Button("Tamam") {
                if vm.registerSucceeded { dismiss() }
            }
        } message: {
            Text(alertMessage)
        }
        .navigationTitle("Kayıt")
    }
}
