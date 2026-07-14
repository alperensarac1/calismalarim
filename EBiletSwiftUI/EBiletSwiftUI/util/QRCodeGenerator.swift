//
//  QRCodeGenerator.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI
import CoreImage
import CoreImage.CIFilterBuiltins

/*
    QRCodeGenerator

    SwiftUI tarafında QR kod üretmek için kullanacağız.

    CoreImage içinde hazır QR filtresi vardır:
    CIFilter.qrCodeGenerator()

    Bu sınıf:
    - String alır
    - QR kod UIImage üretir
    - SwiftUI Image olarak ekranda gösterebiliriz
*/
final class QRCodeGenerator {

    static let shared = QRCodeGenerator()

    private let context = CIContext()
    private let filter = CIFilter.qrCodeGenerator()

    private init() {}

    func generate(from text: String) -> UIImage? {
        let data = Data(text.utf8)

        filter.message = data
        filter.correctionLevel = "Q"

        guard let outputImage = filter.outputImage else {
            return nil
        }

        /*
            QR kod ilk üretildiğinde küçük olur.
            Büyütürken kalite bozulmasın diye transform kullanıyoruz.
        */
        let scaledImage = outputImage.transformed(
            by: CGAffineTransform(scaleX: 12, y: 12)
        )

        guard let cgImage = context.createCGImage(
            scaledImage,
            from: scaledImage.extent
        ) else {
            return nil
        }

        return UIImage(cgImage: cgImage)
    }
}
