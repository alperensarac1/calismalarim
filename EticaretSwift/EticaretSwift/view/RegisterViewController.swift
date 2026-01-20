//
//  RegisterViewController.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 16.01.2026.
//

import UIKit

final class RegisterViewController: UIViewController {

    @IBOutlet private weak var nameField: UITextField!
    @IBOutlet private weak var emailField: UITextField!
    @IBOutlet private weak var passwordField: UITextField!
    @IBOutlet private weak var password2Field: UITextField!
    @IBOutlet private weak var registerButton: UIButton!

    init() {
        super.init(nibName: "RegisterViewController", bundle: nil)
    }
    required init?(coder: NSCoder) { fatalError("XIB kullanıyoruz") }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }

    private func setupUI() {
        view.backgroundColor = .systemBackground
        title = "Kayıt"

        nameField.returnKeyType = .next

        emailField.keyboardType = .emailAddress
        emailField.autocapitalizationType = .none
        emailField.autocorrectionType = .no
        emailField.returnKeyType = .next

        passwordField.isSecureTextEntry = true
        passwordField.returnKeyType = .next

        password2Field.isSecureTextEntry = true
        password2Field.returnKeyType = .done

        nameField.delegate = self
        emailField.delegate = self
        passwordField.delegate = self
        password2Field.delegate = self

        if #available(iOS 15.0, *) {
            var cfg = UIButton.Configuration.filled()
            cfg.title = "Kayıt Ol"
            cfg.cornerStyle = .large
            registerButton.configuration = cfg
        } else {
            registerButton.setTitle("Kayıt Ol", for: .normal)
        }

        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Giriş",
                                                            style: .plain,
                                                            target: self,
                                                            action: #selector(goLogin))
    }

    @IBAction private func goLogin() {
        navigationController?.popViewController(animated: true)
    }

    @IBAction private func registerTapped(_ sender: UIButton) {
        let name = (nameField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
           let email = (emailField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
           let p1 = (passwordField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
           let p2 = (password2Field.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)

           guard name.count >= 2 else { showError("Ad Soyad çok kısa."); return }
           guard email.contains("@") else { showError("E-posta geçersiz."); return }
           guard p1.count >= 4 else { showError("Şifre en az 4 karakter olmalı."); return }
           guard p1 == p2 else { showError("Şifreler eşleşmiyor."); return }

           setLoading(true)

           Task {
               do {
                   let res = try await ApiClient.shared.register(name: name, email: email, password: p1)
                   AuthManager.shared.setSession(token: res.token, userId: res.user_id)

                   await MainActor.run {
                       self.setLoading(false)
                       self.navigationController?.popToRootViewController(animated: true)
                   }
               } catch {
                   await MainActor.run {
                       self.setLoading(false)
                       self.showError(error.localizedDescription)
                   }
               }
           }
    }

    private func setLoading(_ loading: Bool) {
        registerButton.isEnabled = !loading
        registerButton.alpha = loading ? 0.6 : 1.0
    }

    private func showError(_ message: String) {
        let a = UIAlertController(title: "Hata", message: message, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
}

extension RegisterViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        switch textField {
        case nameField: emailField.becomeFirstResponder()
        case emailField: passwordField.becomeFirstResponder()
        case passwordField: password2Field.becomeFirstResponder()
        default:
            textField.resignFirstResponder()
            registerTapped(registerButton)
        }
        return true
    }
}
