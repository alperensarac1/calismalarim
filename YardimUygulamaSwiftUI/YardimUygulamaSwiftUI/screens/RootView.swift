//
//  RootView.swift
//  YardimUygulamaSwiftUI
//
//  Created by Alperen Saraç on 28.02.2026.
//

import Foundation
import SwiftUI

enum AppRoute {
    case login
    case patient
    case helper
}

struct RootView: View {
    @State private var route: AppRoute = .login

    var body: some View {
        Group {
            if Session.isLoggedIn() {
                if Session.role() == .YARDIMCI { HelperTabsView(onLogout: { route = .login }) }
                else { PatientView(onLogout: { route = .login }) }
            } else {
                LoginView(onAuthed: { role in
                    route = (role == .YARDIMCI) ? .helper : .patient
                })
            }
        }
    }
}
