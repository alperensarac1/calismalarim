//
//  ChatSwiftUIApp.swift
//  ChatSwiftUI
//
//  Created by Alperen Saraç on 8.07.2025.
//

import SwiftUI

@main
struct ChatSwiftUIApp: App {
    var body: some Scene {
        WindowGroup {
            ChatAppContent(pref: PrefManager())
        }
    }
}
