//
//  TicketTableViewCell.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 26.06.2026.
//

import Foundation
import UIKit

/*
    TicketTableViewCell

    MyTicketsViewController içindeki tek bilet kartıdır.

    XIB:
    TicketTableViewCell.xib
*/
final class TicketTableViewCell: UITableViewCell {

    static let identifier = "TicketTableViewCell"

    // MARK: - IBOutlet

    @IBOutlet private weak var cardView: UIView!

    @IBOutlet private weak var posterImageView: UIImageView!

    @IBOutlet private weak var eventTitleLabel: UILabel!
    @IBOutlet private weak var dateLabel: UILabel!
    @IBOutlet private weak var venueLabel: UILabel!
    @IBOutlet private weak var locationLabel: UILabel!

    @IBOutlet private weak var priceLabel: UILabel!
    @IBOutlet private weak var statusBadgeLabel: UILabel!

    // MARK: - Lifecycle

    override func awakeFromNib() {
        super.awakeFromNib()

        setupUI()
    }

    override func prepareForReuse() {
        super.prepareForReuse()

        posterImageView.image = nil

        eventTitleLabel.text = nil
        dateLabel.text = nil
        venueLabel.text = nil
        locationLabel.text = nil
        priceLabel.text = nil
        statusBadgeLabel.text = nil
    }

    // MARK: - Setup

    private func setupUI() {
        selectionStyle = .none
        backgroundColor = .clear
        contentView.backgroundColor = .clear

        cardView.backgroundColor = .white
        cardView.layer.cornerRadius = 16
        cardView.layer.masksToBounds = true

        posterImageView.backgroundColor = UIColor(
            red: 226/255,
            green: 232/255,
            blue: 240/255,
            alpha: 1
        )
        posterImageView.contentMode = .scaleAspectFill
        posterImageView.clipsToBounds = true
        posterImageView.layer.cornerRadius = 10
        posterImageView.layer.masksToBounds = true

        eventTitleLabel.font = .boldSystemFont(ofSize: 16)
        eventTitleLabel.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )
        eventTitleLabel.numberOfLines = 2

        dateLabel.font = .systemFont(ofSize: 13)
        dateLabel.textColor = UIColor(
            red: 71/255,
            green: 85/255,
            blue: 105/255,
            alpha: 1
        )

        venueLabel.font = .systemFont(ofSize: 13)
        venueLabel.textColor = UIColor(
            red: 71/255,
            green: 85/255,
            blue: 105/255,
            alpha: 1
        )

        locationLabel.font = .systemFont(ofSize: 12)
        locationLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )

        priceLabel.font = .boldSystemFont(ofSize: 16)
        priceLabel.textColor = UIColor(
            red: 22/255,
            green: 163/255,
            blue: 74/255,
            alpha: 1
        )

        statusBadgeLabel.font = .boldSystemFont(ofSize: 12)
        statusBadgeLabel.textAlignment = .center
        statusBadgeLabel.layer.cornerRadius = 8
        statusBadgeLabel.layer.masksToBounds = true
    }

    // MARK: - Configure

    func configure(with ticket: Ticket) {
        let event = ticket.event

        eventTitleLabel.text = event?.title ?? ticket.eventTitle ?? "Etkinlik bilgisi yok"
        dateLabel.text = "Tarih: \(event?.eventDate ?? "-")"

        let venueName =
            ticket.location?.venueName ??
            ticket.venue?.name ??
            event?.venue?.name ??
            "-"

        venueLabel.text = "Sahne: \(venueName)"

        let cityName =
            ticket.location?.cityName ??
            ticket.city?.name ??
            event?.city?.name ??
            "-"

        let districtName =
            ticket.location?.districtName ??
            ticket.district?.name ??
            event?.district?.name ??
            "-"

        locationLabel.text = "\(cityName) / \(districtName)"

        let price = Int(ticket.price ?? 0)
        priceLabel.text = "\(price) TL"

        configureStatus(ticket.status ?? ticket.ticketStatus ?? "-")

        loadPoster(path: event?.posterUrl)
    }

    private func configureStatus(_ status: String) {
        switch status {
        case "active":
            statusBadgeLabel.text = "Aktif"
            statusBadgeLabel.backgroundColor = UIColor(
                red: 220/255,
                green: 252/255,
                blue: 231/255,
                alpha: 1
            )
            statusBadgeLabel.textColor = UIColor(
                red: 22/255,
                green: 101/255,
                blue: 52/255,
                alpha: 1
            )

        case "used":
            statusBadgeLabel.text = "Kullanıldı"
            statusBadgeLabel.backgroundColor = UIColor(
                red: 226/255,
                green: 232/255,
                blue: 240/255,
                alpha: 1
            )
            statusBadgeLabel.textColor = UIColor(
                red: 71/255,
                green: 85/255,
                blue: 105/255,
                alpha: 1
            )

        case "cancelled":
            statusBadgeLabel.text = "İptal"
            statusBadgeLabel.backgroundColor = UIColor(
                red: 254/255,
                green: 226/255,
                blue: 226/255,
                alpha: 1
            )
            statusBadgeLabel.textColor = UIColor(
                red: 153/255,
                green: 27/255,
                blue: 27/255,
                alpha: 1
            )

        default:
            statusBadgeLabel.text = status
            statusBadgeLabel.backgroundColor = UIColor(
                red: 239/255,
                green: 246/255,
                blue: 255/255,
                alpha: 1
            )
            statusBadgeLabel.textColor = UIColor(
                red: 37/255,
                green: 99/255,
                blue: 235/255,
                alpha: 1
            )
        }
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
