//
//  GirisEkraniVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 2.01.2025.
//

import UIKit

class GirisEkraniVC: UIViewController {

    @IBOutlet weak var imgSifreGoster: UIImageView!
    @IBOutlet weak var etSifre: UITextField!
    var kullaniciBilgileri:KullaniciBilgileri?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        kullaniciBilgileri = KullaniciBilgileri()
        // Do any additional setup after loading the view.
        setupPasswordToggle(for: etSifre, with: imgSifreGoster)
    }
   
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        self.performTransition(segue: segue, withIdentifier: "toAnasayfa", viewControllerType: AnaekranVC.self)
        self.performTransition(segue: segue, withIdentifier: "toGuvenlikSorusu", viewControllerType: GuvenlikSorusuVC.self)
    }
    
    @IBAction func btnSifremiUnuttum(_ sender: Any) {
        
            performSegue(withIdentifier: "toGuvenlikSorusu", sender: nil)
        
    }
    
    
    @IBAction func btniGirisYap(_ sender: Any) {
        guard let sifre = etSifre.text,!etSifre.text!.isEmpty else{
            let alert = UIAlertController(title: "Hata", message: "Şifre alanı boş bırakılamaz.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Tamam", style: .default))
            self.present(alert, animated: true)
            return
        }
        if  kullaniciBilgileri!.sifreSorgulama(dogrulanacakSifre: sifre) {
            performSegue(withIdentifier: "toAnasayfa", sender: nil)
            
           
    } else {
            // Şifreler eşleşmiyor
            let alert = UIAlertController(title: "Hata", message: "Şifreler eşleşmiyor.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Tamam", style: .default))
            self.present(alert, animated: true)
    }
    }
    

}
