//
//  LoginViewModel.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 31.08.2025.
//

import Foundation
import Combine

@MainActor
final class LoginViewModel: ObservableObject {
    @Published var loginResult: KullaniciResponse?

    func loginUser(username: String, password: String) {
        Task {
            do {
                let res = try await APIService.shared.login(username: username, password: password)
                loginResult = res
            } catch let apiErr as APIError {
                switch apiErr {
                case .decodeFailed:
                    loginResult = KullaniciResponse(success: false,
                                                    message: "Yanıt çözümlenemedi",
                                                    userId: nil)
                case .server(let status):
                    loginResult = KullaniciResponse(success: false,
                                                    message: "Sunucu hatası (\(status))",
                                                    userId: nil)
                default:
                    loginResult = KullaniciResponse(success: false,
                                                    message: "Bağlantı hatası: \(apiErr.localizedDescription)",
                                                    userId: nil)
                }
            } catch {
                loginResult = KullaniciResponse(success: false,
                                                message: "Bağlantı hatası: \(error.localizedDescription)",
                                                userId: nil)
            }
        }
    }
}

