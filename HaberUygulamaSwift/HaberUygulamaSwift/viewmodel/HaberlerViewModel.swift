//
//  HaberlerViewModel.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import Foundation
import Combine

class HaberlerViewModel {

    private let service = ApiService.shared

    var gundemDidChange: (([HaberModel]) -> Void)?
    var sonDakikaDidChange: (([HaberModel]) -> Void)?
    var kategorilerDidChange: (([HaberTuruModel]) -> Void)?

    func loadGundemHaberler() {
        service.getGundemHaberler { [weak self] result in
            switch result {
            case .success(let data):
                DispatchQueue.main.async {
                    self?.gundemDidChange?(data)
                }
            case .failure(let error):
                print("Gündem alınamadı: \(error)")
            }
        }
    }

    func loadSonDakikaHaberler() {
        service.getSonDakikaHaberler { [weak self] result in
            switch result {
            case .success(let data):
                DispatchQueue.main.async {
                    self?.sonDakikaDidChange?(data)
                }
            case .failure(let error):
                print("Son Dakika alınamadı: \(error)")
            }
        }
    }

    func loadKategoriler() {
        service.getKategoriler { [weak self] result in
            switch result {
            case .success(let data):
                DispatchQueue.main.async {
                    self?.kategorilerDidChange?(data)
                }
            case .failure(let error):
                print("Kategoriler alınamadı: \(error)")
            }
        }
    }
}
