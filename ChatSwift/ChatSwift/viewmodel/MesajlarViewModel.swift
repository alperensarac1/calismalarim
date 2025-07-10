import Foundation
import Combine

final class MesajlarViewModel {

    @Published private(set) var mesajlar: [Mesaj] = []
    @Published private(set) var hataMesaji: String?

    private let api = ApiService.shared
    private var timer: AnyCancellable?
    private var bag: Set<AnyCancellable> = []

    /// Başlatıldığında **hemen** çeker ve ardından her 15 sn'de bir yeniler.
    func mesajlariYuklePeriyodik(gonderenId: Int, aliciId: Int) {
        timer?.cancel()                                     // önceki timer varsa iptal
        yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId)

        timer = Timer.publish(every: 15, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                self?.yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId)
            }
    }

    func mesajGonder(gonderenId: Int,
                     aliciId: Int,
                     mesajText: String,
                     base64Image: String? = nil) {
        let resimVar = (base64Image?.isEmpty ?? true) ? 0 : 1

        api.mesajGonder(gonderenId: gonderenId,
                        aliciId: aliciId,
                        mesajText: mesajText,
                        resimVar: resimVar,
                        base64Img: base64Image) { [weak self] result in
            switch result {
            case .success(let resp):
                if resp.success {
                    self?.yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId)
                } else {
                    self?.hataMesaji = "Mesaj gönderilemedi"
                }
            case .failure(let err):
                self?.hataMesaji = "Hata: \(err.localizedDescription)"
            }
        }
    }

    // MARK: - Helpers
    private func yukleMesajlar(gonderenId: Int, aliciId: Int) {
        api.mesajlariGetir(gonderenId: gonderenId, aliciId: aliciId) { [weak self] result in
            switch result {
            case .success(let resp):
                if resp.success {
                    self?.mesajlar     = resp.mesajlar
                    self?.hataMesaji   = nil
                } else {
                    self?.hataMesaji   = "Mesajlar yüklenemedi"
                }
            case .failure(let err):
                self?.hataMesaji       = "Hata: \(err.localizedDescription)"
            }
        }
    }

    deinit {
        timer?.cancel()
    }
}
