//
//  SplashView.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation
import SwiftUI

struct SplashView: View {
    @EnvironmentObject var sessionManager: SessionManager
    @EnvironmentObject var router: AppRouter

    var body: some View {
        VStack {
            Spacer()
            Text("onlinetaksi")
                .font(.largeTitle)
                .bold()
            Spacer()
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
                if sessionManager.isLoggedIn {
                    if sessionManager.role == "driver" {
                        router.route = .driverHome
                    } else {
                        router.route = .customerHome
                    }
                } else {
                    router.route = .login
                }
            }
        }
    }
}
