//
//  EventTableViewCell.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 25.06.2026.
//

import Foundation
import UIKit

/*
    EventTableViewCell

    Ana ekrandaki etkinlik kartıdır.

    XIB:
    EventTableViewCell.xib

    UITableView içinde her etkinlik için bu cell kullanılır.
*/
final class EventTableViewCell: UITableViewCell {

    static let identifier = "EventTableViewCell"

    // MARK: - IBOutlet

    @IBOutlet private weak var cardView: UIView!

    @IBOutlet private weak var posterImageView: UIImageView!

    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var dateLabel: UILabel!
    @IBOutlet private weak var venueLabel: UILabel!
    @IBOutlet private weak var locationLabel: UILabel!

    @IBOutlet private weak var priceLabel: UILabel!
    @IBOutlet private weak var quotaLabel: UILabel!

    // MARK: - Lifecycle

    override func awakeFromNib() {
        super.awakeFromNib()

        setupUI()
    }

    override func prepareForReuse() {
        super.prepareForReuse()

        posterImageView.image = nil
        titleLabel.text = nil
        dateLabel.text = nil
        venueLabel.text = nil
        locationLabel.text = nil
        priceLabel.text = nil
        quotaLabel.text = nil
    }

    // MARK: - Setup

    private func setupUI() {
        selectionStyle = .none
        backgroundColor = .clear
        contentView.backgroundColor = .clear

        cardView.backgroundColor = .white
        cardView.layer.cornerRadius = 16
        cardView.layer.masksToBounds = true

        posterImageView.backgroundColor = UIColor(red: 226/255, green: 232/255, blue: 240/255, alpha: 1)
        posterImageView.contentMode = .scaleAspectFill
        posterImageView.clipsToBounds = true

        titleLabel.font = .boldSystemFont(ofSize: 18)
        titleLabel.textColor = UIColor(red: 15/255, green: 23/255, blue: 42/255, alpha: 1)
        titleLabel.numberOfLines = 2

        dateLabel.font = .systemFont(ofSize: 14)
        dateLabel.textColor = UIColor(red: 71/255, green: 85/255, blue: 105/255, alpha: 1)

        venueLabel.font = .systemFont(ofSize: 14)
        venueLabel.textColor = UIColor(red: 71/255, green: 85/255, blue: 105/255, alpha: 1)

        locationLabel.font = .systemFont(ofSize: 13)
        locationLabel.textColor = UIColor(red: 100/255, green: 116/255, blue: 139/255, alpha: 1)

        priceLabel.font = .boldSystemFont(ofSize: 18)
        priceLabel.textColor = UIColor(red: 22/255, green: 163/255, blue: 74/255, alpha: 1)

        quotaLabel.font = .boldSystemFont(ofSize: 13)
        quotaLabel.textColor = UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1)
        quotaLabel.backgroundColor = UIColor(red: 239/255, green: 246/255, blue: 255/255, alpha: 1)
        quotaLabel.layer.cornerRadius = 8
        quotaLabel.layer.masksToBounds = true
        quotaLabel.textAlignment = .center
    }

    // MARK: - Configure

    func configure(with event: Event) {
        titleLabel.text = event.title
        dateLabel.text = "Tarih: \(event.eventDate ?? "-")"

        let venueName = event.venue?.name ?? "-"
        venueLabel.text = "Sahne: \(venueName)"

        let cityName = event.cityName ?? event.city?.name ?? "-"
        let districtName = event.districtName ?? event.district?.name ?? "-"

        locationLabel.text = "\(cityName) / \(districtName)"

        let price = Int(event.basePrice ?? 0)
        priceLabel.text = "\(price) TL"

        let quota = event.remainingQuota ?? 0
        quotaLabel.text = "  Kalan: \(quota)  "

        loadPoster(path: event.posterUrl)
    }

    private func loadPoster(path: String?) {
        guard let path, !path.isEmpty else {
            posterImageView.image = nil
            return
        }

        let finalURLString: String

        if path.hasPrefix("http") {
            finalURLString = path
        } else {
            finalURLString = APIClient.baseURL + path
        }

        guard let url = URL(string: finalURLString) else {
            posterImageView.image = nil
            return
        }

        /*
            Basit resim yükleme.
            Senior mimaride bunu ImageLoader/cache sınıfına ayırırız.
        */
        URLSession.shared.dataTask(with: url) { [weak self] data, _, _ in
            guard let self else { return }

            guard let data,
                  let image = UIImage(data: data) else {
                return
            }

            DispatchQueue.main.async {
                self.posterImageView.image = image
            }
        }.resume()
    }
}
