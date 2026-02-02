import UIKit

final class CreateShipmentViewController: UIViewController {

    @IBOutlet private weak var etReceiverPhone: UITextField!
    @IBOutlet private weak var btnLookup: UIButton!
    @IBOutlet private weak var tvLookupResult: UILabel!
    @IBOutlet private weak var confirmRow: UIStackView!
    @IBOutlet private weak var btnConfirm: UIButton!
    @IBOutlet private weak var btnCancel: UIButton!

    @IBOutlet private weak var resultBox: UIView!
    @IBOutlet private weak var tvCode: UILabel!
    @IBOutlet private weak var tvExpires: UILabel!
    @IBOutlet private weak var btnCopyCode: UIButton!

    @IBOutlet private weak var progress: UIActivityIndicatorView!

    private let tokenStore = TokenStore()
    private lazy var api = APIClient(
        baseURL: URL(string: "https://alperensaracdeneme.com/cargo/")!,
        tokenStore: tokenStore
    )
    private lazy var vm = CreateShipmentViewModel(api: api)

    private var confirmedPhone: String?

    // Home refresh tetiklemek için:
    var onCreated: (() -> Void)?

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Yeni Gönderi"

        etReceiverPhone.keyboardType = .phonePad
        progress.stopAnimating()

        resetConfirm()

        vm.onLookup = { [weak self] st in
            guard let self else { return }
            switch st {
            case .idle:
                break
            case .loading:
                self.setLoading(true)
            case .success(let d):
                self.setLoading(false)
                self.tvLookupResult.text = "Bulunan: \(d.masked_first_name) \(d.masked_last_name) • Onaylıyor musun?"
                self.tvLookupResult.isHidden = false
                self.confirmRow.isHidden = false
                self.confirmedPhone = (self.etReceiverPhone.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
            case .error(let msg):
                self.setLoading(false)
                self.resetConfirm()
                // Android’deki özel mesaj:
                if msg.lowercased().contains("receiver address not found") || msg.uppercased().contains("RECEIVER_ADDRESS_MISSING") {
                    self.showAlert(title: "Uyarı", message: "Bu kullanıcı henüz adresini kaydetmemiş.")
                } else {
                    self.showAlert(title: "Hata", message: msg)
                }
            }
        }

        vm.onCreate = { [weak self] st in
            guard let self else { return }
            switch st {
            case .idle:
                break
            case .loading:
                self.setLoading(true)
            case .success(let d):
                self.setLoading(false)

                // Android’de finish + home refresh vardı
                self.onCreated?()

                // İstersen direkt geri dönelim (Android gibi)
                self.copyableSuccessAndClose(d)

            case .error(let msg):
                self.setLoading(false)
                if msg.lowercased().contains("receiver address not found") || msg.uppercased().contains("RECEIVER_ADDRESS_MISSING") {
                    self.showAlert(title: "Uyarı", message: "Bu kullanıcı henüz adresini kaydetmemiş.")
                } else {
                    self.showAlert(title: "Hata", message: msg)
                }
            }
        }
    }

    private func resetConfirm() {
        confirmedPhone = nil
        tvLookupResult.isHidden = true
        confirmRow.isHidden = true
        resultBox.isHidden = true
    }

    private func setLoading(_ loading: Bool) {
        if loading { progress.startAnimating() } else { progress.stopAnimating() }
        btnLookup.isEnabled = !loading
        btnConfirm.isEnabled = !loading
        btnCancel.isEnabled = !loading
        etReceiverPhone.isEnabled = !loading
    }

    @IBAction private func lookupTapped(_ sender: UIButton) {
        resetConfirm()

        let raw = (etReceiverPhone.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        if raw.count < 10 {
            showAlert(title: "Uyarı", message: "Telefonu kontrol et.")
            return
        }

        // Android gibi normalize etmek istersen:
        // - 05xx girerse +905xx'e çevir
        let normalized = PhoneUtil.normalizeTrToE164(raw)
        if !PhoneUtil.isLikelyTrPhoneE164(normalized) {
            showAlert(title: "Uyarı", message: "Telefon formatı hatalı. Örn: 05xx... veya +905xx...")
            return
        }

        etReceiverPhone.text = normalized

    
        vm.lookupReceiver(phoneE164: normalized)

    }

    @IBAction private func cancelTapped(_ sender: UIButton) {
        resetConfirm()
    }

    @IBAction private func confirmTapped(_ sender: UIButton) {
        guard let phone = confirmedPhone, !phone.isEmpty else {
            showAlert(title: "Uyarı", message: "Önce kişiyi bul.")
            return
        }
        vm.createShipment(receiverPhoneE164: phone)
    }

    private func copyableSuccessAndClose(_ d: CreateShipmentData) {
        let a = UIAlertController(
            title: "Gönderi Oluşturuldu",
            message: "Kod: \(d.pickup_code)\nSon geçerlilik: \(d.code_expires_at)",
            preferredStyle: .alert
        )

        a.addAction(UIAlertAction(title: "Kopyala", style: .default) { _ in
            UIPasteboard.general.string = d.pickup_code
            self.navigationController?.popViewController(animated: true)
        })
        a.addAction(UIAlertAction(title: "Tamam", style: .default) { _ in
            self.navigationController?.popViewController(animated: true)
        })

        present(a, animated: true)
    }

    private func showAlert(title: String, message: String) {
        let a = UIAlertController(title: title, message: message, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
}
