//
//  HomeTabsView.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI
struct HomeTabsView: View {
    var body: some View {
        TabView {
            GundemView()
                .tabItem { Label("Gündem", systemImage: "flame") }
            BugunView()
                .tabItem { Label("Bugün", systemImage: "calendar") }
            ProfilView()
                .tabItem { Label("Profil", systemImage: "person") }
        }
    }
}
