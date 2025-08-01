
import Foundation
import SwiftUI
import AVKit

struct KategoriView: View {
    var kategoriAd: String
    @StateObject private var viewModel = KategorilerViewModel()

    init(kategoriAd: String) {
        self.kategoriAd = kategoriAd
    }

    var body: some View {
        VStack {
            Text("Kategori: \(kategoriAd)")
                .font(.title)
                .padding()

            // Haberler grid görünümü
            if !viewModel.kategoriHaberleri.isEmpty {
                LazyVGrid(columns: [GridItem(.flexible())]) {
                    ForEach(viewModel.kategoriHaberleri, id: \.id) { haber in
                        // Her bir haber kartı tıklanabilir ve yönlendirme yapacak
                        NavigationLink(destination: HaberDetayView(haber: haber)) {
                            HaberCardView(haber: haber)
                        }
                    }
                }
                .padding()
            } else {
                Text("Haberler yükleniyor...")
                    .padding()
            }
            Spacer()
        }
        .onAppear {
            viewModel.loadKategoriHaberleri(turAd: kategoriAd)
        }
    }
}

struct KategoriView_Previews: PreviewProvider {
    static var previews: some View {
        KategoriView(kategoriAd: "Teknoloji")
    }
}
