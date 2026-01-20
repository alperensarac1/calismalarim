import UIKit

final class CheckOutViewController: UIViewController {

    @IBOutlet private weak var nameField: UITextField!
    @IBOutlet private weak var phoneField: UITextField!
    @IBOutlet private weak var addressTextView: UITextView!
    @IBOutlet private weak var totalLabel: UILabel!
    @IBOutlet private weak var confirmButton: UIButton!
    @IBOutlet weak var cityField: UITextField!
    private let totalText: String
    private var isLoading = false

    init(totalText: String) {
        self.totalText = totalText
        super.init(nibName: "CheckOutViewController", bundle: nil)
    }

    required init?(coder: NSCoder) { fatalError("XIB kullanıyoruz") }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if !AuthManager.shared.isLoggedIn {
            let vc = LoginViewController()
            navigationController?.pushViewController(vc, animated: true)
        }
    }

    private func setupUI() {
        view.backgroundColor = .systemBackground
        title = "Ödeme"

        totalLabel.text = totalText

        nameField.returnKeyType = .next

        phoneField.keyboardType = .phonePad
        phoneField.returnKeyType = .next

        cityField.returnKeyType = .done

        addressTextView.layer.cornerRadius = 12
        addressTextView.layer.borderWidth = 1
        addressTextView.layer.borderColor = UIColor.systemGray4.cgColor
        addressTextView.font = .systemFont(ofSize: 15)
        addressTextView.textContainerInset = UIEdgeInsets(top: 10, left: 8, bottom: 10, right: 8)
        
        if #available(iOS 15.0, *) {
            var cfg = UIButton.Configuration.filled()
            cfg.title = "Siparişi Onayla"
            cfg.cornerStyle = .medium
            confirmButton.configuration = cfg
        } else {
            confirmButton.setTitle("Siparişi Onayla", for: .normal)
        }
    }

    private func setLoading(_ loading: Bool) {
        isLoading = loading
        confirmButton.isEnabled = !loading
        confirmButton.alpha = loading ? 0.6 : 1.0
        view.isUserInteractionEnabled = !loading
    }

    private func isValidPhone(_ s: String) -> Bool {
        let digits = s.filter(\.isNumber)
        return digits.count >= 10
    }

    @IBAction private func confirmTapped(_ sender: UIButton) {
        if !AuthManager.shared.isLoggedIn {
                let vc = LoginViewController()
                navigationController?.pushViewController(vc, animated: true)
                return
            }

            let name = (nameField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
            let phone = (phoneField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
            let city = (cityField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
            let address = addressTextView.text.trimmingCharacters(in: .whitespacesAndNewlines)

            guard !name.isEmpty else { showError("Ad Soyad boş olamaz."); return }
            guard isValidPhone(phone) else { showError("Telefon numarası geçersiz."); return }
            guard !city.isEmpty else { showError("Şehir boş olamaz."); return }
            guard address.count >= 10 else { showError("Adres çok kısa."); return }

            guard !isLoading else { return }
            setLoading(true)

            // idempotency anahtarı (çift tıklama / tekrar istek için güzel olur)
            let idem = UUID().uuidString

            Task {
                do {
                    let res = try await ApiClient.shared.checkout(
                        addressLine1: address,
                        city: city,
                        addressName: name,
                        // addressLine2/district/postalCode yok şimdilik
                        idempotencyKey: idem
                    )

                    await MainActor.run {
                        self.setLoading(false)

                        let a = UIAlertController(
                            title: "Sipariş alındı ✅",
                            message: "Sipariş No: \(res.order_id)\nToplam: \(String(format: "₺ %.2f", res.total))",
                            preferredStyle: .alert
                        )
                        a.addAction(UIAlertAction(title: "Tamam", style: .default) { _ in
                            // Cart’a dön (Cart ekranı viewWillAppear’da tekrar getCart çekecek)
                            self.navigationController?.popViewController(animated: true)
                        })
                        self.present(a, animated: true)
                    }
                } catch {
                    await MainActor.run {
                        self.setLoading(false)
                        self.showError(error.localizedDescription)
                    }
                }
            }
        
    }

    private func showError(_ msg: String) {
        let a = UIAlertController(title: "Hata", message: msg, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
}
