//
//  Extensions.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 19.07.2025.
//

import Foundation
import UIKit
import AVKit
import AVFoundation

func playVideo(from urlString: String, on viewController: UIViewController) {
    guard let url = URL(string: urlString) else {
        print("Geçersiz URL: \(urlString)")
        return
    }

    let player = AVPlayer(url: url)
    let playerViewController = AVPlayerViewController()
    playerViewController.player = player

    viewController.present(playerViewController, animated: true) {
        player.play()
    }
}
func getThumbnailImage(forUrl url: URL, completion: @escaping (UIImage?) -> Void) {
    DispatchQueue.global().async {
        let asset = AVAsset(url: url)
        let imageGenerator = AVAssetImageGenerator(asset: asset)
        imageGenerator.appliesPreferredTrackTransform = true

        let time = CMTime(seconds: 1.0, preferredTimescale: 600)
        if let cgImage = try? imageGenerator.copyCGImage(at: time, actualTime: nil) {
            let thumbnail = UIImage(cgImage: cgImage)
            DispatchQueue.main.async {
                completion(thumbnail)
            }
        } else {
            DispatchQueue.main.async {
                completion(nil)
            }
        }
    }
}
