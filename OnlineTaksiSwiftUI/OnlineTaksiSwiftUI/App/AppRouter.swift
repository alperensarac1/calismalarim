//
//  AppRouter.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import Foundation

enum AppRoute {
    case splash
    case login
    case register
    case customerHome
    case driverHome
}

final class AppRouter: ObservableObject {
    @Published var route: AppRoute = .splash
}
