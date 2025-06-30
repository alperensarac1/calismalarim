//
//  NotDefteriDetaysayfaVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 27.01.2025.
//

import UIKit


class NotDefteriDetaysayfaVC: UIViewController {

    var gelenNot: Not? // Gönderilen Not
    var notDefteriDao: NotDefteriDAO?

    @IBOutlet weak var etIcerik: UITextField!
    @IBOutlet weak var etBaslik: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        DispatchQueue.main.async {
            self.notDefteriDao = NotDefteriDAO()
        }

        // Eğer düzenlenmek üzere bir not varsa, alanları doldur
        if let not = gelenNot {
            etBaslik.text = not.not_baslik
            etIcerik.text = not.not_icerik
        } else {
            // Yeni not ekleniyorsa alanları temizle
            etBaslik.text = ""
            etIcerik.text = ""
        }
    }
    
    @IBAction func btnKaydet(_ sender: Any) {
        if let not = gelenNot {
            // Var olan notu güncelle
            notDefteriDao?.notGuncelle(not: not, yeniBaslik: etBaslik.text ?? "", yeniIcerik: etIcerik.text ?? "")
        } else {
            // Yeni not ekle
            notDefteriDao?.notEkle(baslik: etBaslik.text ?? "", icerik: etIcerik.text ?? "")
        }
        // Kaydetme işleminden sonra geri dön
        navigationController?.popViewController(animated: true)
    }
}
