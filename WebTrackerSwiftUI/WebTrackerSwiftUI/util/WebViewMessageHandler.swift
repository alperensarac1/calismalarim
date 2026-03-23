//
//  WebViewMessageHandler.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation
import WebKit

final class WebViewMessageHandler: NSObject, WKScriptMessageHandler {

    var onJsonReceived: ((String) -> Void)?

    init(onJsonReceived: ((String) -> Void)? = nil) {
        self.onJsonReceived = onJsonReceived
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard message.name == "iosLogger" else { return }

        if let body = message.body as? String {
            onJsonReceived?(body)
        }
    }
}
