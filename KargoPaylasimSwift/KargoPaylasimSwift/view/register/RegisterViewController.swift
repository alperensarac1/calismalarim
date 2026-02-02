import UIKit

final class RegisterViewController: UIViewController {
    
    
    @IBOutlet private weak var cardView: UIView!


    @IBOutlet private weak var etFirst: UITextField!
    @IBOutlet private weak var etLast: UITextField!
    @IBOutlet private weak var etPhone: UITextField!
    @IBOutlet private weak var etTc: UITextField!
    @IBOutlet private weak var etPassword: UITextField!

    @IBOutlet private weak var etAddressTitle: UITextField!
    @IBOutlet private weak var etCity: UITextField!
    @IBOutlet private weak var etDistrict: UITextField!
    @IBOutlet private weak var etNeighborhood: UITextField!
    @IBOutlet private weak var tvAddressLine: UITextView!
    @IBOutlet private weak var etPostal: UITextField!

    @IBOutlet private weak var btnRegister: UIButton!
    @IBOutlet private weak var progress: UIActivityIndicatorView!
    @IBOutlet private weak var tvBackLogin: UIButton!

    private let tokenStore = TokenStore()
    private lazy var api = APIClient(
        baseURL: URL(string: "https://alperensaracdeneme.com/cargo/")!,
        tokenStore: tokenStore
    )
    private lazy var vm = RegisterFlowViewModel(api: api, tokenStore: tokenStore)

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Kayıt"

        progress.stopAnimating()

        // TextView görünümü
        tvAddressLine.layer.cornerRadius = 10
        tvAddressLine.layer.borderWidth = 1
        tvAddressLine.layer.borderColor = UIColor.separator.cgColor

        // Klavye tipleri
        etPhone.keyboardType = .phonePad
        etTc.keyboardType = .numberPad
        etPostal.keyboardType = .numberPad
        etPassword.isSecureTextEntry = true

        vm.onState = { [weak self] st in
            guard let self else { return }
            switch st {
            case .idle:
                self.setLoading(false)
            case .loading:
                self.setLoading(true)
            case .success:
                self.setLoading(false)
                self.showAlert(title: "Başarılı", message: "Kayıt tamamlandı. Giriş yapabilirsin.") {
                    self.navigationController?.popViewController(animated: true)
                }
            case .error(let msg):
                self.setLoading(false)
                self.showAlert(title: "Hata", message: msg)
            }
        }
    }

    @IBAction private func registerTapped(_ sender: UIButton) {
        let first = (etFirst.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let last  = (etLast.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let phone = (etPhone.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let tc    = (etTc.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let pass  = etPassword.text ?? ""

        let fullName = "\(first) \(last)".trimmingCharacters(in: .whitespacesAndNewlines)

        // Address
        let addrTitle = (etAddressTitle.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let city = (etCity.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let district = (etDistrict.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let neighborhood = (etNeighborhood.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let addressLine = tvAddressLine.text.trimmingCharacters(in: .whitespacesAndNewlines)
        let postal = (etPostal.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)

        // Basit validasyon (Android ile aynı mantık)
        if first.isEmpty || last.isEmpty || phone.isEmpty || pass.isEmpty {
            showAlert(title: "Uyarı", message: "Kişisel bilgileri doldur.")
            return
        }
        if addrTitle.isEmpty || city.isEmpty || district.isEmpty || neighborhood.isEmpty || addressLine.isEmpty {
            showAlert(title: "Uyarı", message: "Adres bilgilerini doldur.")
            return
        }
        if !tc.isEmpty && tc.count != 11 {
            showAlert(title: "Uyarı", message: "TC Kimlik No 11 haneli olmalı.")
            return
        }

        vm.registerThenCreateAddress(
            fullName: fullName,
            phone: phone,
            password: pass,
            addressTitle: addrTitle,
            city: city,
            district: district,
            neighborhood: neighborhood,
            addressLine: addressLine,
            postal: postal
        )
    }

    @IBAction private func backTapped(_ sender: UIButton) {
        navigationController?.popViewController(animated: true)
    }

    private func setLoading(_ loading: Bool) {
        if loading { progress.startAnimating() } else { progress.stopAnimating() }
        btnRegister.isEnabled = !loading
        tvBackLogin.isEnabled = !loading
        view.isUserInteractionEnabled = !loading
    }

    private func showAlert(title: String, message: String, onOK: (() -> Void)? = nil) {
        let a = UIAlertController(title: title, message: message, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default) { _ in onOK?() })
        present(a, animated: true)
    }
}
