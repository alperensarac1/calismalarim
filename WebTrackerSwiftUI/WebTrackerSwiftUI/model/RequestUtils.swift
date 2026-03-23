//
//  RequestUtils.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation

enum RequestUtils {

    static func shouldIgnoreUrl(_ url: String?) -> Bool {
        guard let url else { return true }
        let lower = url.lowercased()

        let ignoredExtensions = [
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg",
            ".css", ".js", ".map",
            ".woff", ".woff2", ".ttf", ".otf",
            ".ico", ".mp4", ".webm", ".mp3", ".aac", ".m4a"
        ]

        return ignoredExtensions.contains { lower.contains($0) }
    }

    static func looksLikeApi(_ url: String?) -> Bool {
        guard let url else { return false }
        let lower = url.lowercased()

        let apiKeywords = [
            "/api/",
            "graphql",
            ".json",
            "ajax",
            "rest",
            "v1/",
            "v2/",
            "endpoint"
        ]

        return apiKeywords.contains { lower.contains($0) }
    }

    static func guessResourceType(_ url: String?) -> String {
        guard let url else { return "unknown" }
        let lower = url.lowercased()

        switch true {
        case lower.contains(".png"), lower.contains(".jpg"), lower.contains(".jpeg"),
             lower.contains(".webp"), lower.contains(".svg"):
            return "image"
        case lower.contains(".css"):
            return "css"
        case lower.contains(".js"):
            return "js"
        case lower.contains(".mp4"), lower.contains(".webm"):
            return "video"
        case lower.contains(".json"), lower.contains("/api/"), lower.contains("graphql"):
            return "api"
        default:
            return "other"
        }
    }

    static func splitUrlAndQuery(_ fullUrl: String) -> (String, String) {
        guard let index = fullUrl.firstIndex(of: "?") else {
            return (fullUrl, "")
        }

        let base = String(fullUrl[..<index])
        let queryStart = fullUrl.index(after: index)
        let query = String(fullUrl[queryStart...])
        return (base, query)
    }

    static func buildFinalUrl(baseUrl: String, query: String) -> String {
        if query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return baseUrl.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let normalizedBase = baseUrl.trimmingCharacters(in: .whitespacesAndNewlines)

        let encodedQuery = query
            .split(separator: "&", omittingEmptySubsequences: false)
            .map { part -> String in
                guard let eqIndex = part.firstIndex(of: "=") else {
                    return String(part).addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? String(part)
                }

                let key = String(part[..<eqIndex])
                let valueStart = part.index(after: eqIndex)
                let value = String(part[valueStart...])
                let encodedValue = value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? value
                return "\(key)=\(encodedValue)"
            }
            .joined(separator: "&")

        if normalizedBase.contains("?") {
            return "\(normalizedBase)&\(encodedQuery)"
        } else {
            return "\(normalizedBase)?\(encodedQuery)"
        }
    }

    static func detectContentType(for body: String) -> String {
        let trimmed = body.trimmingCharacters(in: .whitespacesAndNewlines)

        if trimmed.hasPrefix("{") || trimmed.hasPrefix("[") {
            return "application/json; charset=utf-8"
        }

        if trimmed.contains("=") {
            return "application/x-www-form-urlencoded; charset=utf-8"
        }

        return "text/plain; charset=utf-8"
    }

    static func currentTimeString() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm:ss"
        return formatter.string(from: Date())
    }
}
