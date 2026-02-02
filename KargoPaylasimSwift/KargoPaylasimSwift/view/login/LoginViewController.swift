
import UIKit

final class LoginViewController: UIViewController {

    @IBOutlet private weak var phoneTextField: UITextField!
    @IBOutlet private weak var passwordTextField: UITextField!
    @IBOutlet private weak var loginButton: UIButton!
    @IBOutlet private weak var registerButton: UIButton!
    @IBOutlet private weak var activityIndicator: UIActivityIndicatorView!

    
    private let tokenStore = TokenStore()
    private lazy var api = APIClient(baseURL: URL(string: "https://alperensaracdeneme.com/cargo/")!,
                                     tokenStore: tokenStore)
    private lazy var vm = AuthViewModel(api: api, tokenStore: tokenStore)

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "Giriş"
        activityIndicator.stopAnimating()

        // Token varsa direkt home
        if tokenStore.isLoggedIn() {
            goHome()
            return
        }

        vm.onLoginState = { [weak self] state in
            guard let self else { return }
            switch state {
            case .idle:
                self.setLoading(false)
            case .loading:
                self.setLoading(true)
            case .success:
                self.setLoading(false)
                self.goHome()
            case .error(let msg):
                self.setLoading(false)
                self.showAlert(title: "Hata", message: msg)
            }
        }
    }

    @IBAction private func loginTapped(_ sender: UIButton) {
        let phone = (phoneTextField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
             let pass = passwordTextField.text ?? ""

             if phone.isEmpty || pass.isEmpty {
                 showAlert(title: "Uyarı", message: "Telefon ve şifre gir.")
                 return
             }

             vm.login(phone: phone, password: pass)
    }

    @IBAction private func registerTapped(_ sender: UIButton) {
        let vc = RegisterViewController(nibName: "RegisterViewController", bundle: nil)
               navigationController?.pushViewController(vc, animated: true)
    }

    private func setLoading(_ loading: Bool) {
        if loading {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }
        loginButton.isEnabled = !loading
        registerButton.isEnabled = !loading
        phoneTextField.isEnabled = !loading
        passwordTextField.isEnabled = !loading
    }

    private func goHome() {
        let vc = HomeViewController(nibName: "HomeViewController", bundle: nil)
        navigationController?.setViewControllers([vc], animated: true)
    }

    private func showAlert(title: String, message: String) {
        let a = UIAlertController(title: title, message: message, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
}
