//
//  RootView.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation
import SwiftUI

struct RootView: View {
    @EnvironmentObject var sessionManager: SessionManager
    @EnvironmentObject var router: AppRouter

    var body: some View {
        switch router.route {
        case .splash:
            SplashView()
        case .login:
            LoginView()
        case .register:
            RegisterView()
        case .customerHome:
            CustomerHomeView()
        case .driverHome:
            DriverHomeView()
        }
    }
}
