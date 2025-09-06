import Foundation
import AVFoundation
import SwiftUI

struct RemoteVideoThumbnail: View {
    let url: URL
    @State private var image: UIImage? = nil

    var body: some View {
        Group {
            if let image {
                Image(uiImage: image).resizable().scaledToFill()
            } else {
                Rectangle().fill(.gray.opacity(0.15)).overlay { ProgressView() }
            }
        }
        .onAppear(perform: generate)
        .clipped()
    }

    private func generate() {
        guard image == nil else { return }
        let asset = AVAsset(url: url)
        let generator = AVAssetImageGenerator(asset: asset)
        generator.appliesPreferredTrackTransform = true
        let time = CMTime(seconds: 1, preferredTimescale: 600)
        DispatchQueue.global().async {
            if let cg = try? generator.copyCGImage(at: time, actualTime: nil) {
                DispatchQueue.main.async { self.image = UIImage(cgImage: cg) }
            }
        }
    }
}
