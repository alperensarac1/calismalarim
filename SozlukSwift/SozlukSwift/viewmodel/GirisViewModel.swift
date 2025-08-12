//
//  GirisViewModel.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import Foundation
final class GirisViewModel {
    func login(username: String, password: String,
                   completion: @escaping (_ success: Bool, _ message: String?, _ userId: Int?) -> Void) {
            SozlukDao.shared.login(username: username, password: password) { result in
                DispatchQueue.main.async {
                    switch result {
                    case .success(let resp):
                        completion(resp.success, resp.message, resp.user_id)
                    case .failure(let err):
                        completion(false, err.localizedDescription, nil)
                    }
                }
            }
        }
}

