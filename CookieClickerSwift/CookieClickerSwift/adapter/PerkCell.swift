//
//  PerkCell.swift
//  CookieClickerSwift
//
//  Created by Alperen Saraç on 15.09.2025.
//

import UIKit

class PerkCell: UITableViewCell {

    @IBOutlet weak var buyButton: UIButton!
    @IBOutlet weak var metaLabel: UILabel!
    @IBOutlet weak var descLabel: UILabel!
    @IBOutlet weak var titleLabel: UILabel!
    
    var onBuy: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

    @IBAction func buyTapped(_ sender: Any) {
        onBuy?()
    }
    override func prepareForReuse() { super.prepareForReuse(); onBuy = nil }
}
