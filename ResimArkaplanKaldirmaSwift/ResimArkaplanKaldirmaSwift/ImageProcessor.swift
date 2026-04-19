//
//  ImageProcessor.swift
//  ResimArkaplanKaldirmaSwift
//
//  Created by Alperen Saraç on 1.04.2026.
//

import Foundation
import UIKit

enum ImageProcessor {

    static func removeConnectedRegionByColor(
        source: UIImage,
        startX: Int,
        startY: Int,
        tolerance: CGFloat
    ) -> UIImage? {

        guard let cgImage = source.cgImage else { return nil }

        let width = cgImage.width
        let height = cgImage.height

        guard startX >= 0, startX < width, startY >= 0, startY < height else {
            return nil
        }

        let bytesPerPixel = 4
        let bytesPerRow = bytesPerPixel * width
        let bitsPerComponent = 8

        var rawData = [UInt8](repeating: 0, count: height * bytesPerRow)

        guard let colorSpace = CGColorSpace(name: CGColorSpace.sRGB) else { return nil }

        guard let context = CGContext(
            data: &rawData,
            width: width,
            height: height,
            bitsPerComponent: bitsPerComponent,
            bytesPerRow: bytesPerRow,
            space: colorSpace,
            bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
        ) else {
            return nil
        }

        context.draw(cgImage, in: CGRect(x: 0, y: 0, width: width, height: height))

        func pixelIndex(x: Int, y: Int) -> Int {
            y * bytesPerRow + x * bytesPerPixel
        }

        func visitIndex(x: Int, y: Int) -> Int {
            y * width + x
        }

        let targetIndex = pixelIndex(x: startX, y: startY)
        let targetR = rawData[targetIndex]
        let targetG = rawData[targetIndex + 1]
        let targetB = rawData[targetIndex + 2]

        var visited = [Bool](repeating: false, count: width * height)
        var queue: [(Int, Int)] = [(startX, startY)]

        while !queue.isEmpty {
            let (x, y) = queue.removeFirst()

            if x < 0 || x >= width || y < 0 || y >= height { continue }

            let vIndex = visitIndex(x: x, y: y)
            if visited[vIndex] { continue }
            visited[vIndex] = true

            let index = pixelIndex(x: x, y: y)

            let r = rawData[index]
            let g = rawData[index + 1]
            let b = rawData[index + 2]
            let a = rawData[index + 3]

            if a == 0 { continue }

            let distance = colorDistance(
                r1: r, g1: g, b1: b,
                r2: targetR, g2: targetG, b2: targetB
            )

            if distance <= tolerance {
                rawData[index + 3] = 0

                queue.append((x + 1, y))
                queue.append((x - 1, y))
                queue.append((x, y + 1))
                queue.append((x, y - 1))
            }
        }

        guard let outputCGImage = context.makeImage() else { return nil }
        return UIImage(cgImage: outputCGImage, scale: source.scale, orientation: .up)
    }

    private static func colorDistance(
        r1: UInt8, g1: UInt8, b1: UInt8,
        r2: UInt8, g2: UInt8, b2: UInt8
    ) -> CGFloat {
        let dr = CGFloat(Int(r1) - Int(r2))
        let dg = CGFloat(Int(g1) - Int(g2))
        let db = CGFloat(Int(b1) - Int(b2))
        return sqrt(dr * dr + dg * dg + db * db)
    }
}

extension UIImage {
    func normalizedImage() -> UIImage {
        if imageOrientation == .up { return self }

        UIGraphicsBeginImageContextWithOptions(size, false, scale)
        draw(in: CGRect(origin: .zero, size: size))
        let normalized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return normalized ?? self
    }
}
