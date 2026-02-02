//
//  LoginView.swift
//  KargoPaylasimSwiftUI
//
//  Created by Alperen Saraç on 30.01.2026.
//

import Foundation
import SwiftUI

struct LoginView: View {
    @StateObject var vm: AuthVM

    @State private var phone = ""
    @State private var password = ""
    @State private var showRegister = false

    var body: some View {
        NavigationStack {
            Form {
                Section("Giriş") {
                    TextField("Telefon (05xx...)", text: $phone)
                        .keyboardType(.phonePad)

                    SecureField("Şifre", text: $password)

                    Button("Giriş Yap") {
                        Task {
                            _ = await vm.doLogin(phoneRaw: phone, password: password)
                        }
                    }
                    .disabled(vm.isLoading)
                }

                if let err = vm.errorText {
                    Section { Text(err).foregroundStyle(.red) }
                }
            }
            .overlay { if vm.isLoading { ProgressView() } }
            .navigationTitle("Giriş")
            .toolbar {
                ToolbarItem {
                    Button("Kayıt Ol") { showRegister = true }
                }
            }
            .sheet(isPresented: $showRegister) {
                RegisterView(vm: vm)
            }
        }
    }
}
