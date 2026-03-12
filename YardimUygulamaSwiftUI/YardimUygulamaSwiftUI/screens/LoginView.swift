//
//  LoginView.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation
import SwiftUI

struct LoginView: View {
    @StateObject private var vm = AuthVM()
    var onAuthed: (Role) -> Void

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                Text("Giriş").font(.title2).bold()

                TextField("Telefon", text: $vm.phone)
                    .textFieldStyle(.roundedBorder)

                SecureField("Şifre", text: $vm.pass)
                    .textFieldStyle(.roundedBorder)

                if vm.loading { ProgressView() }

                Button("Giriş Yap") {
                    Task {
                        if let role = await vm.login() {
                            onAuthed(role)
                        }
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(vm.loading)

                if !vm.error.isEmpty {
                    Text(vm.error).foregroundColor(.red)
                }

                NavigationLink("Kayıt Ol", destination: RegisterView(onAuthed: onAuthed))
                    .padding(.top, 10)

                Spacer()
            }
            .padding()
        }
    }
}
