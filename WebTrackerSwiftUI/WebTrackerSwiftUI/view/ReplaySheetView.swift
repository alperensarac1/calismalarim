//
//  ReplaySheetView.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation

import SwiftUI

struct ReplaySheetView: View {
    let log: NetworkLog
    let onReplay: (_ baseUrl: String, _ params: String, _ openInWebView: Bool) -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var baseUrl: String = ""
    @State private var params: String = ""
    @State private var openInWebView: Bool = true

    init(
        log: NetworkLog,
        onReplay: @escaping (_ baseUrl: String, _ params: String, _ openInWebView: Bool) -> Void
    ) {
        self.log = log
        self.onReplay = onReplay

        if log.method.caseInsensitiveCompare("GET") == .orderedSame {
            let split = RequestUtils.splitUrlAndQuery(log.url)
            _baseUrl = State(initialValue: split.0)
            _params = State(initialValue: split.1)
        } else {
            _baseUrl = State(initialValue: log.url)
            _params = State(initialValue: log.requestBody ?? "")
        }
    }

    var body: some View {
        NavigationView {
            Form {
                Section {
                    Text("Method: \(log.method)")
                }

                Section("Base URL") {
                    TextField("Base URL", text: $baseUrl)
                }

                Section("Query / Body") {
                    TextEditor(text: $params)
                        .frame(height: 180)
                }

                Section {
                    Toggle("Replay sonrası WebView'de aç", isOn: $openInWebView)
                }

                Section {
                    Button("Tekrar Dene") {
                        onReplay(baseUrl, params, openInWebView)
                        dismiss()
                    }
                }
            }
            .navigationTitle("Replay")
        }
    }
}
