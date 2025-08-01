//
//  KategorilerViewModel.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import Foundation

class KategorilerViewModel {

    private let haberService = ApiService.shared

    var kategoriHaberleriDidChange: (([HaberModel]) -> Void)?

    private(set) var kategoriHaberleri: [HaberModel] = [] {
        didSet {
            kategoriHaberleriDidChange?(kategoriHaberleri)
        }
    }

    func loadKategoriHaberleri(turAd: String) {
        haberService.getHaberler { [weak self] result in
            switch result {
            case .success(let tumHaberler):
                let filtrelenmis = tumHaberler.filter { $0.tur_adi == turAd }
                DispatchQueue.main.async {
                    self?.kategoriHaberleri = filtrelenmis
                }
            case .failure(let error):
                print("Haberler alınamadı: \(error)")
            }
        }
    }
}
