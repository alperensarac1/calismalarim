import Foundation
import Combine

class HaberDetayViewModel: ObservableObject {

    private let haberService = ApiService.shared

    // Published properties, SwiftUI'de UI'yi güncellemek için kullanılır
    @Published var yorumlar: [YorumModel] = []
    @Published var son3Haberler: [HaberModel] = []

    // Yorumları ve son 3 haberi yüklerken callback'lere gerek yok
    // Bu nedenle `didSet` veya closure'lara gerek yok

    func loadYorumlar(haberId: String) {
        haberService.getYorumlar(haberId: haberId) { [weak self] result in
            if case .success(let data) = result {
                DispatchQueue.main.async {
                    self?.yorumlar = data  // @Published sayesinde bu değişiklik UI'yi günceller
                }
            }
        }
    }

    func yorumEkle(haberId: String, ad: String, yorum: String) {
        let request = YorumInsertRequest(haber_id: haberId, kullanici: ad, yorum: yorum)
        haberService.insertYorum(request: request) { [weak self] result in
            if case .success(let response) = result, response.tf == true {
                // Yorum başarıyla eklendikten sonra yorumları yeniden yükle
                self?.loadYorumlar(haberId: haberId)
            }
        }
    }

    func loadSon3Haber() {
        haberService.getSon3Haber { [weak self] result in
            if case .success(let haberler) = result {
                DispatchQueue.main.async {
                    self?.son3Haberler = haberler  // @Published sayesinde bu değişiklik UI'yi günceller
                }
            }
        }
    }
}
