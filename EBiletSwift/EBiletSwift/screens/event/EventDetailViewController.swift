//
//  EventDetailViewController.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 26.06.2026.
//

import Foundation
import UIKit

/*
    EventDetailViewController

    XIB tabanlı etkinlik detay ekranıdır.

    Bu ekrana HomeViewController'dan sadece eventId gelir.

    Neden sadece eventId?
    Çünkü detay ekranında en güncel veriyi backend'den almak daha doğrudur.

    Kullanılan API'ler:

    1. events/event_detail.php
       Etkinlik detayını getirir.

    2. tickets/ticket_buy.php
       Kullanıcı için bilet oluşturur.
*/
final class EventDetailViewController: UIViewController {

    // MARK: - IBOutlet

    @IBOutlet private weak var scrollView: UIScrollView!
    @IBOutlet private weak var contentView: UIView!

    @IBOutlet private weak var posterImageView: UIImageView!

    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var descriptionLabel: UILabel!

    @IBOutlet private weak var dateTitleLabel: UILabel!
    @IBOutlet private weak var dateValueLabel: UILabel!

    @IBOutlet private weak var locationTitleLabel: UILabel!
    @IBOutlet private weak var locationValueLabel: UILabel!

    @IBOutlet private weak var venueTitleLabel: UILabel!
    @IBOutlet private weak var venueValueLabel: UILabel!

    @IBOutlet private weak var addressTitleLabel: UILabel!
    @IBOutlet private weak var addressValueLabel: UILabel!

    @IBOutlet private weak var priceLabel: UILabel!
    @IBOutlet private weak var quotaLabel: UILabel!

    @IBOutlet private weak var buyTicketButton: UIButton!

    @IBOutlet private weak var statusLabel: UILabel!
    @IBOutlet private weak var activityIndicator: UIActivityIndicatorView!

    // MARK: - Properties

    /*
        Home ekranından gelen etkinlik id.
    */
    private let eventId: Int

    /*
        Backend'den gelen güncel etkinlik.
    */
    private var currentEvent: Event?

    // MARK: - Init

    /*
        XIB ile çalışan ViewController'da dışarıdan eventId almak için
        custom init kullanıyoruz.

        nibName:
        EventDetailViewController.xib dosyasını yükler.
    */
    init(eventId: Int) {
        self.eventId = eventId

        super.init(
            nibName: "EventDetailViewController",
            bundle: nil
        )
    }

