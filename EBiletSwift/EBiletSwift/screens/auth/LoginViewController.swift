//
//  LoginViewController.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation
import UIKit

/*
    LoginViewController

    XIB tabanlı giriş ekranıdır.

    Bu ekranda:
    - E-posta alınır
    - Şifre alınır
    - auth/login.php API'si çağrılır
    - Başarılı olursa UserDefaults içine session kaydedilir

    XIB içindeki elemanlar:
    - titleLabel
    - subtitleLabel
    - emailTextField
    - passwordTextField
    - loginButton
    - registerButton
    - activityIndicator
*/
final class LoginViewController: UIViewController {

    // MARK: - IBOutlet

    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var subtitleLabel: UILabel!

    @IBOutlet private weak var emailTextField: UITextField!
    @IBOutlet private weak var passwordTextField: UITextField!

    @IBOutlet private weak var loginButton: UIButton!
    @IBOutlet private weak var registerButton: UIButton!

    @IBOutlet private weak var activityIndicator: UIActivityIndicatorView!

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
        checkExistingSession()
    }

    // MARK: - Setup

    private func setupUI() {
        view.backgroundColor = UIColor(red: 245/255, green: 246/255, blue: 250/255, alpha: 1)

        title = "Giriş Yap"

        titleLabel.text = "Etkinlik Bileti"
        titleLabel.font = .boldSystemFont(ofSize: 30)
        titleLabel.textColor = UIColor(red: 15/255, green: 23/255, blue: 42/255, alpha: 1)
        titleLabel.textAlignment = .center

        subtitleLabel.text = "Etkinlikleri keşfet, biletini QR kodla kullan."
        subtitleLabel.font = .systemFont(ofSize: 15)
        subtitleLabel.textColor = UIColor(red: 100/255, green: 116/255, blue: 139/255, alpha: 1)
        subtitleLabel.textAlignment = .center

        setupTextField(
            emailTextField,
            placeholder: "E-posta",
            keyboardType: .emailAddress,
            isSecure: false
        )

        setupTextField(
            passwordTextField,
            placeholder: "Şifre",
            keyboardType: .default,
            isSecure: true
        )

        setupButton(
            loginButton,
            title: "Giriş Yap",
            backgroundColor: UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1)
        )

        registerButton.setTitle("Hesabın yok mu? Kayıt ol", for: .normal)
        registerButton.setTitleColor(
            UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1),
            for: .normal
        )

        activityIndicator.hidesWhenStopped = true
        activityIndicator.stopAnimating()
    }

    private func setupTextField(
        _ textField: UITextField,
        placeholder: String,
        keyboardType: UIKeyboardType,
        isSecure: Bool
    ) {
        textField.placeholder = placeholder
        textField.keyboardType = keyboardType
        textField.isSecureTextEntry = isSecure
        textField.autocapitalizationType = .none
        textField.autocorrectionType = .no

        textField.backgroundColor = UIColor(red: 238/255, green: 242/255, blue: 255/255, alpha: 1)
        textField.layer.cornerRadius = 12
        textField.layer.masksToBounds = true

        /*
            TextField içine soldan padding vermek için leftView kullanıyoruz.
        */
        let paddingView = UIView(frame: CGRect(x: 0, y: 0, width: 14, height: 44))
        textField.leftView = paddingView
        textField.leftViewMode = .always
    }

    private func setupButton(
        _ button: UIButton,
        title: String,
        backgroundColor: UIColor
    ) {
        button.setTitle(title, for: .normal)
        button.backgroundColor = backgroundColor
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .boldSystemFont(ofSize: 16)
        button.layer.cornerRadius = 12
        button.layer.masksToBounds = true
    }

    /*
        Kullanıcı daha önce giriş yaptıysa Home'a yönlendireceğiz.

        HomeViewController sonraki adımda eklenecek.
        Şimdilik sadece yorum olarak bırakıyoruz.
    */
    private func checkExistingSession() {
        if SessionManager.shared.isLoggedIn {
            let homeVC = HomeViewController()
            navigationController?.setViewControllers([homeVC], animated: false)
        }
    }

    // MARK: - Actions

    @IBAction private func loginButtonTapped(_ sender: UIButton) {
        loginUser()
    }

    @IBAction private func registerButtonTapped(_ sender: UIButton) {
        let registerVC = RegisterViewController()
        navigationController?.pushViewController(registerVC, animated: true)
    }

    // MARK: - Login

    private func loginUser() {
        let email = emailTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let password = passwordTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        guard !email.isEmpty else {
            showAlert(message: "E-posta zorunludur")
            return
        }

        guard email.isValidEmail else {
            showAlert(message: "Geçerli bir e-posta giriniz")
            return
        }

        guard !password.isEmpty else {
            showAlert(message: "Şifre zorunludur")
            return
        }

        guard password.count >= 6 else {
            showAlert(message: "Şifre en az 6 karakter olmalıdır")
            return
        }

        setLoading(true)

        APIService.shared.login(
            email: email,
            password: password
        ) { [weak self] result in
            guard let self else { return }

            self.setLoading(false)

            switch result {
            case .success(let response):

                guard response.success else {
                    self.showAlert(message: response.message)
                    return
                }

                guard let user = response.data else {
                    self.showAlert(message: "Kullanıcı bilgisi alınamadı")
                    return
                }

                SessionManager.shared.saveUser(user)

                self.showAlert(
                    title: "Başarılı",
                    message: "Giriş başarılı. Hoş geldin \(user.fullName)"
                ) {
                    let homeVC = HomeViewController()
                    self.navigationController?.setViewControllers([homeVC], animated: true)
                }

            case .failure(let error):
                self.showAlert(message: error.localizedDescription)
            }
        }
    }

    private func setLoading(_ isLoading: Bool) {
        loginButton.isEnabled = !isLoading
        registerButton.isEnabled = !isLoading

        if isLoading {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }
    }
}
