//
//  CanliQuizSwiftUIApp.swift
//  CanliQuizSwiftUI
//
//  Created by Alperen Saraç on 24.05.2026.
//

import SwiftUI

@main
struct CanliQuizSwiftUIApp: App {
    @StateObject private var viewModel = QuizViewModel()

        var body: some Scene {
            WindowGroup {
                ContentView()
                    .environmentObject(viewModel)
            }
        }
}
