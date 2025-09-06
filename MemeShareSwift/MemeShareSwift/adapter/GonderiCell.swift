//
//  GonderiCell.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 30.08.2025.
//

import UIKit
import AVKit

class GonderiCell: UICollectionViewCell {
    
    
    
    @IBAction func btnOynatTapped(_ sender: Any) {
        onPlayTapped?()
    }
    @IBOutlet weak var btnOynat: UIButton!
    
    @IBOutlet weak var tvTarih: UILabel!
    @IBOutlet weak var tvKullaniciAdi: UILabel!
    @IBOutlet weak var videoGonderi: UIView!
    @IBOutlet weak var imgGonderi: UIImageView!
    // Video oynatma için closure
     var onPlayTapped: (() -> Void)?
     
     override func awakeFromNib() {
         super.awakeFromNib()
         imgGonderi.contentMode = .scaleAspectFill
         imgGonderi.clipsToBounds = true
         videoGonderi.isHidden = true
         btnOynat.isHidden = true
     }
    
    func configure(with model: GonderiModel, baseURL: URL) {
           tvTarih.text = model.uploadedAt
           tvKullaniciAdi.text = "Kullanıcı #\(model.userId)"
           
           let fullUrl = baseURL.appendingPathComponent(model.mediaUrl)
           
           if model.mediaType == "image" {
               imgGonderi.isHidden = false
               btnOynat.isHidden = true
               videoGonderi.isHidden = true
               
               imgGonderi.setRemoteImage(url: fullUrl)
               
           } else if model.mediaType == "video" {
               imgGonderi.isHidden = false
               btnOynat.isHidden = false
               videoGonderi.isHidden = true
               
               // Basitçe thumbnail almak için AVAsset
               DispatchQueue.global().async {
                   let asset = AVAsset(url: fullUrl)
                   let generator = AVAssetImageGenerator(asset: asset)
                   generator.appliesPreferredTrackTransform = true
                   let time = CMTimeMake(value: 1, timescale: 1)
                   if let cgImage = try? generator.copyCGImage(at: time, actualTime: nil) {
                       let thumbnail = UIImage(cgImage: cgImage)
                       DispatchQueue.main.async {
                           self.imgGonderi.image = thumbnail
                       }
                   }
               }
           } else {
               imgGonderi.image = nil
               btnOynat.isHidden = true
               videoGonderi.isHidden = true
           }
       }
}
extension UIImageView {
    func setRemoteImage(url: URL) {
        image = nil
        URLSession.shared.dataTask(with: url) { data, _, _ in
            if let data = data, let img = UIImage(data: data) {
                DispatchQueue.main.async {
                    self.image = img
                }
            }
        }.resume()
    }
}
