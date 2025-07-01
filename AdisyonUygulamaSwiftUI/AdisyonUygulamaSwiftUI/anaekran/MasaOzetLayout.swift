import SwiftUI
import Foundation

struct MasaOzetLayoutView: View {
    @Binding var masaListesi: [Masa]
    private var onMasaDetayTikla: (Masa) -> Void
    private var onOdemedenSonraGuncelle: () -> Void

    @State private var selectedMasa: Masa?
    @State private var showActionSheet = false
    @State private var showPaymentAlert = false
    @State private var isProcessingPayment = false

    private let dao = AdisyonServisDao.shared

    init(
        masaListesi: Binding<[Masa]>,
        onMasaDetayTikla: @escaping (Masa) -> Void,
        onOdemedenSonraGuncelle: @escaping () -> Void
    ) {
        self._masaListesi = masaListesi
        self.onMasaDetayTikla = onMasaDetayTikla
        self.onOdemedenSonraGuncelle = onOdemedenSonraGuncelle
    }

    private var acikMasalar: [Masa] {
        masaListesi.filter { $0.acikMi == 1 }
    }

    var body: some View {
        VStack(spacing: 20) {
            Text("\(acikMasalar.count) adet masa açık.")
                .font(.system(size: 20))
                .foregroundColor(.red)

            List(acikMasalar) { masa in
                Button {
                    selectedMasa = masa
                    showActionSheet = true
                } label: {
                    HStack {
                        Text("Masa \(masa.id)")
                        Spacer()
                        Text(masa.toplamFiyat.fiyatYaz())
                    }
                }
            }
            .listStyle(PlainListStyle())
        }
        .padding(50)
        .actionSheet(isPresented: $showActionSheet) {
            guard let masa = selectedMasa else {
                return ActionSheet(title: Text(""), buttons: [.cancel()])
            }
            return ActionSheet(
                title: Text("Masa \(masa.id)"),
                buttons: [
                    .default(Text("Ürün Ekle")) {
                        onMasaDetayTikla(masa)
                    },
                    .default(Text("Ödeme Al")) {
                        isProcessingPayment = true
                        Task {
                            do {
                                _ = try await dao.masaOdemeYap(masaId: masa.id)
                                showPaymentAlert = true
                                onOdemedenSonraGuncelle()
                            } catch {
                                print("⛔️ Ödeme hatası: \(error.localizedDescription)")
                            }
                            isProcessingPayment = false
                        }
                    },
                    .cancel(Text("İptal"))
                ]
            )
        }
        .alert("Ödeme alındı", isPresented: $showPaymentAlert) {
            Button("Tamam", role: .cancel) { }
        }
    }
}
