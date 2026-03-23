//
//  WebViewStore.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation
import WebKit

@MainActor
final class WebViewStore: ObservableObject {
    weak var webView: WKWebView?
}
