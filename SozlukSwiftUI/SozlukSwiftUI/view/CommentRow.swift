//
//  CommentRow.swift
//  SozlukSwiftUI
//
//  Created by Alperen Saraç on 11.08.2025.
//

import Foundation
import SwiftUI

struct CommentRowView: View {
    let comment: Comment

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(comment.comment_text)
                .font(.body)
                .fixedSize(horizontal: false, vertical: true)
            HStack(spacing: 12) {
                Text(comment.username)
                Spacer()
                Text(comment.created_at.asTrDate)
                Text("👍\(comment.likes)")
                Text("👎\(comment.dislikes)")
            }
            .font(.caption)
            .foregroundStyle(.secondary)
        }
        .padding(.vertical, 8)
    }
}
