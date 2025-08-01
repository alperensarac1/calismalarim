import Foundation
import Combine

class HaberlerViewModel: ObservableObject {
    @Published var gundemHaberler: [HaberModel] = []
    @Published var sonDakikaHaberler: [HaberModel] = []
    @Published var kategoriler: [HaberTuruModel] = [] // Kategoriler buraya eklendi
    
    private let service = ApiService.shared
    
    func loadGundemHaberler() {
        service.getGundemHaberler { [weak self] result in
            if case .success(let data) = result {
                DispatchQueue.main.async {
                    self?.gundemHaberler = data
                }
            }
        }
    }

    func loadSonDakikaHaberler() {
        service.getSonDakikaHaberler { [weak self] result in
            if case .success(let data) = result {
                DispatchQueue.main.async {
                    self?.sonDakikaHaberler = data
                }
            }
        }
    }
    
    func loadKategoriler() {
        service.getKategoriler { [weak self] result in
            if case .success(let data) = result {
                DispatchQueue.main.async {
                    self?.kategoriler = data // Burada kategoriler güncelleniyor
                }
            }
        }
    }
}
