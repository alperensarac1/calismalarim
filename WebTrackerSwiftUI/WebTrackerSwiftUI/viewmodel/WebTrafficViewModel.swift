//
//  WebTrafficViewModel.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//
import Foundation
import SwiftUI

@MainActor
final class WebTrafficViewModel: ObservableObject {

    @Published var urlText: String = "https://example.com"
    @Published var filterOptions: FilterOptions = FilterOptions()
    @Published var visibleLogs: [NetworkLog] = []

    private var allLogs: [NetworkLog] = []
    private var seenRequests: Set<String> = []

    func clearLogs() {
        allLogs.removeAll()
        seenRequests.removeAll()
        refreshVisibleLogs()
    }

    func addLogIfNeeded(_ log: NetworkLog) {
        let key = "\(log.source)_\(log.method)_\(log.url)_\(log.requestBody ?? "")_\(log.time)"
        guard !seenRequests.contains(key) else { return }

        seenRequests.insert(key)
        allLogs.insert(log, at: 0)
        refreshVisibleLogs()
    }

    func parseAndAddJsLog(_ jsonString: String) {
        guard
            let data = jsonString.data(using: .utf8),
            let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return
        }

        let url = json["url"] as? String ?? ""
        let method = json["method"] as? String ?? "GET"
        let body = json["body"] as? String
        let source = json["source"] as? String ?? "JS_HOOK"
        let host = URL(string: url)?.host ?? "Bilinmiyor"

        let log = NetworkLog(
            method: method,
            url: url,
            host: host,
            time: RequestUtils.currentTimeString(),
            headers: [:],
            isMainFrame: false,
            resourceType: "api",
            requestBody: body,
            source: source
        )

        addLogIfNeeded(log)
    }

    func updateEnableFilter(_ value: Bool) {
        filterOptions.enableFilter = value
        refreshVisibleLogs()
    }

    func updateOnlyApi(_ value: Bool) {
        filterOptions.onlyApiRequests = value
        refreshVisibleLogs()
    }

    func updateEnableJsHook(_ value: Bool) {
        filterOptions.enableJsHook = value
    }

    func updateOnlyGet(_ value: Bool) {
        filterOptions.showOnlyGet = value
        if value { filterOptions.showOnlyPost = false }
        refreshVisibleLogs()
    }

    func updateOnlyPost(_ value: Bool) {
        filterOptions.showOnlyPost = value
        if value { filterOptions.showOnlyGet = false }
        refreshVisibleLogs()
    }

    func updateSearchQuery(_ value: String) {
        filterOptions.searchQuery = value
        refreshVisibleLogs()
    }

    func getAllLogsText() -> String {
        if allLogs.isEmpty {
            return "Henüz kopyalanacak istek yok."
        }

        var result = "TOPLAM ISTEK SAYISI: \(allLogs.count)\n\n"
        for (index, log) in allLogs.enumerated() {
            result += "ISTEK #\(index + 1)\n"
            result += formatSingleLog(log)
            result += "\n\n"
        }
        return result
    }

    func formatSingleLog(_ log: NetworkLog) -> String {
        var text = ""
        text += "========================================\n"
        text += "METHOD      : \(log.method)\n"
        text += "SOURCE      : \(log.source)\n"
        text += "TYPE        : \(log.resourceType)\n"
        text += "TIME        : \(log.time)\n"
        text += "HOST        : \(log.host)\n"
        text += "MAIN_FRAME  : \(log.isMainFrame)\n"
        text += "URL         : \(log.url)\n"
        text += "HEADERS     :\n"

        if log.headers.isEmpty {
            text += "  - yok\n"
        } else {
            for (key, value) in log.headers {
                text += "  \(key): \(value)\n"
            }
        }

        text += "BODY        :\n"
        text += log.requestBody ?? "yok"
        return text
    }

    private func refreshVisibleLogs() {
        let filters = filterOptions

        visibleLogs = allLogs.filter { log in
            let searchOk: Bool
            if filters.searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                searchOk = true
            } else {
                searchOk = log.url.localizedCaseInsensitiveContains(filters.searchQuery)
            }

            if !filters.enableFilter {
                return searchOk
            }

            let methodOk: Bool
            if filters.showOnlyGet {
                methodOk = log.method.caseInsensitiveCompare("GET") == .orderedSame
            } else if filters.showOnlyPost {
                methodOk = log.method.caseInsensitiveCompare("POST") == .orderedSame
            } else {
                methodOk =
                    log.method.caseInsensitiveCompare("GET") == .orderedSame ||
                    log.method.caseInsensitiveCompare("POST") == .orderedSame
            }

            let ignoredOk = !RequestUtils.shouldIgnoreUrl(log.url)

            let apiOk: Bool
            if filters.onlyApiRequests {
                apiOk =
                    RequestUtils.looksLikeApi(log.url) ||
                    log.resourceType.caseInsensitiveCompare("api") == .orderedSame ||
                    log.source.caseInsensitiveCompare("JS_HOOK") == .orderedSame
            } else {
                apiOk = true
            }

            return searchOk && methodOk && ignoredOk && apiOk
        }
    }
}
