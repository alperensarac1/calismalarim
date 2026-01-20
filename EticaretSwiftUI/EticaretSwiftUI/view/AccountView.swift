//
//  AccountView.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI

struct AccountView: View {
    @EnvironmentObject private var auth: AuthManager
    @State private var showLogin = false

    var body: some View {
        VStack(spacing: 12) {
            if auth.isLoggedIn {
                Text("Giriş yapıldı ✅")
                Button("Çıkış Yap") { auth.logout() }
                    .buttonStyle(.borderedProminent)
            } else {
                Text("Giriş yapılmadı")
                Button("Giriş Yap") { showLogin = true }
                    .buttonStyle(.borderedProminent)
            }
        }
        .navigationTitle("Hesap")
        .sheet(isPresented: $showLogin) { NavigationStack { LoginView() } }
    }
}
