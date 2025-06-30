//
//  UrunDetayVC.swift
//  KahveQRKodOlusturucuSwiftUI
//
//  Created by Alperen Saraç on 12.04.2025.
//

import SwiftUI

struct UrunDetayVC: View {
    let urun: Urun
    @Environment(\.dismiss) var dismiss

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .padding(10)
                }
                Spacer()
            }//:HStack

            AsyncImage(url: URL(string: urun.urun_resim.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")) { phase in
                if let image = phase.image {
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(height: 200)
                        .cornerRadius(12)
                } else if phase.error != nil {
                    Image("kahve")
                        .resizable()
                        .frame(height: 200)
                        .cornerRadius(12)
                } else {
                    ProgressView()
                        .frame(height: 200)
                }
            }

            Text(urun.urun_ad)
                .font(.title2)
                .bold()

            Text(urun.urun_aciklama)
                .font(.body)
                .multilineTextAlignment(.center)
            if urun.urun_indirim == "1",
               let indirimliFiyat = Double(urun.urun_indirimli_fiyat),
               let fiyat = Double(urun.urun_fiyat) {

                Text("\(Int(indirimliFiyat)) TL")
                    .foregroundColor(.red)
                    .font(.title2)
                    .bold()

                Text("\(Int(fiyat)) TL")
                    .strikethrough()
                    .foregroundColor(.gray)

            } else if let fiyat = Double(urun.urun_fiyat) {
                Text("\(Int(fiyat)) TL")
                    .foregroundColor(.primary)
                    .font(.title2)
            }

            Spacer()
        }//:VStack
        .padding()
        .navigationBarBackButtonHidden(true).background(bgColor)
    }
}
