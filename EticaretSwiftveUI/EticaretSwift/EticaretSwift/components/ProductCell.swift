//
//  ProductCell.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 14.01.2026.
//

import UIKit

struct ProductUI {
    let id: Int
    let title: String
    let priceText: String
    let imageUrl: String?   // şimdilik nil olabilir
}

final class ProductCell: UICollectionViewCell {

    @IBOutlet private weak var cardView: UIView!
    @IBOutlet private weak var productImageView: UIImageView!
    @IBOutlet private weak var nameLabel: UILabel!
    @IBOutlet private weak var priceLabel: UILabel!
    @IBOutlet private weak var addToCartButton: UIButton!
    var onAddToCart: (() -> Void)?

    override func awakeFromNib() {
        super.awakeFromNib()

        cardView.layer.cornerRadius = 12
        cardView.layer.masksToBounds = true

        // Hafif gölge (MaterialCardView hissi)
        contentView.layer.shadowColor = UIColor.black.cgColor
        contentView.layer.shadowOpacity = 0.08
        contentView.layer.shadowRadius = 8
        contentView.layer.shadowOffset = CGSize(width: 0, height: 2)
        contentView.layer.masksToBounds = false

        productImageView.contentMode = .scaleAspectFill
        productImageView.clipsToBounds = true

        nameLabel.numberOfLines = 2
        nameLabel.font = .boldSystemFont(ofSize: 15)

        priceLabel.textAlignment = .right
        priceLabel.font = .systemFont(ofSize: 14)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        // shadow path performans için
        contentView.layer.shadowPath = UIBezierPath(roundedRect: bounds, cornerRadius: 12).cgPath
    }

    func configure(with item: ProductUI) {
        nameLabel.text = item.title
          priceLabel.text = item.priceText
          setImage(urlString: item.imageUrl)
    }
    @IBAction private func addToCartTapped(_ sender: UIButton) {
        onAddToCart?()
    }
    func setImage(urlString: String?) {
        productImageView.image = UIImage(systemName: "photo")
        guard let s = urlString, let url = URL(string: s) else { return }

        Task {
            do {
                let (data, _) = try await URLSession.shared.data(from: url)
                if let img = UIImage(data: data) {
                    await MainActor.run { self.productImageView.image = img }
                }
            } catch { }
        }
    }

}
