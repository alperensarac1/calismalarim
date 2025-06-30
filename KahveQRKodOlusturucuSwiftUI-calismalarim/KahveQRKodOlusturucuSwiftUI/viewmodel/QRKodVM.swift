//
//  QRKodVM.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 30.03.2025.
import Foundation
import SwiftUI
import CoreImage.CIFilterBuiltins

class QRKodVM: ObservableObject {
    @Published var dogrulamaKodu: String = ""
    @Published var qrKodGorseli: UIImage? = nil
    @Published var kalanSure: String = ""

    private var timer: Timer?
    private let context = CIContext()
    private let filter = CIFilter.qrCodeGenerator()
    
    func yeniKodUret()->String {
        let yeniKod = String(UUID().uuidString.prefix(6))
        print("🆕 Yeni doğrulama kodu üretildi: \(yeniKod)")
        dogrulamaKodu = yeniKod
        qrKodGorseli = generateQRCode(from: yeniKod)
        startTimer()
        return yeniKod
    }

    private func startTimer() {
        timer?.invalidate()
        var remainingTime = 90

        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            guard let self = self else { return }

            if remainingTime > 0 {
                self.kalanSure = "\(remainingTime) saniye"
                remainingTime -= 1
            } else {
                self.timer?.invalidate()
                self.yeniKodUret()
            }
        }
    }

    private func generateQRCode(from string: String) -> UIImage? {
        let data = Data(string.utf8)
        let context = CIContext()
        let filter = CIFilter(name: "CIQRCodeGenerator")!

        filter.setValue(data, forKey: "inputMessage")
        filter.setValue("H", forKey: "inputCorrectionLevel")

        if let outputImage = filter.outputImage {
           
            let transform = CGAffineTransform(scaleX: 10, y: 10)
            let scaledImage = outputImage.transformed(by: transform)

            if let cgImage = context.createCGImage(scaledImage, from: scaledImage.extent) {
                return UIImage(cgImage: cgImage)
            }
        }

        return nil
    }


    deinit {
        timer?.invalidate()
    }
}
