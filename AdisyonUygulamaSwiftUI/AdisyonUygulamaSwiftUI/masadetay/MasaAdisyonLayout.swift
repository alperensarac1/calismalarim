import Foundation
import SwiftUI
import Combine



struct MasaAdisyonLayout: View {
    let masa: Masa
    @ObservedObject var viewModel: MasaDetayViewModel

    @State private var showingPaymentAlert = false
    @State private var isProcessingPayment = false

    var body: some View {
        VStack(spacing: 16) {
            List(viewModel.urunler, id: \.urunId) { urun in
                HStack {
                    Text("\(urun.urunAd) (\(urun.adet))")
                        .font(.system(size: 14))
                    Spacer()
                    Text(String(format: "%.2f TL", urun.toplamFiyat))
                        .font(.system(size: 14))
                }
                .padding(.vertical, 6)
            }
            .listStyle(PlainListStyle())
            .frame(minHeight: 200)

            Divider()

            HStack {
                Text("Toplam:")
                    .font(.system(size: 18, weight: .semibold))
                Spacer()
                Text(String(format: "%.2f TL", viewModel.toplamFiyat))
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.blue)
            }
            .padding(.horizontal, 16)

            Button(action: pay) {
                Text(isProcessingPayment ? "Bekleyiniz..." : "Ödeme Al")
                    .frame(maxWidth: .infinity, minHeight: 44)
                    .background(isProcessingPayment ? Color.gray : Color.green)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                    .font(.system(size: 16, weight: .bold))
            }
            .disabled(isProcessingPayment)
            .padding(.horizontal, 16)
        }
        .padding(.vertical, 16)
        .alert("Ödeme alındı", isPresented: $showingPaymentAlert) {
            Button("Tamam", role: .cancel) {}
        }
    }

    private func pay() {
        isProcessingPayment = true
        Task {
            await viewModel.odemeAl {
                showingPaymentAlert = true
                isProcessingPayment = false
            }
        }
    }
}
