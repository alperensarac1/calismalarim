import Foundation
import Combine
class KategorilerViewModel: ObservableObject {
    @Published var kategoriHaberleri: [HaberModel] = []
    private let haberService = ApiService.shared
    
    // Veri çekme fonksiyonu
    func loadKategoriHaberleri(turAd: String) {
        haberService.getHaberler { [self] result in
            switch result {
            case .success(let tumHaberler):
                // Kategorilere göre filtreleme işlemi yapılıyor
                let filtrelenmis = tumHaberler.filter { $0.tur_adi == turAd }
                DispatchQueue.main.async {
                    self.kategoriHaberleri = filtrelenmis
                }
            case .failure(let error):
                print("Haberler alınamadı: \(error)")
            }
        }
    }
}
