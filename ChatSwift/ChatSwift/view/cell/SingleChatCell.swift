//
//  SingleChatCell.swift
//  ChatSwift
//
//  Created by Alperen Saraç on 3.07.2025.
//

import UIKit
import SDWebImage
class SingleChatCell: UITableViewCell {

    @IBOutlet weak var tvTarih: UILabel!
    @IBOutlet weak var tvMesaj: UILabel!
    @IBOutlet weak var ivMesajResim: UIImageView!
    @IBOutlet weak var bubbleView: UIView!
    @IBOutlet weak var leadingConstraint: NSLayoutConstraint!
    @IBOutlet weak var trailingConstraint: NSLayoutConstraint!
    
    override func awakeFromNib() {
           super.awakeFromNib()
           bubbleView.layer.cornerRadius = 12
           bubbleView.clipsToBounds = true
           ivMesajResim.layer.cornerRadius = 8
           ivMesajResim.clipsToBounds = true
           ivMesajResim.contentMode = .scaleAspectFill
       }

       func configure(with mesaj: Mesaj) {
           let benimId = AppConfig.kullaniciId
           let benimMesajim = mesaj.gonderen_id == benimId

           // Renk ve hizalama
           if benimMesajim {
               bubbleView.backgroundColor = UIColor.systemBlue
               leadingConstraint.constant = 60
               trailingConstraint.constant = 8
           } else {
               bubbleView.backgroundColor = UIColor.systemGray4
               leadingConstraint.constant = 8
               trailingConstraint.constant = 60
           }

           // Mesaj metni
           if let text = mesaj.mesaj_text, !text.isEmpty {
               tvMesaj.isHidden = false
               tvMesaj.text = text
           } else {
               tvMesaj.isHidden = true
           }

           // Resim gösterimi
           if let resimUrl = mesaj.resim_url, !resimUrl.isEmpty {
               ivMesajResim.isHidden = false
               ivMesajResim.sd_setImage(with: URL(string: resimUrl))
           } else {
               ivMesajResim.isHidden = true
           }

           // Tarih
           tvTarih.text = mesaj.tarih
       }
   }
