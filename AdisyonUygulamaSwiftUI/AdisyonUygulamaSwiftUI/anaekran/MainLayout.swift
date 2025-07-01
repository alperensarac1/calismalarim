import Foundation
import SwiftUI

struct MainLayoutView: View {
    @Binding var masaListesi: [Masa]
    private let onMasaClick: (Masa) -> Void

    private let dao = AdisyonServisDao.shared

    init(
        masaListesi: Binding<[Masa]>,
        onMasaClick: @escaping (Masa) -> Void
    ) {
        self._masaListesi = masaListesi
        self.onMasaClick = onMasaClick
    }

    var body: some View {
        GeometryReader { geo in
            HStack(spacing: 10) {
                MasaOzetLayoutView(
                    masaListesi: $masaListesi,
                    onMasaDetayTikla: onMasaClick,
                    onOdemedenSonraGuncelle: {
                        Task {
                            do {
                                masaListesi = try await dao.masalariGetir()
                            } catch {
                                print("🔁 Masa listesi güncellenemedi: \(error.localizedDescription)")
                            }
                        }
                    }
                )
                .frame(width: geo.size.width / 3)
                .background(Color.white.cornerRadius(8))

                MasalarLayout(
                    masaList: masaListesi,
                    onMasaClick: onMasaClick
                )
                .frame(width: geo.size.width * 2 / 3)
            }
            .padding(10)
            .background(
                Color(red: 1.0, green: 186/255, blue: 186/255)
            )
        }
    }
}
