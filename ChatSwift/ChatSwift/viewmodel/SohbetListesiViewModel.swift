import Foundation
import Combine

final class SohbetListesiViewModel {
    
    @Published private(set) var konusulanKisiler: [KonusulanKisi] = []
    @Published private(set) var hataMesaji: String?
    
    private let api = ApiService.shared
    private var yenilemeTimer: AnyCancellable?
    
    /// 15 saniyede bir konuşulan kişileri getirir
    func sohbetListesiniBaslat(kullaniciId: Int) {
        yenilemeTimer?.cancel()
        sohbetListesiniGetir(kullaniciId: kullaniciId) // hemen ilk yükleme
        
        yenilemeTimer = Timer.publish(every: 15, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                self?.sohbetListesiniGetir(kullaniciId: kullaniciId)
            }
    }

    private func sohbetListesiniGetir(kullaniciId: Int) {
        api.konusulanKisiler(kullaniciId: kullaniciId) { [weak self] result in
            switch result {
            case .success(let response):
                if response.success {
                    self?.konusulanKisiler = response.kisiler
                    self?.hataMesaji = nil
                } else {
                    self?.hataMesaji = "Liste alınamadı"
                }
            case .failure(let error):
                self?.hataMesaji = "Sunucu hatası: \(error.localizedDescription)"
            }
        }
    }

    deinit {
        yenilemeTimer?.cancel()
    }
}
