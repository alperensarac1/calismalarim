//
//  MyTicketsViewController.swift
//  EBiletSwift
//
//  Created by Alperen Saraç on 26.06.2026.
//

import Foundation
import UIKit

/*
    MyTicketsViewController

    Kullanıcının satın aldığı biletleri listeler.

    Backend:
    tickets/my_tickets.php

    POST:
    api_token

    XIB yapısı:
    - titleLabel
    - statusLabel
    - activityIndicator
    - tableView
*/
final class MyTicketsViewController: UIViewController {

    // MARK: - IBOutlet

    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var statusLabel: UILabel!
    @IBOutlet private weak var activityIndicator: UIActivityIndicatorView!
    @IBOutlet private weak var tableView: UITableView!

    // MARK: - Data

    private var tickets: [Ticket] = []

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
        setupTableView()
        loadMyTickets()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        /*
            QR kontrol ekranında bilet kullanılmış olabilir.
            Bu yüzden ekrana geri dönüldüğünde listeyi tazelemek iyi olur.
        */
        loadMyTickets()
    }

    // MARK: - Setup

    private func setupUI() {
        view.backgroundColor = UIColor(
            red: 245/255,
            green: 246/255,
            blue: 250/255,
            alpha: 1
        )

        title = "Biletlerim"

        titleLabel.text = "Biletlerim"
        titleLabel.font = .boldSystemFont(ofSize: 26)
        titleLabel.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )

        statusLabel.text = "Biletler yükleniyor..."
        statusLabel.font = .systemFont(ofSize: 14)
        statusLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
        statusLabel.numberOfLines = 0

        activityIndicator.hidesWhenStopped = true
        activityIndicator.stopAnimating()
    }

    private func setupTableView() {
        tableView.backgroundColor = .clear
        tableView.separatorStyle = .none

        tableView.delegate = self
        tableView.dataSource = self

        let nib = UINib(
            nibName: "TicketTableViewCell",
            bundle: nil
        )

        tableView.register(
            nib,
            forCellReuseIdentifier: TicketTableViewCell.identifier
        )
    }

    // MARK: - API

    private func loadMyTickets() {
        setLoading(true)
        statusLabel.text = "Biletler yükleniyor..."

        APIService.shared.getMyTickets(
            apiToken: SessionManager.shared.apiToken
        ) { [weak self] result in
            guard let self else { return }

            self.setLoading(false)

            switch result {
            case .success(let response):

                guard response.success else {
                    self.statusLabel.text = response.message
                    return
                }

                self.tickets = response.data ?? []
                self.tableView.reloadData()

                if self.tickets.isEmpty {
                    self.statusLabel.text = "Henüz satın alınmış biletin yok."
                } else {
                    self.statusLabel.text = "\(self.tickets.count) bilet listelendi."
                }

            case .failure(let error):
                self.statusLabel.text = error.localizedDescription
            }
        }
    }

    private func setLoading(_ isLoading: Bool) {
        if isLoading {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }
    }
}

// MARK: - UITableViewDataSource, UITableViewDelegate

extension MyTicketsViewController: UITableViewDataSource, UITableViewDelegate {

    func tableView(
        _ tableView: UITableView,
        numberOfRowsInSection section: Int
    ) -> Int {
        return tickets.count
    }

    func tableView(
        _ tableView: UITableView,
        cellForRowAt indexPath: IndexPath
    ) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(
            withIdentifier: TicketTableViewCell.identifier,
            for: indexPath
        ) as? TicketTableViewCell else {
            return UITableViewCell()
        }

        let ticket = tickets[indexPath.row]
        cell.configure(with: ticket)

        return cell
    }

    func tableView(
        _ tableView: UITableView,
        didSelectRowAt indexPath: IndexPath
    ) {
        tableView.deselectRow(at: indexPath, animated: true)

        let ticket = tickets[indexPath.row]

        guard let ticketId = ticket.resolvedTicketId else {
            showAlert(message: "Bilet ID alınamadı.")
            return
        }

        let detailVC = TicketDetailViewController(ticketId: ticketId)
        navigationController?.pushViewController(detailVC, animated: true)
    }

    func tableView(
        _ tableView: UITableView,
        heightForRowAt indexPath: IndexPath
    ) -> CGFloat {
        return 160
    }
}
