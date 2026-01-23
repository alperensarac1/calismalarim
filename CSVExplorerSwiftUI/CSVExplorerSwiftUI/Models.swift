//
//  Models.swift
//  CSVExplorerSwiftUI
//
//  Created by Alperen Saraç on 22.01.2026.
//

import Foundation

struct CsvRow: Identifiable, Hashable {
    let id: UUID = UUID()
    let externalId: String?
    let json: String
    let dict: [String: String]
}

struct FieldItem: Identifiable, Hashable {
    let id = UUID()
    let key: String
    let value: String
}
