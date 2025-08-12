//
//  EntryCell.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 9.08.2025.
//

import UIKit

class EntryCell: UITableViewCell {

    @IBOutlet weak var tvTarih: UILabel!
    @IBOutlet weak var tvYazanKisi: UILabel!
    @IBOutlet weak var tvEntry: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        tvEntry.numberOfLines = 0
        tvYazanKisi.textColor = .secondaryLabel
        tvTarih.textColor = .secondaryLabel
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
