//
//  KayitViewModel.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation
final class KayitViewModel {
    func register(username: String, password: String, email: String, completion: @escaping (SimpleResponse) -> Void) {
        SozlukDao.shared.register(username: username, password: password, email: email) { result in
            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    completion(response)
                case .failure:
                    completion(SimpleResponse(success: false, message: "Bağlantı hatası"))
                }
            }
        }
    }
}
