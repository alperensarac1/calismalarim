//
//  HaberCellCollectionViewCell.swift
//  HaberUygulamaSwift
//
//  Created by Alperen Saraç on 18.07.2025.
//

import UIKit

protocol HaberCellDelegate: AnyObject {
    func playButtonTapped(for haber: HaberModel)
}

class HaberCellCollectionViewCell: UICollectionViewCell {
    
    @IBOutlet weak var tvDevaminiOku: UILabel!
    @IBOutlet weak var tvHaberBaslik: UILabel!
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var videoView: UIView!
    @IBOutlet weak var btnPlay: UIButton!
    weak var delegate: HaberCellDelegate?
      var haber: HaberModel?

      override func awakeFromNib() {
          super.awakeFromNib()
          btnPlay.addTarget(self, action: #selector(playTapped), for: .touchUpInside)
      }

      @objc func playTapped() {
          guard let haber = haber else { return }
          delegate?.playButtonTapped(for: haber)
      }
}
