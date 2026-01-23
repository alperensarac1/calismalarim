//
//  CSVParser.swift
//  CSVExplorerSwiftUI
//
//  Created by Alperen Saraç on 22.01.2026.
//

import Foundation


enum CsvParser {

    static func parse(text: String) -> (headers: [String], rows: [CsvRow]) {
        let lines = text
            .replacingOccurrences(of: "\r\n", with: "\n")
            .replacingOccurrences(of: "\r", with: "\n")
            .split(separator: "\n", omittingEmptySubsequences: true)
            .map { String($0) }

        guard let headerLine = lines.first else { return ([], []) }

        let headers = splitCsvLine(headerLine)
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }

        var out: [CsvRow] = []
        if headers.isEmpty { return ([], []) }

        for i in 1..<lines.count {
            let values = splitCsvLine(lines[i])

            var dict: [String: String] = [:]
            for (idx, key) in headers.enumerated() {
                if idx < values.count {
                    let v = values[idx].trimmingCharacters(in: .whitespacesAndNewlines)
                    if !v.isEmpty { dict[key] = v }
                }
            }

            let externalId = guessExternalId(headers: headers, dict: dict)

            let jsonData = (try? JSONSerialization.data(withJSONObject: dict, options: [])) ?? Data()
            let json = String(data: jsonData, encoding: .utf8) ?? "{}"

            out.append(CsvRow(externalId: externalId, json: json, dict: dict))
        }

        return (headers, out)
    }

    private static func guessExternalId(headers: [String], dict: [String: String]) -> String? {
        let candidates = ["id", "ID", "Id", "user_id", "uid", "pk"]
        for c in candidates {
            if headers.contains(c), let v = dict[c], !v.isEmpty { return v }
        }
        return nil
    }

    // Basit CSV splitter: quote, escaped quote ("") destekli
    private static func splitCsvLine(_ line: String) -> [String] {
        var res: [String] = []
        var cur = ""
        var inQuotes = false
        let chars = Array(line)
        var i = 0

        while i < chars.count {
            let ch = chars[i]
            if ch == "\"" {
                if inQuotes, i + 1 < chars.count, chars[i + 1] == "\"" {
                    cur.append("\"")
                    i += 1
                } else {
                    inQuotes.toggle()
                }
            } else if ch == ",", !inQuotes {
                res.append(cur)
                cur = ""
            } else {
                cur.append(ch)
            }
            i += 1
        }
        res.append(cur)
        return res
    }
}
