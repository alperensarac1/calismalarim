//
//  KayitOlVC.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import UIKit

class KayitOlVC: UIViewController {

    @IBOutlet weak var etSifre: UITextField!
    @IBOutlet weak var etKullaniciAdi: UITextField!
    @IBOutlet weak var etEmail: UITextField!
    
    private let viewModel = KayitViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
    @IBAction func btnKayitOl(_ sender: Any) {
        
        guard let username = etKullaniciAdi.text, !username.isEmpty,
                      let email = etEmail.text, !email.isEmpty,
                      let password = etSifre.text, !password.isEmpty else {
                    showAlert(title: "Hata", message: "Tüm alanları doldurun")
                    return
                }

                viewModel.register(username: username, password: password, email: email) { response in
                    if response.success {
                        self.showAlert(title: "Başarılı", message: response.message)
                        self.navigationController?.popViewController(animated: true)
                    } else {
                        self.showAlert(title: "Hata", message: response.message)
                    }
                }
        
    }
    @IBAction func btnGirisYap(_ sender: Any) {
        navigationController?.popViewController(animated: true)
    }
    
}
