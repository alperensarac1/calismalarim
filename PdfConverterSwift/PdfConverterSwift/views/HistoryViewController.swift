//
//  HistoryViewController.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation
import UIKit

final class HistoryViewController: UIViewController {

    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    @IBOutlet weak var lblEmpty: UILabel!

    private let viewModel = HistoryViewModel()

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Geçmiş İşlemler"
        setupUI()
        setupTableView()
        bindViewModel()
        viewModel.loadJobs()
    }

    private func setupUI() {
        lblEmpty.text = "Henüz işlem yok"
        lblEmpty.isHidden = true
        activityIndicator.hidesWhenStopped = true
    }

    private func setupTableView() {
        tableView.dataSource = self
        tableView.delegate = self
    }

    private func bindViewModel() {
        viewModel.onStateChanged = { [weak self] in
            guard let self = self else { return }

            if self.viewModel.isLoading {
                self.activityIndicator.startAnimating()
            } else {
                self.activityIndicator.stopAnimating()
            }

            if let error = self.viewModel.errorText, !error.isEmpty {
                self.lblEmpty.isHidden = false
                self.lblEmpty.text = "Hata: \(error)"
            } else if self.viewModel.jobs.isEmpty && !self.viewModel.isLoading {
                self.lblEmpty.isHidden = false
                self.lblEmpty.text = "Henüz işlem yok"
            } else {
                self.lblEmpty.isHidden = true
            }

            self.tableView.reloadData()
        }
    }
}

extension HistoryViewController: UITableViewDataSource, UITableViewDelegate {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        viewModel.jobs.count
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 220
    }

    func tableView(
        _ tableView: UITableView,
        cellForRowAt indexPath: IndexPath
    ) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(
            withIdentifier: "JobCell",
            for: indexPath
        ) as? JobCell else {
            return UITableViewCell()
        }

        let job = viewModel.jobs[indexPath.row]
        cell.configure(with: job, parentViewController: self)
        return cell
    }
}
