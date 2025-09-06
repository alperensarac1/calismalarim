import UIKit
import Combine

class GirisYapVC: UIViewController {
    
    @IBOutlet weak var etSifre: UITextField!
    @IBOutlet weak var etKullaniciAdi: UITextField!
    private let vm = LoginViewModel()
    private var bag = Set<AnyCancellable>()
    
    private var didNavigateAfterLogin = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        vm.$loginResult
            .receive(on: DispatchQueue.main)
            .sink { [weak self] res in
                guard let self, let res else { return }
                
                if res.success, let uid = res.userId, !self.didNavigateAfterLogin {
                    self.didNavigateAfterLogin = true
                    
                    // Toast göster ve kapandıktan sonra programatik push yap
                    self.showToast("Giriş başarılı!") { [weak self] in
                        guard let self = self else { return }
                        self.navigateToHome(userId: uid)
                    }
                    
                } else if res.success == false {
                    self.showToast(res.message ?? "Bilinmeyen hata")
                }
            }
            .store(in: &bag)
    }
    func navigateToHome(userId: Int) {
         // Storyboard ID: "AnasayfaVC" olmalı (Identity Inspector > Storyboard ID)
         guard let home = storyboard?.instantiateViewController(withIdentifier: "AnasayfaVC") as? AnasayfaVC else {
             assertionFailure("AnasayfaVC bulunamadı. Storyboard ID doğru mu?")
             return
         }
         home.userId = userId
         
         // Eğer şu an bir Alert gösteriliyorsa önce kapat, sonra push et
         if presentedViewController != nil {
             dismiss(animated: false) { [weak self] in
                 self?.navigationController?.pushViewController(home, animated: true)
             }
         } else {
             navigationController?.pushViewController(home, animated: true)
         }
     }
    @IBAction func btnGirisYap(_ sender: Any) {
        let u = etKullaniciAdi.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let p = etSifre.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !u.isEmpty, !p.isEmpty else {
            showToast("Tüm alanları doldurun")
            return
        }
        view.endEditing(true)
        vm.loginUser(username: u, password: p)
    }
    
    @IBAction func btnKayitOl(_ sender: Any) {
        // Programatik push (Storyboard ID: "KayitOlVC")
        if let register = storyboard?.instantiateViewController(withIdentifier: "KayitOlVC") as? KayitOlVC {
            navigationController?.pushViewController(register, animated: true)
        }
        // MARK: - Programatik geçiş (segue yok)
    
    }
}
        // MARK: - Tek-örnek toast helper (completion ile)
        extension UIViewController {
            private struct ToastHolder { static var toast: UIAlertController? = nil }

            /// Basit toast gibi alert. Aynı anda ikinci kez gösterilmesini engeller.
            func showToast(_ message: String, duration: TimeInterval = 1.2, then: (() -> Void)? = nil) {
                // Önceki açık toast varsa kapat
                if let presented = ToastHolder.toast {
                    presented.dismiss(animated: false)
                    ToastHolder.toast = nil
                }

                let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
                ToastHolder.toast = alert

                let presentBlock = { [weak self] in
                    guard let self = self else { return }
                    self.present(alert, animated: true)
                    DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                        alert.dismiss(animated: true) {
                            ToastHolder.toast = nil
                            then?()
                        }
                    }
                }

                if presentedViewController != nil {
                    dismiss(animated: false) { presentBlock() }
                } else {
                    presentBlock()
                }
            }
        }
