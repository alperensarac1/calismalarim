import SwiftUI
import AVKit

struct HaberDetayView: View {
    var haber: HaberModel
    @StateObject private var viewModel = HaberDetayViewModel()
    @State private var yorumlar: [YorumModel] = []
    @State private var son3Haberler: [HaberModel] = []
    @State private var isPlaying = false
    @State private var rumuz = ""
    @State private var yorum = ""
    
    @State private var player: AVPlayer?
    
    init(haber: HaberModel) {
        self.haber = haber
    }
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
              
                
                
                // Video İçeriği (Video veya Görsel)
                            if haber.media_type == "video" {
                                Text(haber.baslik).font(.headline).foregroundColor(.gray.opacity(0.6))
                                VideoPlayer(player: player)
                                    .frame(height: 200)
                                    .cornerRadius(8)
                                    .padding(.horizontal)
                                
                                Button(action: {
                                    if let player = player {
                                        if player.timeControlStatus == .playing {
                                            player.pause()
                                            isPlaying = false
                                        } else {
                                            player.play()
                                            isPlaying = true
                                        }
                                    } else {
                                        playVideo()  // Eğer player nil ise yeni bir player başlatıyoruz
                                    }
                                }) {
                                    Text(isPlaying ? "Pause" : "Play")
                                        .font(.headline)
                                        .foregroundColor(.white)
                                        .padding()
                                        .background(Color.blue)
                                        .cornerRadius(8)
                                }
                            } else {
                                Text(haber.baslik).font(.headline).foregroundColor(.gray.opacity(0.6))
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
                


                // Yazar ve Yayınlanma Tarihi
                Text("\(haber.ad ?? "") \(haber.soyad ?? "") - \(haber.unvan ?? "")")
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .padding(.horizontal)
                
                Text(haber.yayinlanma_tarihi)
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .padding(.horizontal)
                
                // İçerik Metni
                Text(haber.icerik)
                    .font(.body)
                    .fontWeight(.bold)
                    .padding(.horizontal)
                
                
             
                
                if !viewModel.yorumlar.isEmpty {
                    // Yorumlar Bölümü
                    Text("Son Haberler")
                        .font(.title3)
                        .fontWeight(.bold)
                        .padding(.horizontal)
                    ForEach(viewModel.son3Haberler) { model in
                        SonUcHaberCardView(haber: model)
                    }
                    // Yorumlar Bölümü
                    Text("Yorumlar")
                        .font(.title3)
                        .fontWeight(.bold)
                        .padding(.horizontal)
                    ForEach(viewModel.yorumlar, id: \.id) { yorum in
                        Text("\(yorum.kullanici): \(yorum.yorum)")
                            .padding(.horizontal)
                    }
                } else {
                    ForEach(viewModel.son3Haberler) { model in
                        SonUcHaberCardView(haber: model)
                    }
                    Text("Yorumlar yükleniyor...")
                        .padding(.horizontal)
                }
                
                // Yorum Yaz Bölümü
                Text("Yorum Yaz")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.gray)
                    .padding(.horizontal)
                
                TextField("Rumuz", text: $rumuz)
                    .padding()
                    .background(RoundedRectangle(cornerRadius: 8).stroke(Color.gray, lineWidth: 1))
                    .padding(.horizontal)
                
                TextField("Yorumunuz", text: $yorum)
                    .padding()
                    .background(RoundedRectangle(cornerRadius: 8).stroke(Color.gray, lineWidth: 1))
                    .padding(.horizontal)
                
                Button(action: {
                    if !rumuz.isEmpty && !yorum.isEmpty {
                        viewModel.yorumEkle(haberId: haber.id, ad: rumuz, yorum: yorum)
                        rumuz = ""
                        yorum = ""
                    } else {
                        print("İlgili alanlar boş bırakılamaz")
                    }
                }) {
                    Text("GÖNDER")
                        .fontWeight(.bold)
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                        .padding(.horizontal)
                }
                
          

            }
        }.onAppear {
            viewModel.loadYorumlar(haberId: haber.id)
            viewModel.loadSon3Haber()
            
        }
        
    }

    
    func playVideo() {
        guard let url = URL(string: haber.media_url) else { return }
        player = AVPlayer(url: url)
        player?.play()
        isPlaying.toggle()
    }
}
