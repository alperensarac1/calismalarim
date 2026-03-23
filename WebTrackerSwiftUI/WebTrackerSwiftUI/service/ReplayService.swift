//
//  ReplayService.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation

enum ReplayService {
    static func replay(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String
    ) async -> String {
        if editedBaseUrl.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return "Base URL boş olamaz"
        }

        do {
            let request: URLRequest

            if originalLog.method.caseInsensitiveCompare("GET") == .orderedSame {
                let finalUrlString = RequestUtils.buildFinalUrl(baseUrl: editedBaseUrl, query: editedParams)
                guard let url = URL(string: finalUrlString) else {
                    return "Geçersiz URL"
                }

                var req = URLRequest(url: url)
                req.httpMethod = "GET"
                request = req
            } else {
                guard let url = URL(string: editedBaseUrl) else {
                    return "Geçersiz URL"
                }

                var req = URLRequest(url: url)
                req.httpMethod = "POST"
                let contentType = RequestUtils.detectContentType(for: editedParams)
                req.setValue(contentType, forHTTPHeaderField: "Content-Type")
                req.httpBody = editedParams.data(using: .utf8)
                request = req
            }

            let (data, response) = try await URLSession.shared.data(for: request)

            let httpResponse = response as? HTTPURLResponse
            let responseCode = httpResponse?.statusCode ?? -1
            let headerText = httpResponse?.allHeaderFields.map { "\($0.key): \($0.value)" }.joined(separator: "\n") ?? "Header yok"
            let bodyText = String(data: data, encoding: .utf8) ?? "Body okunamadı"

            return """
            HTTP CODE: \(responseCode)

            REQUEST:
            \(buildReplayRequestSummary(originalLog: originalLog, editedBaseUrl: editedBaseUrl, editedParams: editedParams))

            RESPONSE HEADERS:
            \(headerText)

            RESPONSE BODY:
            \(String(bodyText.prefix(5000)))
            """
        } catch {
            return error.localizedDescription
        }
    }

    private static func buildReplayRequestSummary(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String
    ) -> String {
        """
        ORIGINAL METHOD: \(originalLog.method)
        SOURCE         : \(originalLog.source)
        EDITED BASE URL: \(editedBaseUrl)
        EDITED PARAMS  :
        \(editedParams.isEmpty ? "yok" : editedParams)
        """
    }
}
