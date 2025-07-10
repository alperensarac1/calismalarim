import UIKit
import Combine

class ViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var tableView: UITableView!
    private var kisiler: [KonusulanKisi] = []
    private let viewModel = SohbetListesiViewModel()
    private var bag = Set<AnyCancellable>()

    override func viewDidLoad() {
        super.viewDidLoad()

        

        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()

        viewModel.$konusulanKisiler
            .receive(on: DispatchQueue.main)
            .sink { [weak self] yeniListe in
                self?.kisiler = yeniListe
                self?.tableView.reloadData()
            }
            .store(in: &bag)

        viewModel.$hataMesaji
            .compactMap { $0 }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] mesaj in
                self?.showAlert(mesaj)
            }
            .store(in: &bag)

        viewModel.sohbetListesiniBaslat(kullaniciId: AppConfig.kullaniciId)
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        if AppConfig.kullaniciId == -1 {
            AppConfig.kullaniciId = PrefManager().getirKullaniciId()
            if AppConfig.kullaniciId == -1 {
                showRegistrationAlert()
            }
        }
    }


    private func showRegistrationAlert() {
        let alert = UIAlertController(title: "Kayıt Ol", message: "İsminizi ve numaranızı giriniz", preferredStyle: .alert)
        alert.addTextField { $0.placeholder = "Ad" }
        alert.addTextField {
            $0.placeholder = "Numara"
            $0.keyboardType = .numberPad
        }

        let kayitAction = UIAlertAction(title: "Kayıt Ol", style: .default) { _ in
            let ad = alert.textFields?[0].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            let numara = alert.textFields?[1].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

            guard !ad.isEmpty, !numara.isEmpty else {
                self.showAlert("Boş alan bırakmayınız")
                self.showRegistrationAlert()
                return
            }

            ApiService.shared.kullaniciKayit(ad: ad, numara: numara) { result in
                DispatchQueue.main.async {
                    switch result {
                    case .success(let resp) where resp.success && resp.id != nil:
                        AppConfig.kullaniciId = resp.id!
                        PrefManager().kaydetKullaniciId(id: resp.id!)
                        self.viewDidLoad() // kayıt tamamlandıktan sonra yeniden başlat
                    case .success(let resp):
                        self.showAlert(resp.error ?? "Kayıt başarısız")
                        self.showRegistrationAlert()
                    case .failure(let err):
                        self.showAlert("Hata: \(err.localizedDescription)")
                        self.showRegistrationAlert()
                    }
                }
            }
        }

        alert.addAction(kayitAction)
        alert.addAction(UIAlertAction(title: "Çık", style: .destructive) { _ in
            exit(0)
        })

        self.present(alert, animated: true)
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        kisiler.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let kisi = kisiler[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "KonusulanlarCell", for: indexPath) as! KonusulanlarCell
        cell.configure(with: kisi)
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let secilen = kisiler[indexPath.row]
        kisiyeGoreKonusmayaGit(numara: secilen.numara)
    }

    private func kisiyeGoreKonusmayaGit(numara: String) {
        ApiService.shared.kullanicilariGetir { [weak self] result in
            switch result {
            case .success(let response):
                if let kisi = response.kullanicilar.first(where: { $0.numara == numara }) {
                    let vc = UIStoryboard(name: "Main", bundle: nil)
                        .instantiateViewController(withIdentifier: "SingleChatViewController") as! SingleChatVC
                    vc.aliciId = kisi.id
                    vc.aliciAd = kisi.ad
                    self?.navigationController?.pushViewController(vc, animated: true)
                } else {
                    self?.showAlert("Bu numara kayıtlı değil")
                }
            case .failure(let err):
                self?.showAlert("Sunucu hatası: \(err.localizedDescription)")
            }
        }
    }

    @IBAction func btnKonusmaBaslat(_ sender: Any) {
        let alert = UIAlertController(title: "Yeni Konuşma", message: "Numara giriniz", preferredStyle: .alert)
        alert.addTextField {
            $0.placeholder = "Alıcı numarası"
            $0.keyboardType = .numberPad
        }

        alert.addAction(UIAlertAction(title: "Başlat", style: .default) { _ in
            let numara = alert.textFields?.first?.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            if numara.isEmpty {
                self.showAlert("Numara boş olamaz")
            } else {
                self.kisiyeGoreKonusmayaGit(numara: numara)
            }
        })

        alert.addAction(UIAlertAction(title: "İptal", style: .cancel))
        present(alert, animated: true)
    }

    private func showAlert(_ msg: String) {
        let alert = UIAlertController(title: nil, message: msg, preferredStyle: .alert)
        present(alert, animated: true)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            alert.dismiss(animated: true)
        }
    }
}
