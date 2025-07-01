//
//  AdisyonUygulamaSwiftUIApp.swift
//  AdisyonUygulamaSwiftUI
//
//  Created by Alperen Saraç on 9.06.2025.
//
import SwiftUI

@main
struct AdisyonUygulamaSwiftUIApp: App {
    @StateObject private var masalarVM = MasalarViewModel()
    @StateObject private var urunVM = UrunViewModel()
    // detayVM will be re-created per selected masa in NavigationStack,
    // but we need a placeholder here for the root view:
    @StateObject private var detayVM = MasaDetayViewModel(masaId: 0)

    var body: some Scene {
        WindowGroup {
            MainScreen(
                masalarVM: masalarVM,
                urunVM: urunVM,
                detayVM: detayVM
            )
        }
    }
}
