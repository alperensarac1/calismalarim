import UIKit
import Combine

class KayitOlVC: UIViewController {

    @IBOutlet weak var etSifre: UITextField!
    @IBOutlet weak var etKullaniciAdi: UITextField!
    private let vm = RegisterViewModel()
       private var bag = Set<AnyCancellable>()

       override func viewDidLoad() {
           super.viewDidLoad()

           vm.$registerResult
               .receive(on: DispatchQueue.main)
               .sink { [weak self] res in
                   guard let self, let res else { return }
                   if res.success {
                       self.showToast("Kayıt başarılı! Giriş yapabilirsiniz")
                       // ➜ 1) Geri dön (login’e)
                       self.navigationController?.popViewController(animated: true)

                       // ➜ 2) Segue ile login’e dönmek istersen:
                       // self.performSegue(withIdentifier: "backToLogin", sender: nil)
                   } else {
                       self.showToast("Hata: \(res.message)")
                   }
               }
               .store(in: &bag)

           vm.$isLoading
               .receive(on: DispatchQueue.main)
               .sink { [weak self] loading in
                   self?.view.isUserInteractionEnabled = !loading
               }
               .store(in: &bag)
       }



    @IBAction func btnGirisYap(_ sender: Any) {
        // Login ekranına dön
              navigationController?.popViewController(animated: true)
              // veya segue: performSegue(withIdentifier: "backToLogin", sender: nil)
    }
    @IBAction func btnKayitOl(_ sender: Any) {
        let u = etKullaniciAdi.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
               let p = etSifre.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
               guard !u.isEmpty, !p.isEmpty else {
                   showToast("Tüm alanları doldurun")
                   return
               }
               view.endEditing(true)
               vm.registerUser(username: u, password: p)
    }
    
}
