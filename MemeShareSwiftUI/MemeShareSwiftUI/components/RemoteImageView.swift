import Foundation
import SwiftUI

struct RemoteImageView: View {
    let url: URL
    @State private var uiImage: UIImage? = nil

    var body: some View {
        Group {
            if let img = uiImage {
                Image(uiImage: img).resizable().scaledToFill()
            } else {
                Rectangle().fill(.gray.opacity(0.15))
                    .overlay { ProgressView() }
            }
        }
        .onAppear(perform: load)
        .clipped()
    }

    private func load() {
        guard uiImage == nil else { return }
        URLSession.shared.dataTask(with: url) { data, _, _ in
            if let data, let img = UIImage(data: data) {
                DispatchQueue.main.async { self.uiImage = img }
            }
        }.resume()
    }
}
