//
//  YorumlarCell.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import UIKit

class YorumlarCell: UITableViewCell {

    @IBOutlet weak var tvDislike: UILabel!
    @IBOutlet weak var tvLike: UILabel!
    @IBOutlet weak var tvTarih: UILabel!
    @IBOutlet weak var tvYorumYazanKisi: UILabel!
    @IBOutlet weak var tvYorum: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        tvYorum.numberOfLines = 0
             tvYorumYazanKisi.textColor = .secondaryLabel
             tvTarih.textColor = .secondaryLabel
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