    /*
        Storyboard kullanmadığımız için bu init normalde kullanılmayacak.
        Ama Swift zorunlu tuttuğu için ekliyoruz.
    */
    required init?(coder: NSCoder) {
        self.eventId = 0
        super.init(coder: coder)
    }

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
        loadEventDetail()
    }

    // MARK: - Setup

    private func setupUI() {
        view.backgroundColor = UIColor(
            red: 245/255,
            green: 246/255,
            blue: 250/255,
            alpha: 1
        )

        title = "Etkinlik Detayı"

        posterImageView.backgroundColor = UIColor(
            red: 226/255,
            green: 232/255,
            blue: 240/255,
            alpha: 1
        )
        posterImageView.contentMode = .scaleAspectFill
        posterImageView.clipsToBounds = true
        posterImageView.layer.cornerRadius = 16
        posterImageView.layer.masksToBounds = true

        titleLabel.font = .boldSystemFont(ofSize: 24)
        titleLabel.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )
        titleLabel.numberOfLines = 0

        descriptionLabel.font = .systemFont(ofSize: 15)
        descriptionLabel.textColor = UIColor(
            red: 71/255,
            green: 85/255,
            blue: 105/255,
            alpha: 1
        )
        descriptionLabel.numberOfLines = 0

        setupDetailTitleLabel(dateTitleLabel, text: "Tarih")
        setupDetailTitleLabel(locationTitleLabel, text: "Konum")
        setupDetailTitleLabel(venueTitleLabel, text: "Sahne")
        setupDetailTitleLabel(addressTitleLabel, text: "Adres")

        setupDetailValueLabel(dateValueLabel)
        setupDetailValueLabel(locationValueLabel)
        setupDetailValueLabel(venueValueLabel)
        setupDetailValueLabel(addressValueLabel)

        priceLabel.font = .boldSystemFont(ofSize: 24)
        priceLabel.textColor = UIColor(
            red: 22/255,
            green: 163/255,
            blue: 74/255,
            alpha: 1
        )

        quotaLabel.font = .boldSystemFont(ofSize: 14)
        quotaLabel.textColor = UIColor(
            red: 37/255,
            green: 99/255,
            blue: 235/255,
            alpha: 1
        )
        quotaLabel.backgroundColor = UIColor(
            red: 239/255,
            green: 246/255,
            blue: 255/255,
            alpha: 1
        )
        quotaLabel.layer.cornerRadius = 10
        quotaLabel.layer.masksToBounds = true
        quotaLabel.textAlignment = .center

        buyTicketButton.setTitle("Bilet Al", for: .normal)
        buyTicketButton.backgroundColor = UIColor(
            red: 22/255,
            green: 163/255,
            blue: 74/255,
            alpha: 1
        )
        buyTicketButton.setTitleColor(.white, for: .normal)
        buyTicketButton.titleLabel?.font = .boldSystemFont(ofSize: 17)
        buyTicketButton.layer.cornerRadius = 12
        buyTicketButton.layer.masksToBounds = true

        statusLabel.font = .systemFont(ofSize: 14)
        statusLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
        statusLabel.numberOfLines = 0
        statusLabel.text = "Etkinlik detayı yükleniyor..."

        activityIndicator.hidesWhenStopped = true
        activityIndicator.stopAnimating()

        /*
            İlk yüklenmeden önce boş görünmesin.
        */
        clearUI()
    }

    private func setupDetailTitleLabel(
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

    private func setupDetailValueLabel(_ label: UILabel) {
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
        posterImageView.image = nil

        titleLabel.text = ""
        descriptionLabel.text = ""

        dateValueLabel.text = ""
        locationValueLabel.text = ""
        venueValueLabel.text = ""
        addressValueLabel.text = ""

        priceLabel.text = ""
        quotaLabel.text = ""

        buyTicketButton.isEnabled = false
        buyTicketButton.alpha = 0.6
    }

    // MARK: - API

    private func loadEventDetail() {
        guard eventId > 0 else {
            statusLabel.text = "Etkinlik bilgisi alınamadı."
            return
        }

        setLoading(true)
        statusLabel.text = "Etkinlik detayı yükleniyor..."

        APIService.shared.getEventDetail(
            apiToken: SessionManager.shared.apiToken,
            eventId: eventId
        ) { [weak self] result in
            guard let self else { return }

            self.setLoading(false)

            switch result {
            case .success(let response):

                guard response.success else {
                    self.statusLabel.text = response.message
                    return
                }

                guard let event = response.data else {
                    self.statusLabel.text = "Etkinlik bilgisi alınamadı."
                    return
                }

                self.currentEvent = event
                self.bindEvent(event)
                self.statusLabel.text = "Etkinlik detayı getirildi."

            case .failure(let error):
                self.statusLabel.text = error.localizedDescription
            }
        }
    }

    private func buyTicket() {
        guard let event = currentEvent else {
            showAlert(message: "Etkinlik bilgisi bulunamadı.")
            return
        }

        let remainingQuota = event.remainingQuota ?? 0

        guard remainingQuota > 0 else {
            showAlert(message: "Bu etkinlik için kontenjan kalmamış.")
            return
        }

        setBuying(true)
        statusLabel.text = "Bilet oluşturuluyor..."

        APIService.shared.buyTicket(
            apiToken: SessionManager.shared.apiToken,
            eventId: event.id
        ) { [weak self] result in
            guard let self else { return }

            self.setBuying(false)

            switch result {
            case .success(let response):

                guard response.success else {
                    self.statusLabel.text = response.message
                    self.showAlert(message: response.message)
                    return
                }

                let ticketCode = response.data?.ticketCode ?? "-"

                self.statusLabel.text = "Bilet başarıyla oluşturuldu."

                self.showAlert(
                    title: "Bilet Alındı",
                    message: "Bilet kodu: \(ticketCode)"
                ) {
                    let myTicketsVC = MyTicketsViewController()
                    self.navigationController?.pushViewController(myTicketsVC, animated: true)
                }

            case .failure(let error):
                self.statusLabel.text = error.localizedDescription
                self.showAlert(message: error.localizedDescription)
            }
        }
    }

    // MARK: - Bind

    private func bindEvent(_ event: Event) {
        titleLabel.text = event.title
        descriptionLabel.text = event.description ?? "Açıklama bulunmuyor."

        dateValueLabel.text = event.eventDate ?? "-"

        let cityName = event.city?.name ?? event.cityName ?? "-"
        let districtName = event.district?.name ?? event.districtName ?? "-"

        locationValueLabel.text = "\(cityName) / \(districtName)"
        venueValueLabel.text = event.venue?.name ?? "-"
        addressValueLabel.text = event.venue?.address ?? "-"

        let price = Int(event.basePrice ?? 0)
        priceLabel.text = "\(price) TL"

        let quota = event.remainingQuota ?? 0
        quotaLabel.text = "  Kalan: \(quota)  "

        if quota > 0 {
            buyTicketButton.isEnabled = true
            buyTicketButton.alpha = 1
            buyTicketButton.setTitle("Bilet Al", for: .normal)
            buyTicketButton.backgroundColor = UIColor(
                red: 22/255,
                green: 163/255,
                blue: 74/255,
                alpha: 1
            )
        } else {
            buyTicketButton.isEnabled = false
            buyTicketButton.alpha = 0.7
            buyTicketButton.setTitle("Kontenjan Doldu", for: .normal)
            buyTicketButton.backgroundColor = UIColor(
                red: 148/255,
                green: 163/255,
                blue: 184/255,
                alpha: 1
            )
        }

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

    // MARK: - Actions

    @IBAction private func buyTicketButtonTapped(_ sender: UIButton) {
        buyTicket()
    }

    // MARK: - Loading

    private func setLoading(_ isLoading: Bool) {
        buyTicketButton.isEnabled = !isLoading

        if isLoading {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }
    }

    private func setBuying(_ isBuying: Bool) {
        buyTicketButton.isEnabled = !isBuying

        if isBuying {
            buyTicketButton.setTitle("Bilet Oluşturuluyor...", for: .normal)
            activityIndicator.startAnimating()
        } else {
            buyTicketButton.setTitle("Bilet Al", for: .normal)
            activityIndicator.stopAnimating()
        }
    }
}
