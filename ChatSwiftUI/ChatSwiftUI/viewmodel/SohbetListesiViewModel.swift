import Foundation
import Combine
import Alamofire

@MainActor
final class SohbetListesiViewModel: ObservableObject {
    
    @Published var konusulanKisiler: [KonusulanKisi] = []
    @Published var hataMesaji: String? = nil
    
    private var yenilemeTimer: Timer?
    
    func sohbetListesiniBaslat(kullaniciId: Int) {
        yenilemeTimer?.invalidate()
        
        Task { await self.yukleKonusulanKisiler(kullaniciId: kullaniciId) }
        
        yenilemeTimer = Timer.scheduledTimer(withTimeInterval: 15.0, repeats: true) { [weak self] _ in
            guard let self else { return }
            Task { await self.yukleKonusulanKisiler(kullaniciId: kullaniciId) }
        }
    }
    
    func durdur() {
        yenilemeTimer?.invalidate()
        yenilemeTimer = nil
    }
    
    // Artık async hale getirildi ve çağrılar MainActor context’inde yapılmalı
    private func yukleKonusulanKisiler(kullaniciId: Int) async {
        await withCheckedContinuation { continuation in
            ApiService.shared.konusulanKisiler(kullaniciId: kullaniciId) { [weak self] result in
                guard let self else {
                    continuation.resume()
                    return
                }
                
                switch result {
                case .success(let yanit):
                    if yanit.success {
                        self.konusulanKisiler = yanit.kisiler
                        self.hataMesaji = nil
                    } else {
                        self.hataMesaji = "Liste alınamadı"
                    }
                case .failure(let error):
                    self.hataMesaji = "Sunucu hatası: \(error.localizedDescription)"
                }
                
                continuation.resume()
            }
        }
    }
    
    deinit {
        yenilemeTimer?.invalidate()
    }
}
