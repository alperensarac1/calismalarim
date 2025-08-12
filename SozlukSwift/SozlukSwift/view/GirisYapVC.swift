//
//  GirisYapVC.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import UIKit

class GirisYapVC: UIViewController {

    @IBOutlet weak var etSifre: UITextField!
    @IBOutlet weak var etKullaniciAdi: UITextField!
    
    private let viewModel = GirisViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if SessionManager.shared.isLoggedIn() {
            performSegue(withIdentifier: "toGundem", sender: nil)
        }
    }
    


  
    @IBAction func btnGirisYap(_ sender: Any) {
        
        guard let username = etKullaniciAdi.text, !username.isEmpty,
                 let password = etSifre.text, !password.isEmpty else {
               showAlert(title: "Hata", message: "Kullanıcı adı ve şifre boş olamaz")
               return
           }

           viewModel.login(username: username, password: password) { [weak self] success, message, userId in
               guard let self = self else { return }
               if success, let uid = userId {
                   SessionManager.shared.saveUserSession(userId: uid, username: username)
                   self.performSegue(withIdentifier: "toGundem", sender: nil)
               } else {
                   self.showAlert(title: "Hata", message: message ?? "Giriş başarısız")
               }
           }
        
    }
    @IBAction func btnKayitOl(_ sender: Any) {
        performSegue(withIdentifier: "toKayitOl", sender: nil)
    }
}
