//
//  UpgradeCell.swift
//  CookieClickerSwift
//
//  Created by Alperen Saraç on 14.09.2025.
//

import UIKit

class UpgradeCell: UITableViewCell {
    @IBOutlet weak var buyButton: UIButton!
    @IBOutlet weak var priceLabel: UILabel!
    @IBOutlet weak var descLabel: UILabel!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var iconImageView: UIImageView!
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
        print("✅ UpgradeCell.buyTapped fired")
             onBuy?()
    }
}
