//
//  DetailsView.swift
//  CSVExplorerSwiftUI
//
//  Created by Alperen Saraç on 22.01.2026.
//

import Foundation
import SwiftUI

struct DetailsView: View {

    let rowJson: String
    let headers: [String]

    @State private var q: String = ""
    @State private var fields: [FieldItem] = []
    @State private var filtered: [FieldItem] = []
    @State private var info: String = ""

    var body: some View {
        VStack(spacing: 12) {
            TextField("Search in fields", text: $q)
                .textFieldStyle(.roundedBorder)
                .onChange(of: q) {new in
                    applyFilter(new)
                }

            HStack(spacing: 10) {
                Button("Copy JSON") {
                    UIPasteboard.general.string = rowJson
                    info = "Copied JSON"
                }
                .buttonStyle(.borderedProminent)

                Button("Copy CSV") {
                    UIPasteboard.general.string = buildCsv()
                    info = "Copied CSV row"
                }
                .buttonStyle(.bordered)
            }

            HStack {
                Text("\(filtered.count) fields")
                    .font(.headline)
                Spacer()
                if !info.isEmpty {
                    Text(info).foregroundStyle(.secondary)
                }
            }

            List(filtered) { it in
                VStack(alignment: .leading, spacing: 6) {
                    Text(it.key).font(.headline)
                    Text(it.value).font(.body)
                }
                .padding(.vertical, 6)
            }
            .listStyle(.plain)

            Spacer(minLength: 0)
        }
        .padding(14)
        .navigationTitle("Details")
        .onAppear {
            buildFields()
            applyFilter("")
        }
    }

    private func buildFields() {
        let obj = (try? JSONSerialization.jsonObject(with: Data(rowJson.utf8), options: [])) as? [String: Any] ?? [:]
        var out: [FieldItem] = []

        if !headers.isEmpty {
            for h in headers {
                let v = "\(obj[h] ?? "")"
                out.append(FieldItem(key: h, value: v.isEmpty ? "-" : v))
            }

            let extras = obj.keys.filter { !headers.contains($0) }.sorted()
            for k in extras {
                let v = "\(obj[k] ?? "")"
                out.append(FieldItem(key: k, value: v.isEmpty ? "-" : v))
            }
        } else {
            for k in obj.keys.sorted() {
                let v = "\(obj[k] ?? "")"
                out.append(FieldItem(key: k, value: v.isEmpty ? "-" : v))
            }
        }

        fields = out
        filtered = out
    }

    private func applyFilter(_ q0: String) {
        let qq = q0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if qq.isEmpty { filtered = fields; return }
        filtered = fields.filter {
            $0.key.lowercased().contains(qq) || $0.value.lowercased().contains(qq)
        }
    }

    private func buildCsv() -> String {
        guard !headers.isEmpty else { return rowJson }
        let obj = (try? JSONSerialization.jsonObject(with: Data(rowJson.utf8), options: [])) as? [String: Any] ?? [:]
        let headerLine = headers.joined(separator: ",")
        let rowLine = headers.map { esc("\(obj[$0] ?? "")") }.joined(separator: ",")
        return headerLine + "\n" + rowLine
    }

    private func esc(_ value0: String) -> String {
        var v = value0
        let needs = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")
        v = v.replacingOccurrences(of: "\"", with: "\"\"")
        if needs { v = "\"\(v)\"" }
        return v
    }
}
