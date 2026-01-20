import UIKit

final class LoginViewController: UIViewController {

    @IBOutlet private weak var emailField: UITextField!
    @IBOutlet private weak var passwordField: UITextField!
    @IBOutlet private weak var loginButton: UIButton!
    @IBOutlet private weak var goRegisterButton: UIButton!

    init() {
        super.init(nibName: "LoginViewController", bundle: nil)
    }
    required init?(coder: NSCoder) { fatalError("XIB kullanıyoruz") }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }

    private func setupUI() {
        view.backgroundColor = .systemBackground
        title = "Giriş"

        emailField.keyboardType = .emailAddress
        emailField.autocapitalizationType = .none
        emailField.autocorrectionType = .no
        emailField.returnKeyType = .next

        passwordField.isSecureTextEntry = true
        passwordField.returnKeyType = .done

        emailField.delegate = self
        passwordField.delegate = self

        if #available(iOS 15.0, *) {
            var cfg = UIButton.Configuration.filled()
            cfg.title = "Giriş Yap"
            cfg.cornerStyle = .large
            loginButton.configuration = cfg
        } else {
            loginButton.setTitle("Giriş Yap", for: .normal)
        }
    }

    @IBAction private func loginTapped(_ sender: UIButton) {
        let email = (emailField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
          let pass = (passwordField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)

          guard !email.isEmpty else { showError("E-posta boş olamaz."); return }
          guard pass.count >= 4 else { showError("Şifre en az 4 karakter olmalı."); return }

          setLoading(true)

          Task {
              do {
                  let res = try await ApiClient.shared.login(email: email, password: pass)
                  AuthManager.shared.setSession(token: res.token, userId: res.user_id)

                  await MainActor.run {
                      self.setLoading(false)
                      self.navigationController?.popViewController(animated: true)
                  }
              } catch {
                  await MainActor.run {
                      self.setLoading(false)
                      self.showError(error.localizedDescription)
                  }
              }
          }
    }

    @IBAction private func goRegisterTapped(_ sender: UIButton) {
        let vc = RegisterViewController()
        navigationController?.pushViewController(vc, animated: true)
    }

    private func setLoading(_ loading: Bool) {
        loginButton.isEnabled = !loading
        loginButton.alpha = loading ? 0.6 : 1.0
    }

    private func showError(_ message: String) {
        let a = UIAlertController(title: "Hata", message: message, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
}

extension LoginViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if textField == emailField {
            passwordField.becomeFirstResponder()
        } else {
            textField.resignFirstResponder()
            loginTapped(loginButton)
        }
        return true
    }
}
