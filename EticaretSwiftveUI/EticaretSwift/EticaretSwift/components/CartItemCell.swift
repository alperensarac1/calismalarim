//
//  CartItemCell.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 14.01.2026.
//

import UIKit

final class CartItemCell: UITableViewCell {

    @IBOutlet private weak var thumbImageView: UIImageView!
    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var priceLabel: UILabel!

    @IBOutlet private weak var minusButton: UIButton!
    @IBOutlet private weak var plusButton: UIButton!
    @IBOutlet private weak var qtyLabel: UILabel!
    @IBOutlet private weak var deleteButton: UIButton!

    var onPlus: (() -> Void)?
    var onMinus: (() -> Void)?
    var onDelete: (() -> Void)?

    override func awakeFromNib() {
        super.awakeFromNib()

        selectionStyle = .none

        thumbImageView.contentMode = .scaleAspectFill
        thumbImageView.clipsToBounds = true
        thumbImageView.layer.cornerRadius = 10
        thumbImageView.image = UIImage(systemName: "photo")

        minusButton.layer.cornerRadius = 10
        plusButton.layer.cornerRadius = 10
        minusButton.backgroundColor = .systemGray6
        plusButton.backgroundColor = .systemGray6

        qtyLabel.textAlignment = .center
        qtyLabel.font = .systemFont(ofSize: 15, weight: .semibold)

        deleteButton.setTitle("Sil", for: .normal)
        deleteButton.setTitleColor(.systemRed, for: .normal)
    }

    func configure(title: String, priceText: String, qty: Int,image:String) {
        titleLabel.text = title
        priceLabel.text = priceText
        qtyLabel.text = "\(qty)"
        setImage(urlString: image)
        
        
        minusButton.isEnabled = qty > 1
        minusButton.alpha = qty > 1 ? 1.0 : 0.4
    }
    func setImage(urlString: String?) {
        thumbImageView.image = UIImage(systemName: "photo")
        guard let s = urlString, let url = URL(string: s) else { return }

        Task {
            do {
                let (data, _) = try await URLSession.shared.data(from: url)
                if let img = UIImage(data: data) {
                    await MainActor.run { self.thumbImageView.image = img }
                }
            } catch { }
        }
    }

    @IBAction private func plusTapped(_ sender: UIButton) { onPlus?() }
    @IBAction private func minusTapped(_ sender: UIButton) { onMinus?() }
    @IBAction private func deleteTapped(_ sender: UIButton) { onDelete?() }
}
