//
//  JobCell.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//

import Foundation
import UIKit

final class JobCell: UITableViewCell {

    @IBOutlet weak var lblJobType: UILabel!
    @IBOutlet weak var lblStatus: UILabel!
    @IBOutlet weak var lblCreatedAt: UILabel!
    @IBOutlet weak var lblError: UILabel!
    @IBOutlet weak var btnOpenResult: UIButton!
    @IBOutlet weak var btnDownload: UIButton!
    @IBOutlet weak var progressView: UIProgressView!
    @IBOutlet weak var lblProgress: UILabel!

    private var currentJob: JobItem?
    private let downloadHelper = DownloadHelper()
    private weak var parentViewController: UIViewController?

    override func awakeFromNib() {
        super.awakeFromNib()
        selectionStyle = .none
        setupUI()
        bindDownloadHelper()
    }

    private func setupUI() {
        lblError.isHidden = true
        btnOpenResult.isHidden = true
        btnDownload.isHidden = true
        progressView.isHidden = true
        lblProgress.isHidden = true
        progressView.progress = 0
    }

    private func bindDownloadHelper() {
        downloadHelper.onProgressChanged = { [weak self] progress in
            guard let self = self else { return }
            self.progressView.isHidden = false
            self.lblProgress.isHidden = false
            self.progressView.progress = Float(progress)
            self.lblProgress.text = "\(Int(progress * 100))%"
        }

        downloadHelper.onStateChanged = { [weak self] isDownloading in
            guard let self = self else { return }
            self.btnDownload.isEnabled = !isDownloading
            self.btnDownload.setTitle(isDownloading ? "İndiriliyor..." : "İndir", for: .normal)
        }

        downloadHelper.onCompleted = { [weak self] fileURL in
            guard let self = self else { return }
            self.progressView.progress = 1.0
            self.lblProgress.text = "100%"

            let activityVC = UIActivityViewController(
                activityItems: [fileURL],
                applicationActivities: nil
            )

            self.parentViewController?.present(activityVC, animated: true)
        }

        downloadHelper.onError = { [weak self] errorMessage in
            guard let self = self else { return }
            self.lblError.isHidden = false
            self.lblError.text = "İndirme hatası: \(errorMessage)"
        }
    }

    func configure(with job: JobItem, parentViewController: UIViewController) {
        self.currentJob = job
        self.parentViewController = parentViewController

        lblJobType.text = "İşlem: \(mapJobType(job.job_type))"
        lblStatus.text = "Durum: \(mapStatus(job.status))"
        lblCreatedAt.text = "Tarih: \(job.created_at ?? "-")"

        if let error = job.error_message, !error.isEmpty {
            lblError.isHidden = false
            lblError.text = "Hata: \(error)"
        } else {
            lblError.isHidden = true
            lblError.text = nil
        }

        if let resultURL = job.result_file_url, !resultURL.isEmpty {
            btnOpenResult.isHidden = false
            btnDownload.isHidden = false
        } else {
            btnOpenResult.isHidden = true
            btnDownload.isHidden = true
        }

        progressView.isHidden = true
        lblProgress.isHidden = true
        progressView.progress = 0
        lblProgress.text = "0%"
        btnDownload.isEnabled = true
        btnDownload.setTitle("İndir", for: .normal)
    }

    @IBAction func didTapOpenResult(_ sender: UIButton) {
        guard let resultURL = currentJob?.result_file_url,
              let url = URL(string: resultURL) else { return }

        UIApplication.shared.open(url)
    }

    @IBAction func didTapDownload(_ sender: UIButton) {
        guard let resultURL = currentJob?.result_file_url else { return }
        downloadHelper.downloadFile(from: resultURL)
    }

    private func mapJobType(_ jobType: String?) -> String {
        switch jobType {
        case "jpg_to_pdf":
            return "JPG to PDF"
        case "pdf_to_word":
            return "PDF to Word"
        case "word_to_pdf":
            return "Word to PDF"
        case "pdf_merge":
            return "PDF Birleştirme"
        default:
            return jobType ?? "-"
        }
    }

    private func mapStatus(_ status: String?) -> String {
        switch status {
        case "waiting":
            return "Bekliyor"
        case "processing":
            return "İşleniyor"
        case "done":
            return "Tamamlandı"
        case "failed":
            return "Başarısız"
        default:
            return status ?? "-"
        }
    }
}
