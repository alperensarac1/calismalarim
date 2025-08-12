//
//  EntryRow.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI

struct EntryRowView: View {
    let entry: Entry

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(entry.title)
                .font(.headline)
                .lineLimit(2)
            Text(entry.content)
                .font(.body)
                .fixedSize(horizontal: false, vertical: true)
            HStack(spacing: 8) {
                if let u = entry.username, !u.isEmpty {
                    Text(u)
                }
                Spacer()
                Text(entry.created_at.asTrDate)
            }
            .font(.caption)
            .foregroundStyle(.secondary)
        }
        .padding(.vertical, 8)
    }
}
