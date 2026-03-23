//
//  ContentView.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation
import SwiftUI
import WebKit

struct ContentView: View {
    @StateObject private var viewModel = WebTrafficViewModel()
    @StateObject private var webViewStore = WebViewStore()

    @State private var actionLog: NetworkLog?
    @State private var detailLog: NetworkLog?
    @State private var replayLog: NetworkLog?
    @State private var replayResult: String?

    var body: some View {
        VStack(spacing: 10) {
            TextField("URL", text: $viewModel.urlText)
                .textFieldStyle(.roundedBorder)

            HStack {
                Button("Aç") {
                    let current = viewModel.urlText
                    let normalized = current.hasPrefix("http://") || current.hasPrefix("https://")
                        ? current
                        : "https://\(current)"

                    viewModel.urlText = normalized
                    viewModel.clearLogs()

                    if let url = URL(string: normalized) {
                        webViewStore.webView?.load(URLRequest(url: url))
                    }
                }

                Button("Kopyala") {
                    UIPasteboard.general.string = viewModel.getAllLogsText()
                }
            }

            VStack(alignment: .leading, spacing: 6) {
                Toggle("Filtre aktif", isOn: Binding(
                    get: { viewModel.filterOptions.enableFilter },
                    set: { viewModel.updateEnableFilter($0) }
                ))

                Toggle("Sadece API benzeri istekler", isOn: Binding(
                    get: { viewModel.filterOptions.onlyApiRequests },
                    set: { viewModel.updateOnlyApi($0) }
                ))

                Toggle("JS Hook aktif", isOn: Binding(
                    get: { viewModel.filterOptions.enableJsHook },
                    set: { viewModel.updateEnableJsHook($0) }
                ))

                Toggle("Sadece GET", isOn: Binding(
                    get: { viewModel.filterOptions.showOnlyGet },
                    set: { viewModel.updateOnlyGet($0) }
                ))

                Toggle("Sadece POST", isOn: Binding(
                    get: { viewModel.filterOptions.showOnlyPost },
                    set: { viewModel.updateOnlyPost($0) }
                ))

                TextField(
                    "URL içinde ara",
                    text: Binding(
                        get: { viewModel.filterOptions.searchQuery },
                        set: { viewModel.updateSearchQuery($0) }
                    )
                )
                .textFieldStyle(.roundedBorder)
            }

            TrackedWebView(
                urlString: viewModel.urlText,
                onJsMessage: { json in
                    if viewModel.filterOptions.enableJsHook {
                        viewModel.parseAndAddJsLog(json)
                    }
                },
                onNavigationRequestCaptured: { log in
                    viewModel.addLogIfNeeded(log)
                },
                onWebViewCreated: { webView in
                    webViewStore.webView = webView
                }
            )
            .frame(height: 260)

            Text("Yakalanan İstekler")
                .font(.headline)

            List(viewModel.visibleLogs) { log in
                VStack(alignment: .leading, spacing: 4) {
                    Text("Method: \(log.method) | Kaynak: \(log.source)")
                    Text("Tip: \(log.resourceType)")
                    Text("URL: \(log.url)")
                    Text("Host: \(log.host)")
                    Text("Zaman: \(log.time)")
                    Text("Body: \(log.requestBody ?? "yok")")
                }
                .contentShape(Rectangle())
                .onTapGesture {
                    actionLog = log
                }
            }
        }
        .padding()
        .sheet(item: $actionLog) { log in
            ActionSheetView(
                log: log,
                onDetail: {
                    actionLog = nil
                    detailLog = log
                },
                onReplay: {
                    actionLog = nil
                    replayLog = log
                },
                onCopy: {
                    UIPasteboard.general.string = viewModel.formatSingleLog(log)
                }
            )
            .presentationDetents([.medium])
        }
        .sheet(item: $detailLog) { log in
            DetailSheetView(
                log: log,
                text: viewModel.formatSingleLog(log)
            )
        }
        .sheet(item: $replayLog) { log in
            ReplaySheetView(
                log: log,
                onReplay: { baseUrl, params, openInWebView in
                    if openInWebView {
                        openReplayInWebView(
                            originalLog: log,
                            editedBaseUrl: baseUrl,
                            editedParams: params
                        )
                    }

                    Task {
                        let result = await ReplayService.replay(
                            originalLog: log,
                            editedBaseUrl: baseUrl,
                            editedParams: params
                        )
                        replayResult = result
                    }
                }
            )
        }
        .sheet(item: Binding(
            get: {
                replayResult == nil ? nil : ReplayResultWrapper(text: replayResult!)
            },
            set: { newValue in
                replayResult = newValue?.text
            }
        )) { wrapped in
            ReplayResultSheetView(text: wrapped.text)
        }
    }

    private func openReplayInWebView(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String
    ) {
        if originalLog.method.caseInsensitiveCompare("GET") == .orderedSame {
            let finalUrl = RequestUtils.buildFinalUrl(baseUrl: editedBaseUrl, query: editedParams)
            viewModel.urlText = finalUrl

            if let url = URL(string: finalUrl) {
                webViewStore.webView?.load(URLRequest(url: url))
            }
        } else if originalLog.method.caseInsensitiveCompare("POST") == .orderedSame {
            let contentType = RequestUtils.detectContentType(for: editedParams)

            if contentType.hasPrefix("application/x-www-form-urlencoded"),
               let url = URL(string: editedBaseUrl),
               let bodyData = editedParams.data(using: .utf8) {
                var request = URLRequest(url: url)
                request.httpMethod = "POST"
                request.httpBody = bodyData
                request.setValue(contentType, forHTTPHeaderField: "Content-Type")

                viewModel.urlText = editedBaseUrl
                webViewStore.webView?.load(request)
            }
        }
    }
}

private struct ReplayResultWrapper: Identifiable {
    let id = UUID()
    let text: String
}
