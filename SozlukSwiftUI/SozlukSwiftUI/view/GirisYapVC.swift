//
//  GirisYapVC.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI
struct GirisYapView: View {
    @State private var username = ""
    @State private var password = ""

    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var goToGundem = false

    @StateObject private var vm = GirisViewModel()

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Text("Giriş Yap").font(.largeTitle).bold()

                TextField("Kullanıcı adı", text: $username)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .textFieldStyle(.roundedBorder)

                SecureField("Şifre", text: $password)
                    .textFieldStyle(.roundedBorder)

                Button {
                    vm.login(username: username, password: password)
                } label: {
                    if vm.isLoading { ProgressView() } else { Text("Giriş Yap").bold() }
                }
                .buttonStyle(.borderedProminent)
                .disabled(vm.isLoading)

                NavigationLink("Kayıt Ol", destination: KayitOlView())
                    .buttonStyle(.bordered)

                // Storyboard'daki performSegue("toGundem") karşılığı
                NavigationLink(destination: GundemView(), isActive: $goToGundem) { }
            }
            .padding()
            .onAppear {
                if SessionManager.shared.isLoggedIn() {
                    goToGundem = true
                }
            }
            .onChange(of: vm.loginSucceeded) { new in
                if new { goToGundem = true }
            }
            .onChange(of: vm.errorMessage) { new in
                if let msg = new { alertMessage = msg; showAlert = true }
            }
            .alert("Hata", isPresented: $showAlert, actions: { Button("Tamam", role: .cancel) {} }, message: { Text(alertMessage) })
            .navigationTitle("Giriş")
        }
    }
}
