//
//  RegistrationDialog.swift
//  ChatSwift
//
//  Created by Alperen Saraç on 4.07.2025.
//

import Foundation
import UIKit

extension UIViewController {
    func showRegistrationDialog() {
        let alert = UIAlertController(title: "Kayıt Ol", message: nil, preferredStyle: .alert)
        
        alert.addTextField { textField in
            textField.placeholder = "Ad"
        }
        
        alert.addTextField { textField in
            textField.placeholder = "Numara"
            textField.keyboardType = .numberPad
        }
        
        let kayitAction = UIAlertAction(title: "Kayıt Ol", style: .default) { _ in
            let ad = alert.textFields?[0].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            let numara = alert.textFields?[1].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            
            guard !ad.isEmpty, !numara.isEmpty else {
                self.showMessage("Boş alan bırakma!")
                return
            }
            
            ApiService.shared.kullaniciKayit(ad: ad, numara: numara) { result in
                DispatchQueue.main.async {
                    switch result {
                    case .success(let response):
                        if response.success, let id = response.id {
                            AppConfig.kullaniciId = id
                            PrefManager().kaydetKullaniciId(id: id)
                            self.showMessage("Kayıt başarılı!")
                        } else {
                            self.showMessage(response.error ?? "Kayıt başarısız")
                        }
                    case .failure(let error):
                        self.showMessage("Hata: \(error.localizedDescription)")
                    }
                }
            }
        }
        
        let iptalAction = UIAlertAction(title: "İptal", style: .cancel)

        alert.addAction(kayitAction)
        alert.addAction(iptalAction)
        
        self.present(alert, animated: true)
    }
    
    func showMessage(_ message: String) {
        let bilgi = UIAlertController(title: "Bilgi", message: message, preferredStyle: .alert)
        bilgi.addAction(UIAlertAction(title: "Tamam", style: .default))
        self.present(bilgi, animated: true)
    }
}
