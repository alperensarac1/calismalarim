//
//  TelefonAnasayfaVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 29.01.2025.
//

import UIKit

class TelefonAnasayfaVC: UIViewController {

    @IBOutlet weak var etNumara: UITextField!
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
    }
    

    @IBAction func btnAra(_ sender: Any) {
        showCallAlert(phoneNumber: etNumara.text!, viewController: self)
        
    }
    @IBAction func btnMesajGonde(_ sender: Any) {
        guard let number = etNumara.text, !number.isEmpty else {
                    print("Geçersiz numara")
                    return
                }
                
                let smsURL = "sms:\(number)"
                
                if let url = URL(string: smsURL), UIApplication.shared.canOpenURL(url) {
                    UIApplication.shared.open(url, options: [:], completionHandler: nil)
                } else {
                    print("Bu cihaz SMS gönderemiyor.")
                }
    }
    @IBAction func btnRehbereEkle(_ sender: Any) {
        var rehberDao = RehberDao()
        let alert = UIAlertController(title: "Rehbere Ekle", message: "Rehbere eklenecek kişinin ismini tuşlayınız.", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Kaydet", style: .destructive) { _ in
            alert.addTextField{tf1 in
                tf1.placeholder = "Kişinin ismi"
            }
            let alinanAd = (alert.textFields![0] as UITextField).text!
            rehberDao.kisiEkle(kisi_ad: alinanAd, kisi_tel: self.etNumara.text!)
        })
        self.present(alert, animated: true)
                
    }
    
    func showCallAlert(phoneNumber: String, viewController: UIViewController) {
        let alert = UIAlertController(title: "Arama Yap", message: "\(phoneNumber) numarasını aramak istiyor musunuz?", preferredStyle: .alert)
        
        alert.addAction(UIAlertAction(title: "Evet", style: .default, handler: { _ in
            UIApplication.shared.makeCall(to: phoneNumber)
        }))
        
        alert.addAction(UIAlertAction(title: "İptal", style: .cancel, handler: nil))
        
        viewController.present(alert, animated: true, completion: nil)
    }

}
