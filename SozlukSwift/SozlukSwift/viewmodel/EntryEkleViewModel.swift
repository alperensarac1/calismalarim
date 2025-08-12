//
//  EntryEkleViewModel.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation

final class EntryEkleViewModel {

    private let dao = SozlukDao.shared
    var onAddResult: ((SimpleResponse) -> Void)?

    func addEntry(userId: Int, title: String, content: String) {
        dao.addEntry(userId: userId, title: title, content: content) { [weak self] result in
            guard let self else { return }
            switch result {
            case .success(let resp):
                DispatchQueue.main.async { self.onAddResult?(resp) }
            case .failure:
                DispatchQueue.main.async { self.onAddResult?(SimpleResponse(success: false, message: "Bağlantı hatası")) }
            }
        }
    }
}
