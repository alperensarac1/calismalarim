import Foundation
import SwiftUI

struct MasaDetayLayout: View {
    let masa: Masa
    @ObservedObject var detayVM: MasaDetayViewModel
    @ObservedObject var urunVM: UrunViewModel
    let onBackPressed: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Header
            MasaDetayHeaderLayout(masa: masa, onBackPressed: onBackPressed)
                .frame(height: 60)
                .background(Color.white)

            // Body: Adisyon & Ürünler
            HStack(spacing: 8) {
                // Adisyon paneli
                MasaAdisyonLayout(masa: masa, viewModel: detayVM)
                    .frame(width: 200)
                    .background(Color.white)
                    .cornerRadius(8)

                // Ürünler paneli
                UrunlerLayout(
                    masa: masa,
                    tumUrunListesi: urunVM.urunler.map { urun in
                        Urun(
                            id: urun.id,
                            urunAd: urun.urunAd,
                            urunFiyat: urun.urunFiyat,
                            urunResim: urun.urunResim,
                            urunAdet: urun.urunAdet,
                            urunKategori: urun.urunKategori
                        )
                    },
                    kategoriListesi: urunVM.kategoriler,
                    detayVM: detayVM
                )
                .background(Color.white)
                .cornerRadius(8)
            }
            .onAppear {
                Task {
                    await detayVM.yukleTumVeriler()
                    await urunVM.kategorileriYukle()
                    await urunVM.urunleriYukle()
                }
            }
        }
    }
}
