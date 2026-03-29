//
//  ViewController.swift
//  PdfConverterSwift
//
//  Created by Alperen Saraç on 27.03.2026.
//


import UIKit
import UniformTypeIdentifiers

class ViewController: UIViewController {

        @IBOutlet weak var lblStatus: UILabel!
        @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
        @IBOutlet weak var btnOpenResult: UIButton!

        private let viewModel = MainViewModel()

        private var pendingJobType: String?
        private var isPickingMultiple = false

        override func viewDidLoad() {
            super.viewDidLoad()
            title = "PDF Dönüştürücü"
            setupUI()
            bindViewModel()
        }

        private func setupUI() {
            lblStatus.text = "Hazır"
            activityIndicator.hidesWhenStopped = true
            btnOpenResult.isHidden = true
        }

        private func bindViewModel() {
            viewModel.onStateChanged = { [weak self] in
                guard let self = self else { return }

                self.activityIndicator.isHidden = false

                if self.viewModel.isLoading ||
                    self.viewModel.currentJobStatus == "waiting" ||
                    self.viewModel.currentJobStatus == "processing" {
                    self.activityIndicator.startAnimating()
                } else {
                    self.activityIndicator.stopAnimating()
                }

                self.lblStatus.text = self.buildStatusText()

                if let urlString = self.viewModel.resultFileURL,
                   !urlString.isEmpty {
                    self.btnOpenResult.isHidden = false
                } else {
                    self.btnOpenResult.isHidden = true
                }
            }
        }

        private func buildStatusText() -> String {
            if let error = viewModel.errorText, !error.isEmpty {
                return "Hata: \(error)"
            }

            if let resultURL = viewModel.resultFileURL,
               !resultURL.isEmpty,
               viewModel.currentJobStatus == "done" {
                return "İşlem tamamlandı. Sonuç hazır."
            }

            switch viewModel.currentJobStatus {
            case "waiting":
                return "İş sıraya alındı, worker bekleniyor..."
            case "processing":
                return "Dönüştürme işlemi devam ediyor..."
            default:
                return viewModel.message ?? "Hazır"
            }
        }

        @IBAction func didTapJpgToPdf(_ sender: UIButton) {
            pendingJobType = "jpg_to_pdf"
            isPickingMultiple = false
            presentSingleDocumentPicker(types: [.image])
        }

        @IBAction func didTapPdfToWord(_ sender: UIButton) {
            pendingJobType = "pdf_to_word"
            isPickingMultiple = false
            presentSingleDocumentPicker(types: [.pdf])
        }

        @IBAction func didTapWordToPdf(_ sender: UIButton) {
            pendingJobType = "word_to_pdf"
            isPickingMultiple = false
            presentSingleDocumentPicker(types: [.item])
        }

        @IBAction func didTapMergePdf(_ sender: UIButton) {
            pendingJobType = "pdf_merge"
            isPickingMultiple = true
            presentMultipleDocumentPicker(types: [.pdf])
        }

        @IBAction func didTapHistory(_ sender: UIButton) {
            let storyboard = UIStoryboard(name: "Main", bundle: nil)
            guard let vc = storyboard.instantiateViewController(withIdentifier: "HistoryViewController") as? HistoryViewController else {
                return
            }
            navigationController?.pushViewController(vc, animated: true)
        }

        @IBAction func didTapOpenResult(_ sender: UIButton) {
            guard let urlString = viewModel.resultFileURL,
                  let url = URL(string: urlString) else { return }

            UIApplication.shared.open(url)
        }

        private func presentSingleDocumentPicker(types: [UTType]) {
            let picker = UIDocumentPickerViewController(forOpeningContentTypes: types, asCopy: true)
            picker.allowsMultipleSelection = false
            picker.delegate = self
            present(picker, animated: true)
        }

        private func presentMultipleDocumentPicker(types: [UTType]) {
            let picker = UIDocumentPickerViewController(forOpeningContentTypes: types, asCopy: true)
            picker.allowsMultipleSelection = true
            picker.delegate = self
            present(picker, animated: true)
        }
    }

    extension ViewController: UIDocumentPickerDelegate {
        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            guard !urls.isEmpty else { return }

            if isPickingMultiple {
                viewModel.createMultiFileJob(jobType: "pdf_merge", fileURLs: urls)
            } else {
                guard let firstURL = urls.first, let jobType = pendingJobType else { return }
                viewModel.createSingleFileJob(jobType: jobType, fileURL: firstURL)
            }
        }
}

