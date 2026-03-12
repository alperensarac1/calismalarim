//
//  HelperTabsView.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation
import SwiftUI

struct HelperTabsView: View {
    @StateObject private var vm = HelperVM()
    var onLogout: () -> Void

    private var helperId: Int { Session.userId() }

    var body: some View {
        TabView {
            HelperOpenView(vm: vm, helperId: helperId)
                .tabItem { Label("Açık", systemImage: "list.bullet") }

            HelperAcceptedView(vm: vm, helperId: helperId)
                .tabItem { Label("Kabul", systemImage: "checkmark.circle") }

            HelperHistoryView(vm: vm, helperId: helperId)
                .tabItem { Label("Geçmiş", systemImage: "clock") }
        }
        .toolbar {
            ToolbarItem {
                Button("Çıkış") {
                    Session.clear()
                    onLogout()
                }
            }
        }
    }
}
