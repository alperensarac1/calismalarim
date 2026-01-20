//
//  CategoryCellCollectionViewCell.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 14.01.2026.
//

import UIKit

final class CategoryCell: UICollectionViewCell {

    @IBOutlet private weak var chipView: UIView!
    @IBOutlet private weak var titleLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        chipView.layer.cornerRadius = 16
        chipView.layer.masksToBounds = true
    }

    func configure(title: String, selected: Bool) {
        titleLabel.text = title

        if selected {
            chipView.backgroundColor = .systemBlue
            titleLabel.textColor = .white
        } else {
            chipView.backgroundColor = .systemGray5
            titleLabel.textColor = .label
        }
    }
}
