//
//  RootView.swift
//  EticaretSwiftUI
//
//  Created by Alperen Saraç on 18.01.2026.
//

import Foundation
import SwiftUI

struct RootView: View {
    var body: some View {
        TabView {
            NavigationStack {
                HomeView()
            }
            .tabItem { Label("Ana Sayfa", systemImage: "house") }

            NavigationStack {
                CartView()
            }
            .tabItem { Label("Sepet", systemImage: "cart") }

            NavigationStack {
                AccountView()
            }
            .tabItem { Label("Hesap", systemImage: "person") }
        }
    }
}
