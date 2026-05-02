//
//  OnlineTaksiSwiftUIApp.swift
//  OnlineTaksiSwiftUI
//
//  Created by Alperen Saraç on 23.04.2026.
//

import SwiftUI

@main
struct OnlineTaksiSwiftUIApp: App {
      @StateObject private var sessionManager = SessionManager()
      @StateObject private var router = AppRouter()

      var body: some Scene {
          WindowGroup {
              RootView()
                  .environmentObject(sessionManager)
                  .environmentObject(router)
          }
      }
}
