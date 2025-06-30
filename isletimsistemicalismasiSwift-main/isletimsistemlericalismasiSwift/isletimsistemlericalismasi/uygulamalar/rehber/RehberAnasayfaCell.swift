//
//  RehberAnasayfaCell.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 28.01.2025.
//

import UIKit

class RehberAnasayfaCell: UITableViewCell {

    @IBOutlet weak var tvKisi: UILabel!
    var numara:String!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    @IBAction func btnAra(_ sender: Any) {
        guard let viewController = self.getViewController() else {
                    print("ViewController bulunamadı")
                    return
        }

                let alert = UIAlertController(title: "Arama Yap", message: "\(numara ?? "Bilinmeyen numara") numarasını aramak istiyor musunuz?", preferredStyle: .alert)
                
                alert.addAction(UIAlertAction(title: "Evet", style: .default, handler: { _ in
                    UIApplication.shared.makeCall(to: self.numara ?? "")
                }))
                
                alert.addAction(UIAlertAction(title: "İptal", style: .cancel, handler: nil))
                
                viewController.present(alert, animated: true, completion: nil)
    }
    @IBAction func btnMesajGonder(_ sender: Any) {
        guard let number = numara, !number.isEmpty else {
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
    
    
    func showCallAlert(phoneNumber: String, viewController: UIViewController) {
        let alert = UIAlertController(title: "Arama Yap", message: "\(phoneNumber) numarasını aramak istiyor musunuz?", preferredStyle: .alert)
        
        alert.addAction(UIAlertAction(title: "Evet", style: .default, handler: { _ in
            UIApplication.shared.makeCall(to: phoneNumber)
        }))
        
        alert.addAction(UIAlertAction(title: "İptal", style: .cancel, handler: nil))
        
        viewController.present(alert, animated: true, completion: nil)
    }

}
       

