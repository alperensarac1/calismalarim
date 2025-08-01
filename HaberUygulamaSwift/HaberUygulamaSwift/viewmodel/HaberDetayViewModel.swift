//
//  HaberDetayViewModel.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import Foundation

class HaberDetayViewModel {

    private let haberService = ApiService.shared

    var yorumlar: [YorumModel] = [] {
        didSet {
            yorumlarDidChange?(yorumlar)
        }
    }

    var yorumlarDidChange: (([YorumModel]) -> Void)?
    var son3HaberlerDidChange: (([HaberModel]) -> Void)?

    func loadYorumlar(haberId: String) {
        haberService.getYorumlar(haberId: haberId) { [weak self] result in
            if case .success(let data) = result {
                DispatchQueue.main.async {
                    self?.yorumlar = data
                }
            }
        }
    }

    func yorumEkle(haberId: String, ad: String, yorum: String) {
        let request = YorumInsertRequest(haber_id: haberId, kullanici: ad, yorum: yorum)
        haberService.insertYorum(request: request) { [weak self] result in
            if case .success(let response) = result, response.tf == true {
                self?.loadYorumlar(haberId: haberId)
            }
        }
    }

    func loadSon3Haber() {
        haberService.getSon3Haber { result in
            if case .success(let haberler) = result {
                DispatchQueue.main.async {
                    self.son3HaberlerDidChange?(haberler)
                }
            }
        }
    }
}

