import UIKit

final class AddressCreateViewController: UIViewController {

    @IBOutlet private weak var etTitle: UITextField!
    @IBOutlet private weak var etCity: UITextField!
    @IBOutlet private weak var etDistrict: UITextField!
    @IBOutlet private weak var etNeighborhood: UITextField!
    @IBOutlet private weak var tvAddressLine: UITextView!
    @IBOutlet private weak var etPostal: UITextField!
    @IBOutlet private weak var btnSave: UIButton!
    @IBOutlet private weak var progress: UIActivityIndicatorView!

    // Home refresh için
    var onCreated: (() -> Void)?

    private let tokenStore = TokenStore()
    private lazy var api = APIClient(
        baseURL: URL(string: "https://alperensaracdeneme.com/cargo/")!,
        tokenStore: tokenStore
    )

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Adres Ekle"
        progress.stopAnimating()

        etPostal.keyboardType = .numberPad

        // TextView görünümü
        tvAddressLine.layer.cornerRadius = 10
        tvAddressLine.layer.borderWidth = 1
        tvAddressLine.layer.borderColor = UIColor.separator.cgColor
        tvAddressLine.textContainerInset = UIEdgeInsets(top: 10, left: 8, bottom: 10, right: 8)
    }

    @IBAction private func saveTapped(_ sender: UIButton) {
        let title = (etTitle.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let city = (etCity.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let dist = (etDistrict.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let neigh = (etNeighborhood.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let line = (tvAddressLine.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let postal = (etPostal.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)

        if title.isEmpty || city.isEmpty || dist.isEmpty || line.isEmpty {
            showAlert(title: "Uyarı", message: "Başlık, şehir, ilçe ve açık adres zorunlu.")
            return
        }

        setLoading(true)

        Task { @MainActor in
            do {
                let body = AddressCreateReq(
                    title: title,
                    city: city,
                    district: dist,
                    neighborhood: neigh,
                    address_line: line,
                    postal_code: postal
                )

                let res: ApiResp<AddressCreateData> = try await api.postJSON(
                    "address_create.php",
                    body: body,
                    resp: ApiResp<AddressCreateData>.self
                )

                guard res.ok else {
                    throw SimpleError(res.error ?? "Adres eklenemedi")
                }

                setLoading(false)

                
                onCreated?()

                navigationController?.popViewController(animated: true)

            } catch {
                setLoading(false)
                showAlert(title: "Hata", message: error.localizedDescription)
            }
        }
    }

    private func setLoading(_ loading: Bool) {
        if loading { progress.startAnimating() } else { progress.stopAnimating() }
        btnSave.isEnabled = !loading
        etTitle.isEnabled = !loading
        etCity.isEnabled = !loading
        etDistrict.isEnabled = !loading
        etNeighborhood.isEnabled = !loading
        tvAddressLine.isEditable = !loading
        etPostal.isEnabled = !loading
    }

    private func showAlert(title: String, message: String) {
        let a = UIAlertController(title: title, message: message, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
}

private struct SimpleError: LocalizedError {
    let message: String
    init(_ message: String) { self.message = message }
    var errorDescription: String? { message }
}
