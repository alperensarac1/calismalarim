//
//  QRKodVC.swift
//  KahveQRKodOlusturucuSwiftUI
//
//  Created by Alperen Saraç on 12.04.2025.
//


import SwiftUI

struct QRKodVC: View {
    @StateObject private var viewModel = QRKodVM()

    var body: some View {
        VStack {

            VStack(spacing: 15) {
                if let qrImage = viewModel.qrKodGorseli {
                    Image(uiImage: qrImage)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 300, height: 300)
                        .cornerRadius(20)
                        .shadow(radius: 8)
                } else {
                    ProgressView("QR Kod oluşturuluyor...")
                }
                Text("Yenileme için kalan süre: \(viewModel.kalanSure)")
                    .font(.footnote)
                    .foregroundColor(.gray)
                Text("Her 5 kahve alımınızda 1 kahve de bizden !!")
                    .font(.system(size: 20))
                    .foregroundColor(textColor)

             
            }//:VStack
            .frame(maxWidth: .infinity)

            Spacer()
        }//:VStack
        .padding()
        .background(bgColor)
        .onAppear {
            viewModel.yeniKodUret()
        }
    }
}
