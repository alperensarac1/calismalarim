import Foundation
import Combine
import Alamofire

final class MesajlarViewModel: ObservableObject {
    
    // --- UI’ya yayımlanan durumlar ---
    @Published var mesajlar: [Mesaj] = []
    @Published var hataMesaji: String? = nil
    
    // --- İç alanlar ---
    private var mesajGuncelleTimer: Timer?
    
    // MARK: - Periyodik yükleme
    
    /// 15 sn’de bir mesaj listesini yeniler. İlk çağrıda hemen çeker.
    func mesajlariYuklePeriyodik(gonderenId: Int, aliciId: Int) {
        mesajGuncelleTimer?.invalidate()
        
        mesajGuncelleTimer = Timer.scheduledTimer(withTimeInterval: 15.0, repeats: true) { _ in
            self.yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId)
        }
        
        yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId)
    }
    
    // MARK: - Sunucudan çekme
    
    private func yukleMesajlar(gonderenId: Int, aliciId: Int) {
        ApiService.shared.mesajlariGetir(gonderenId: gonderenId, aliciId: aliciId) { [weak self] result in
            guard let self else { return }
            
            switch result {
            case .success(let yanit):
                if yanit.success {
                    DispatchQueue.main.async {
                        self.mesajlar = yanit.mesajlar
                        self.hataMesaji = nil
                    }
                } else {
                    DispatchQueue.main.async {
                        self.hataMesaji = "Mesajlar yüklenemedi"
                    }
                }
                
            case .failure(let afError):
                DispatchQueue.main.async {
                    self.hataMesaji = "Hata: \(afError.localizedDescription)"
                }
            }
        }
    }
    
    // MARK: - Mesaj gönderme
    
    func mesajGonder(
        gonderenId: Int,
        aliciId: Int,
        mesajText: String,
        base64Image: String? = nil
    ) {
        let resimVar = base64Image?.isEmpty == false ? 1 : 0
        
        ApiService.shared.mesajGonder(
            gonderenId: gonderenId,
            aliciId: aliciId,
            mesajText: mesajText,
            resimVar: resimVar,
            base64Img: base64Image
        ) { [weak self] result in
            guard let self else { return }
            
            switch result {
            case .success(let yanit):
                if yanit.success {
                    // Gönderme başarılı -> listeyi yenile
                    self.yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId)
                } else {
                    DispatchQueue.main.async {
                        self.hataMesaji = yanit.error ?? "Mesaj gönderilemedi"
                    }
                }
                
            case .failure(let afError):
                DispatchQueue.main.async {
                    self.hataMesaji = "Hata: \(afError.localizedDescription)"
                }
            }
        }
    }
    
    // MARK: - Temizlik
    
    deinit {
        mesajGuncelleTimer?.invalidate()
    }
}
