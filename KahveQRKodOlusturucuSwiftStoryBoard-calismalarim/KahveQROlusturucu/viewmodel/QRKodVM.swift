//
//  QRKodVM.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 30.03.2025.
//

import Foundation
import UIKit

class QRKodVM {

    var dogrulamaKoduUpdated: ((String) -> Void)?
    var qrKodUpdated: ((UIImage?) -> Void)?
    var kalanSureUpdated: ((String) -> Void)?
    var kahveServisDao : Service
    
    init(){
        kahveServisDao = ServiceImpl.getInstance()
    }

    private var timer: Timer?
    
    func yeniKodUret()->String {
        let yeniKod = UUID().uuidString.prefix(6)
        print("Yeni doğrulama kodu üretildi: \(yeniKod)")
        koduOlustur(dogrulamaKodu: String(yeniKod))
        dogrulamaKoduUpdated?(String(yeniKod))
        return String(yeniKod)
        
    }

    func qrKodOlustur(dogrulamaKodu: String) {
        guard let qrImage = generateQRCode(from: dogrulamaKodu) else {
            print("QR kod oluşturulamadı.")
            return
        }
        qrKodUpdated?(qrImage)
    }

    func koduOlustur(dogrulamaKodu: String) {
        print("Kod kaydedildi: \(dogrulamaKodu)")
        startTimer()
    }

    private func startTimer() {
        timer?.invalidate()
        var remainingTime = 90

        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            if remainingTime > 0 {
                self?.kalanSureUpdated?("\(remainingTime) saniye")
                remainingTime -= 1
            } else {
                self?.timer?.invalidate()
                self?.yeniKodUret()
            }
        }
    }

    private func generateQRCode(from string: String) -> UIImage? {
        let data = string.data(using: .ascii)
        guard let filter = CIFilter(name: "CIQRCodeGenerator") else { return nil }
        filter.setValue(data, forKey: "inputMessage")
        filter.setValue("H", forKey: "inputCorrectionLevel")
        
        guard let ciImage = filter.outputImage else { return nil }
        let transformedImage = ciImage.transformed(by: CGAffineTransform(scaleX: 10, y: 10))
        
        return UIImage(ciImage: transformedImage)
    }
}
