//
//  UrunlerimizVC.swift
//  KahveQRKodOlusturucuSwiftUI
//
//  Created by Alperen Saraç on 12.04.2025.
//

import SwiftUI

struct UrunlerimizVC: View {
    @StateObject private var viewModel = UrunlerimizVM(kahveServis: ServiceImpl.getInstance())
    @State private var selectedUrun: Urun?
    @State private var showDetay = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                if !viewModel.kampanyalar.isEmpty {
                    UrunKategoriView(
                        kategoriAdi: "Kampanyalar",
                        urunler: viewModel.kampanyalar,
                        onUrunTap: { urun in
                            selectedUrun = urun
                        })
                }

                if !viewModel.icecekler.isEmpty {
                    UrunKategoriView(
                        kategoriAdi: "Atıştırmalıklar",
                        urunler: viewModel.icecekler,
                        onUrunTap: { urun in
                            selectedUrun = urun
                        })
                }

                if !viewModel.atistirmaliklar.isEmpty {
                    UrunKategoriView(
                        kategoriAdi: "İçecekler",
                        urunler: viewModel.atistirmaliklar,
                        onUrunTap: { urun in
                            selectedUrun = urun
                        })
                }
            }//:VStack
            .padding()
        }//:ScrollView
        .sheet(item: $selectedUrun) { urun in
            UrunDetayVC(urun: urun)
        }.background(bgColor)
    }
}

struct UrunKategoriView: View {
    let kategoriAdi: String
    let urunler: [Urun]
    let onUrunTap: (Urun) -> Void

    var body: some View {
        VStack(alignment: .leading) {
            Text(kategoriAdi)
                .font(.title3)
                .bold()
                .padding(.leading, 4)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack {
                    ForEach(urunler, id: \.id) { urun in
                        UrunCard(urun: urun, onTap: { onUrunTap(urun) })
                    }
                }
            }//:ScrollView
        }//:VStack
    }
}

struct UrunCard: View {
    let urun: Urun
    let onTap: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            ZStack{
                AsyncImage(url: URL(string: urun.urun_resim)) { phase in
                    switch phase {
                    case .success(let image):
                        image.resizable()
                             .scaledToFill()
                             .frame(width: 150, height: 100)
                             .clipped()
                             .cornerRadius(8)
                    case .failure(_):
                        Color.gray.frame(width: 150, height: 100).cornerRadius(8)
                    case .empty:
                        ProgressView()
                            .frame(width: 150, height: 100)
                    @unknown default:
                        EmptyView()
                    }
                }
                if urun.urun_indirim == "1"{
                    VStack{
                        Spacer()
                        HStack{
                            Spacer()
                            Text("%-\(indirimYuzdeHesapla(indirimsizFiyat: urun.urun_fiyat, indirimliFiyat: urun.urun_indirimli_fiyat))")
                                .font(.caption2).fontWeight(.bold)
                                .foregroundColor(.red).background(textColor)
                        }//:HStack
                    }//:VStack
                    
                }
            }//:ZStack
            

            HStack{
                Text(urun.urun_ad)
                    .font(.headline)
                    .lineLimit(1)

                if urun.urun_indirim == "1" {
                    
                  

                    Text("\(urun.urun_fiyat) TL")
                        .strikethrough()
                        .font(.caption)
                        .foregroundColor(.gray)
                    
                    Text("\(urun.urun_indirimli_fiyat) TL")
                        .foregroundColor(.red)
                        .bold()

                } else {
                    Text("\(urun.urun_fiyat) TL")
                        .foregroundColor(textColor)
                }
            }//:HStack
            
            
        }//:ScrollView
        .padding(8)
        .background(Color(.systemGray6))
        .cornerRadius(12)
        .frame(width: 160)
        .onTapGesture { onTap() }
    }
}
