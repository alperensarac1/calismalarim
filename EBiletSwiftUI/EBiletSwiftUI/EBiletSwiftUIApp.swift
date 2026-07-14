//
//  EBiletSwiftUIApp.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import SwiftUI

@main
struct EBiletSwiftUIApp: App {
    @StateObject private var appState = AppState()


       var body: some Scene {
           WindowGroup {
               RootView()
                   .environmentObject(appState)
           }
       }
}
struct RootPlaceholderView: View {

    @EnvironmentObject private var appState: AppState

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Text("Etkinlik Bileti SwiftUI")
                    .font(.largeTitle)
                    .bold()

                Text(appState.isLoggedIn ? "Giriş yapılmış" : "Giriş yapılmamış")
                    .foregroundStyle(.secondary)

                Text("Login ve Register ekranları sonraki adımda eklenecek.")
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)
                    .padding()
            }
            .padding()
        }
    }
}
