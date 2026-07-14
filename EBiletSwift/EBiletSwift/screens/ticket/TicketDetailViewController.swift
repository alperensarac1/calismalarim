//
//  TicketDetailViewController.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 26.06.2026.
//

import Foundation
import UIKit
import CoreImage

/*
    TicketDetailViewController

    Tek biletin detayını ve QR kodunu gösterir.

    Backend:
    tickets/ticket_detail.php

    POST:
    api_token
    ticket_id

    QR:
    - Önce qr_code_text kullanılır.
    - Boşsa ticket_code kullanılır.
*/
final class TicketDetailViewController: UIViewController {

    // MARK: - IBOutlet

    @IBOutlet private weak var scrollView: UIScrollView!
    @IBOutlet private weak var contentView: UIView!

    @IBOutlet private weak var cardView: UIView!

    @IBOutlet private weak var eventTitleLabel: UILabel!
    @IBOutlet private weak var statusBadgeLabel: UILabel!

    @IBOutlet private weak var qrImageView: UIImageView!
    @IBOutlet private weak var ticketCodeLabel: UILabel!

    @IBOutlet private weak var dateTitleLabel: UILabel!
    @IBOutlet private weak var dateValueLabel: UILabel!

    @IBOutlet private weak var venueTitleLabel: UILabel!
    @IBOutlet private weak var venueValueLabel: UILabel!

    @IBOutlet private weak var locationTitleLabel: UILabel!
    @IBOutlet private weak var locationValueLabel: UILabel!

    @IBOutlet private weak var priceTitleLabel: UILabel!
    @IBOutlet private weak var priceValueLabel: UILabel!

    @IBOutlet private weak var usedAtLabel: UILabel!

    @IBOutlet private weak var statusLabel: UILabel!
    @IBOutlet private weak var activityIndicator: UIActivityIndicatorView!

    // MARK: - Properties

    private let ticketId: Int
    private var currentTicket: Ticket?

    // MARK: - Init

    init(ticketId: Int) {
        self.ticketId = ticketId

        super.init(
            nibName: "TicketDetailViewController",
            bundle: nil
        )
    }

