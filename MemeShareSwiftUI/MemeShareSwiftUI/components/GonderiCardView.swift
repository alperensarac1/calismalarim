import Foundation
import SwiftUI

struct GonderiCardView: View {
    let model: GonderiModel
    let baseURL: URL
    var onPlayTapped: (() -> Void)?

    var body: some View {
        VStack(spacing: 8) {
            ZStack {
                if model.mediaType == "image" {
                    RemoteImageView(url: baseURL.appendingPathComponent(model.mediaUrl))
                } else if model.mediaType == "video" {
                    RemoteVideoThumbnail(url: baseURL.appendingPathComponent(model.mediaUrl))

                    Button(action: { onPlayTapped?() }) {
                        Image(systemName: "play.circle.fill")
                            .font(.system(size: 44))
                            .symbolRenderingMode(.hierarchical)
                            .foregroundStyle(.white)
                            .shadow(radius: 6)
                    }
                } else {
                    Rectangle().fill(.gray.opacity(0.15))
                }
            }
            .frame(height: 180)
            .clipShape(RoundedRectangle(cornerRadius: 12))

            VStack(alignment: .leading, spacing: 2) {
                Text(model.uploadedAt).font(.caption).foregroundStyle(.secondary)
                Text("Kullanıcı #\(model.userId)").font(.subheadline).bold()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(8)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.06), radius: 6, y: 3)
    }
}
