//
//  AppState.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI

/*
    AppState

    SwiftUI tarafında uygulamanın giriş durumunu yönetir.

    UIKit'te navigationController.setViewControllers yapıyorduk.
    SwiftUI'da root ekranı state'e göre değiştiririz.

    isLoggedIn true:
        HomeView gösterilir.

    isLoggedIn false:
        LoginView gösterilir.
*/
final class AppState: ObservableObject {

    @Published var isLoggedIn: Bool

    init() {
        self.isLoggedIn = SessionManager.shared.isLoggedIn
    }

    func login(user: User) {
        SessionManager.shared.saveUser(user)
        isLoggedIn = true
    }

    func logout() {
        SessionManager.shared.logout()
        isLoggedIn = false
    }

    func refreshLoginState() {
        isLoggedIn = SessionManager.shared.isLoggedIn
    }
}
