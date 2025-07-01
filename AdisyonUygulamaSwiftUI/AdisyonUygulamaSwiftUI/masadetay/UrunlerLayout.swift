import Foundation
import SwiftUI

struct UrunlerLayout: View {
    let masa: Masa
    let tumUrunListesi: [Urun]
    let kategoriListesi: [Kategori]
    @State private var selectedKategoriIndex: Int = 0
    var detayVM: MasaDetayViewModel

    private var kategorilerWithAll: [Kategori] {
        [Kategori(id: 0, kategori_ad: "Tümü")] + kategoriListesi
    }

    private var guncelUrunListesi: [Urun] {
        let selected = kategorilerWithAll[selectedKategoriIndex]
        return selected.id == 0
            ? tumUrunListesi
            : tumUrunListesi.filter { $0.urunKategori.id == selected.id }
    }

    private let gridColumns: [GridItem] = Array(
        repeating: GridItem(.flexible(), spacing: 8),
        count: 3
    )

    private var categoryList: some View {
        List {
            ForEach(kategorilerWithAll.indices, id: \.self) { idx in
                Text(kategorilerWithAll[idx].kategori_ad)
                    .padding(8)
                    .background(selectedKategoriIndex == idx ? Color.blue.opacity(0.2) : Color.clear)
                    .cornerRadius(8)
                    .onTapGesture {
                        selectedKategoriIndex = idx
                    }
            }
        }
        .listStyle(PlainListStyle())
        .frame(width: 150)
    }

    private var emptyMessage: some View {
        Text("Seçilen kategoriye ait ürün bulunamadı.")
            .font(.system(size: 18))
            .foregroundColor(.gray)
            .multilineTextAlignment(.center)
            .padding()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var productGrid: some View {
        ScrollView {
            let products = guncelUrunListesi
            let screenWidth = UIScreen.main.bounds.width
            let itemHeight = ((screenWidth - (8 * 4) - 150) / 3) * 1.2

            LazyVGrid(columns: gridColumns, spacing: 8) {
                ForEach(products) { urun in
                    UrunCell(urun: urun, masa: masa, detayVM: detayVM)
                        .frame(height: itemHeight)
                }
            }
            .padding(8)
        }
    }

    var body: some View {
        HStack(spacing: 8) {
            categoryList

            if guncelUrunListesi.isEmpty {
                emptyMessage
            } else {
                productGrid
            }
        }
    }
}

struct UrunCell: View {
    let urun: Urun
    let masa: Masa
    let detayVM: MasaDetayViewModel

    var body: some View {
        VStack(spacing: 6) {
            if let imageData = Data(base64Encoded: urun.urunResim),
               let uiImage = UIImage(data: imageData) {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFit()
                    .frame(height: 80)
            } else {
                Image(systemName: "photo")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 80)
                    .foregroundColor(.gray)
            }

            Text(urun.urunAd)
                .font(.system(size: 14))
                .multilineTextAlignment(.center)
                .lineLimit(2)

            HStack(spacing: 8) {
                Button {
                    Task {
                        await detayVM.urunCikar(urunId: urun.id)
                    }
                } label: {
                    Image(systemName: "minus.circle.fill")
                }

                Text("\(urun.urunAdet)")
                    .font(.system(size: 14))
                    .frame(minWidth: 24)

                Button {
                    Task {
                        await detayVM.urunEkle(urunId: urun.id)
                    }
                } label: {
                    Image(systemName: "plus.circle.fill")
                }
            }

            Text(urun.urunFiyat.fiyatYaz())
                .font(.system(size: 12, weight: .medium))
                .padding(.top, 4)
        }
        .padding(8)
        .background(Color.white)
        .cornerRadius(8)
        .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 1)
    }
}
