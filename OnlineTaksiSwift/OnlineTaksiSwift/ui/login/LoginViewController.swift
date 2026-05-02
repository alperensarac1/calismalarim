//
//  LoginViewController.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation
import UIKit

final class LoginViewController: UIViewController {

    @IBOutlet weak var phoneTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var goRegisterButton: UIButton!

    private let authRepository = AuthRepository()

    override func viewDidLoad() {
        super.viewDidLoad()

        loginButton.addTarget(self, action: #selector(loginTapped), for: .touchUpInside)
        goRegisterButton.addTarget(self, action: #selector(goRegisterTapped), for: .touchUpInside)
    }

    @objc private func loginTapped() {
        let phone = phoneTextField.text?.trimmingCharacters(in: .whitespaces) ?? ""
        let password = passwordTextField.text?.trimmingCharacters(in: .whitespaces) ?? ""

        guard !phone.isEmpty, !password.isEmpty else {
            showAlert("Telefon ve şifre zorunlu")
            return
        }

        loginButton.isEnabled = false
        loginButton.setTitle("Giriş yapılıyor...", for: .normal)

        authRepository.login(phone: phone, password: password) { [weak self] result in
            guard let self else { return }

            self.loginButton.isEnabled = true
            self.loginButton.setTitle("Giriş Yap", for: .normal)

            switch result {
            case .success(let response):
                SessionManager.shared.saveAuth(
                    token: response.access_token,
                    userId: response.user_id,
                    fullName: response.full_name,
                    role: response.role
                )

                if response.role == "driver" {
                    self.openController(storyboardId: "DriverHomeVC")
                } else {
                    self.openController(storyboardId: "CustomerHomeVC")
                }

            case .failure(let error):
                self.showAlert(error.localizedDescription)
            }
        }
    }

    @objc private func goRegisterTapped() {
        openController(storyboardId: "RegisterVC")
    }

    private func openController(storyboardId: String) {
        let vc = storyboard!.instantiateViewController(withIdentifier: storyboardId)
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true)
    }

    private func showAlert(_ message: String) {
        let alert = UIAlertController(title: "Bilgi", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(alert, animated: true)
    }
}
