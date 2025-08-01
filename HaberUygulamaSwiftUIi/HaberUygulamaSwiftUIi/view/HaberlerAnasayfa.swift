import SwiftUI

struct HaberlerAnasayfa: View {
    @StateObject private var viewModel = HaberlerViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading) {
                Text("Gündem")
                    .font(.title)
                    .padding(.top)
                
                // Gündem News Horizontal List
                ScrollView(.horizontal) {
                    LazyHStack {
                        ForEach(viewModel.gundemHaberler, id: \.id) { haber in
                            HaberCardView(haber: haber)
                        }
                    }
                    .padding()
                }

                Text("Son Dakika")
                    .font(.title)
                    .padding(.top)
                
                // Son Dakika News Horizontal List
                ScrollView(.horizontal) {
                    LazyHStack {
                        ForEach(viewModel.sonDakikaHaberler, id: \.id) { haber in
                            HaberCardView(haber: haber)
                        }
                    }
                    .padding()
                }

                Text("Kategoriler")
                    .font(.title)
                    .padding(.top)

                // Kategoriler Card'ları
                ForEach(viewModel.kategoriler, id: \.id) { kategori in
                    NavigationLink(destination: KategoriView(kategoriAd: kategori.tur_adi ?? "")) {
                        KategoriCardview(kategori: kategori) // CardView içine alalım
                    }
                    .padding(.horizontal)
                }
            }
            .onAppear {
                viewModel.loadGundemHaberler()
                viewModel.loadSonDakikaHaberler()
                viewModel.loadKategoriler()
            }
        }
        .navigationTitle("Haberler")
    }
}

// Kategoriler için Card görünümü
struct KategoriCardview: View {
    var kategori: HaberTuruModel

    var body: some View {
        VStack {
            Text(kategori.tur_adi ?? "Kategori")
                .font(.headline)
                .foregroundColor(.white)
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.gray.opacity(0.5))
                .cornerRadius(10)
                .padding(.vertical, 5)
        }
        .shadow(radius: 5)
    }
}
