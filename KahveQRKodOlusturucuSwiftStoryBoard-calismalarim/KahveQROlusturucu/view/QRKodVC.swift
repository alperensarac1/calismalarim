//
//  QRKodVC.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 28.03.2025.
//

import UIKit

class QRKodVC: UIViewController {
    @IBOutlet weak var tvKalanSure: UILabel!
    private let qrKodOlusturVM = QRKodVM()
    @IBOutlet weak var imgQR: UIImageView!
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        imgQR.image = generateQRCode(from: qrKodOlusturVM.yeniKodUret())
        imgQR.layer.cornerRadius = 20
        observeViewModel()
        
        
    }
    
    
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destination.
     // Pass the selected object to the new view controller.
     }
     */
    func generateQRCode(from string: String) -> UIImage? {
        let data = string.data(using: String.Encoding.ascii)
        
        if let filter = CIFilter(name: "CIQRCodeGenerator") {
            filter.setValue(data, forKey: "inputMessage")
            filter.setValue("H", forKey: "inputCorrectionLevel") // Hata düzeltme seviyesi (L, M, Q, H)
            
            if let outputImage = filter.outputImage {
                let transform = CGAffineTransform(scaleX: 10, y: 10)
                let scaledImage = outputImage.transformed(by: transform)
                return UIImage(ciImage: scaledImage)
            }
        }
        return nil
    }
    private func observeViewModel() {
        qrKodOlusturVM.dogrulamaKoduUpdated = { [weak self] yeniKod in
            DispatchQueue.main.async {
                print("Yeni doğrulama kodu alındı: \(yeniKod)")
                var olusturulanKod = self?.qrKodOlusturVM.koduOlustur(dogrulamaKodu: yeniKod)
                self?.qrKodOlusturVM.qrKodOlustur(dogrulamaKodu: yeniKod)
                
                
                
            }
        }
        
        qrKodOlusturVM.qrKodUpdated = { [weak self] bitmap in
            DispatchQueue.main.async {
                self?.imgQR.image = bitmap
            }
        }
        
        qrKodOlusturVM.kalanSureUpdated = { [weak self] kalanSure in
            DispatchQueue.main.async {
                self?.tvKalanSure.text = "Yenileme için kalan süre: \(kalanSure)"
            }
        }
    }
}

