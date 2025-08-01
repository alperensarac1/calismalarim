import SwiftUI
import AVKit

struct HaberCardView: View {
    var haber: HaberModel
    @State private var isPlaying = false
    
    var body: some View {
        NavigationLink(destination: HaberDetayView(haber: haber)) {
            VStack {
                // Video İçeriği (Video veya Görsel)
                if haber.media_type == "video" {
                    // Video player
                    VideoPlayer(player: AVPlayer(url: URL(string: haber.media_url)!))
                        .frame(height: 200)
                        .cornerRadius(8)
                        .overlay(
                            Button(action: {
                                isPlaying.toggle()
                            }, label: {
                                Image(systemName: isPlaying ? "pause.circle.fill" : "play.circle.fill")
                                    .font(.largeTitle)
                                    .foregroundColor(.white)
                            })
                        )
                } else {
                    // Image
                    AsyncImage(url: URL(string: haber.media_url)) { phase in
                        switch phase {
                        case .empty:
                            Color.gray.opacity(0.3)
                        case .success(let image):
                            image.resizable()
                                 .scaledToFit()
                                 .frame(height: 200)
                                 .cornerRadius(8)
                        case .failure:
                            Text("Resim yüklenemedi")
                        @unknown default:
                            EmptyView()
                        }
                    }
                    .padding(.horizontal)
                }

                HStack(alignment: .firstTextBaseline) {
                    Text(haber.baslik + "...")
                        .font(.headline)
                        .lineLimit(2)
                    Spacer()
                    Text("Devamını Oku->").foregroundColor(.pink.opacity(0.6))
                }
            }
            .padding()
            .background(Color.white)
            .cornerRadius(10)
            .shadow(radius: 5)
        }
        .buttonStyle(PlainButtonStyle()) // Remove default NavigationLink style to keep the card style intact
    }
}
