//
//  RegisterViewController.swift
//  OnlineTaksiSwift
//
//  Created by Alperen Saraç on 24.04.2026.
//

import Foundation
import UIKit

final class RegisterViewController: UIViewController {

    @IBOutlet weak var fullNameTextField: UITextField!
    @IBOutlet weak var phoneTextField: UITextField!
    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var registerButton: UIButton!
    @IBOutlet weak var goLoginButton: UIButton!
    @IBOutlet weak var roleSegmentedControl: UISegmentedControl!
    private let authRepository = AuthRepository()


        override func viewDidLoad() {
            super.viewDidLoad()

            setupRoleSegment()
            registerButton.addTarget(self, action: #selector(registerTapped), for: .touchUpInside)
            goLoginButton.addTarget(self, action: #selector(goLoginTapped), for: .touchUpInside)
        }

        private func setupRoleSegment() {
            roleSegmentedControl.removeAllSegments()
            roleSegmentedControl.insertSegment(withTitle: "Customer", at: 0, animated: false)
            roleSegmentedControl.insertSegment(withTitle: "Driver", at: 1, animated: false)
            roleSegmentedControl.selectedSegmentIndex = 0
        }



    @objc private func registerTapped() {
        let fullName = fullNameTextField.text?.trimmingCharacters(in: .whitespaces) ?? ""
        let phone = phoneTextField.text?.trimmingCharacters(in: .whitespaces) ?? ""
        let emailRaw = emailTextField.text?.trimmingCharacters(in: .whitespaces) ?? ""
        let password = passwordTextField.text?.trimmingCharacters(in: .whitespaces) ?? ""

        guard !fullName.isEmpty, !phone.isEmpty, !password.isEmpty else {
            showAlert("Ad soyad, telefon ve şifre zorunlu")
            return
        }

        let selectedRole: String = roleSegmentedControl.selectedSegmentIndex == 1
            ? "driver"
            : "customer"

        registerButton.isEnabled = false
        registerButton.setTitle("Kayıt yapılıyor...", for: .normal)

        authRepository.register(
            fullName: fullName,
            phone: phone,
            email: emailRaw.isEmpty ? nil : emailRaw,
            password: password,
            role: selectedRole
        ) { [weak self] result in
            guard let self else { return }

            self.registerButton.isEnabled = true
            self.registerButton.setTitle("Kayıt Ol", for: .normal)

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

    @objc private func goLoginTapped() {
        dismiss(animated: true)
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