    required init?(coder: NSCoder) {
        self.ticketId = 0
        super.init(coder: coder)
    }

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
        loadTicketDetail()
    }

    // MARK: - Setup

    private func setupUI() {
        view.backgroundColor = UIColor(
            red: 245/255,
            green: 246/255,
            blue: 250/255,
            alpha: 1
        )

        title = "Bilet Detayı"

        cardView.backgroundColor = .white
        cardView.layer.cornerRadius = 18
        cardView.layer.masksToBounds = true

        eventTitleLabel.font = .boldSystemFont(ofSize: 23)
        eventTitleLabel.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )
        eventTitleLabel.numberOfLines = 0
        eventTitleLabel.textAlignment = .center

        statusBadgeLabel.font = .boldSystemFont(ofSize: 14)
        statusBadgeLabel.textAlignment = .center
        statusBadgeLabel.layer.cornerRadius = 10
        statusBadgeLabel.layer.masksToBounds = true

        qrImageView.backgroundColor = .white
        qrImageView.contentMode = .scaleAspectFit
        qrImageView.layer.cornerRadius = 10
        qrImageView.layer.masksToBounds = true

        ticketCodeLabel.font = .systemFont(ofSize: 14)
        ticketCodeLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
        ticketCodeLabel.textAlignment = .center

        setupTitleLabel(dateTitleLabel, text: "Tarih")
        setupTitleLabel(venueTitleLabel, text: "Sahne")
        setupTitleLabel(locationTitleLabel, text: "Konum")
        setupTitleLabel(priceTitleLabel, text: "Fiyat")

        setupValueLabel(dateValueLabel)
        setupValueLabel(venueValueLabel)
        setupValueLabel(locationValueLabel)
        setupValueLabel(priceValueLabel)

        priceValueLabel.textColor = UIColor(
            red: 22/255,
            green: 163/255,
            blue: 74/255,
            alpha: 1
        )

        usedAtLabel.font = .systemFont(ofSize: 14)
        usedAtLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
        usedAtLabel.numberOfLines = 0

        statusLabel.font = .systemFont(ofSize: 14)
        statusLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
        statusLabel.numberOfLines = 0
        statusLabel.text = "Bilet detayı yükleniyor..."

        activityIndicator.hidesWhenStopped = true
        activityIndicator.stopAnimating()

        clearUI()
    }

    private func setupTitleLabel(
        _ label: UILabel,
        text: String
    ) {
        label.text = text
        label.font = .systemFont(ofSize: 13)
        label.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
    }

    private func setupValueLabel(_ label: UILabel) {
        label.font = .boldSystemFont(ofSize: 15)
        label.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )
        label.numberOfLines = 0
    }

    private func clearUI() {
        eventTitleLabel.text = ""
        statusBadgeLabel.text = ""
        qrImageView.image = nil
        ticketCodeLabel.text = ""

        dateValueLabel.text = ""
        venueValueLabel.text = ""
        locationValueLabel.text = ""
        priceValueLabel.text = ""
        usedAtLabel.text = ""
    }

    // MARK: - API

    private func loadTicketDetail() {
        guard ticketId > 0 else {
            statusLabel.text = "Bilet bilgisi alınamadı."
            return
        }

        setLoading(true)
        statusLabel.text = "Bilet detayı yükleniyor..."

        APIService.shared.getTicketDetail(
            apiToken: SessionManager.shared.apiToken,
            ticketId: ticketId
        ) { [weak self] result in
            guard let self else { return }

            self.setLoading(false)

            switch result {
            case .success(let response):

                guard response.success else {
                    self.statusLabel.text = response.message
                    return
                }

                guard let ticket = response.data else {
                    self.statusLabel.text = "Bilet bilgisi alınamadı."
                    return
                }

                self.currentTicket = ticket
                self.bindTicket(ticket)
                self.statusLabel.text = "Bilet detayı getirildi."

            case .failure(let error):
                self.statusLabel.text = error.localizedDescription
            }
        }
    }

    // MARK: - Bind

    private func bindTicket(_ ticket: Ticket) {
        let eventTitle = ticket.event?.title ?? ticket.eventTitle ?? "Etkinlik"
        eventTitleLabel.text = eventTitle

        configureStatus(ticket.status ?? ticket.ticketStatus ?? "-")

        let qrText = ticket.qrCodeText ?? ticket.ticketCode ?? ""

        if qrText.isEmpty {
            qrImageView.image = nil
            ticketCodeLabel.text = "QR oluşturulamadı"
        } else {
            qrImageView.image = generateQRCode(from: qrText)
            ticketCodeLabel.text = ticket.ticketCode ?? qrText
        }

        dateValueLabel.text = ticket.event?.eventDate ?? "-"

        let venueName =
            ticket.venue?.name ??
            ticket.location?.venueName ??
            ticket.event?.venue?.name ??
            "-"

        venueValueLabel.text = venueName

        let cityName =
            ticket.city?.name ??
            ticket.location?.cityName ??
            ticket.event?.city?.name ??
            "-"

        let districtName =
            ticket.district?.name ??
            ticket.location?.districtName ??
            ticket.event?.district?.name ??
            "-"

        locationValueLabel.text = "\(cityName) / \(districtName)"

        let price = Int(ticket.price ?? 0)
        priceValueLabel.text = "\(price) TL"

        if let usedAt = ticket.usedAt, !usedAt.isEmpty {
            usedAtLabel.text = "Kullanım zamanı: \(usedAt)"
        } else {
            usedAtLabel.text = "Bilet henüz kullanılmadı."
        }
    }

    private func configureStatus(_ status: String) {
        switch status {
        case "active":
            statusBadgeLabel.text = "Aktif Bilet"
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
            statusBadgeLabel.text = "İptal Edildi"
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

    // MARK: - QR

    /*
        CoreImage ile QR kod üretir.

        UIKit tarafında QR üretmek için ekstra kütüphane gerekmez.
        CIFilter(name: "CIQRCodeGenerator") yeterlidir.
    */
    private func generateQRCode(from text: String) -> UIImage? {
        let data = text.data(using: .utf8)

        guard let filter = CIFilter(name: "CIQRCodeGenerator") else {
            return nil
        }

        filter.setValue(data, forKey: "inputMessage")
        filter.setValue("Q", forKey: "inputCorrectionLevel")

        guard let outputImage = filter.outputImage else {
            return nil
        }

        /*
            QR küçük üretildiği için büyütüyoruz.
            transformed ile kalite bozulmadan ölçeklenir.
        */
        let transform = CGAffineTransform(scaleX: 12, y: 12)
        let scaledImage = outputImage.transformed(by: transform)

        let context = CIContext()

        guard let cgImage = context.createCGImage(
            scaledImage,
            from: scaledImage.extent
        ) else {
            return nil
        }

        return UIImage(cgImage: cgImage)
    }

    // MARK: - Loading

    private func setLoading(_ isLoading: Bool) {
        if isLoading {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }
    }
}
