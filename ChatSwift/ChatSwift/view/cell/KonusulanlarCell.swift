//
//  KonusulanlarCell.swift
//  ChatSwift
//
//  Created by Alperen Saraç on 3.07.2025.
//

import UIKit

class KonusulanlarCell: UITableViewCell {

    @IBOutlet weak var tvMesajTarih: UILabel!
    @IBOutlet weak var tvMesajOzet: UILabel!
    @IBOutlet weak var tvKullaniciAdi: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    func configure(with kisi: KonusulanKisi) {
         tvKullaniciAdi.text = kisi.ad
         tvMesajOzet.text = kisi.son_mesaj
         tvMesajTarih.text = kisi.tarih
     }
}
