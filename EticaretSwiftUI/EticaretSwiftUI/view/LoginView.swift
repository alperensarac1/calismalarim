//
//  LoginView.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI
struct LoginView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var auth: AuthManager
    @StateObject private var vm = LoginVM()

    var body: some View {
        Form {
            Section("Giriş") {
                TextField("E-posta", text: $vm.email).keyboardType(.emailAddress).textInputAutocapitalization(.never)
                SecureField("Şifre", text: $vm.password)
                Button(vm.isLoading ? "..." : "Giriş Yap") {
                    Task {
                        if let res = await vm.login() {
                            auth.setSession(token: res.token, userId: res.user_id)
                            dismiss()
                        }
                    }
                }
                .disabled(vm.isLoading)
            }

            if let err = vm.errorMessage {
                Section { Text(err).foregroundStyle(.red) }
            }
        }
        .navigationTitle("Giriş")
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) { Button("Kapat") { dismiss() } }
        }
    }
}
