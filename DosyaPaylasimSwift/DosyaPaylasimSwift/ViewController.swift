//
//  ViewController.swift
//  DosyaPaylasimSwift
//
//  Created by Alperen Saraç on 7.09.2025.
//

import UIKit
import UniformTypeIdentifiers
class ViewController: UIViewController {

    // MARK: - State
    private let baseURL = URL(string: "https://alperensaracdeneme.com/api/")!
    private var selectedFileURL: URL?
    private var lastDownloadURL: String?
    
    @IBOutlet weak var tvKontrolSonucu: UITextView!
    @IBOutlet weak var etKod: UITextField!
    @IBOutlet weak var tvSeciliDosya: UILabel!
    override func viewDidLoad() {
           super.viewDidLoad()
           tvKontrolSonucu.text = ""
           tvSeciliDosya.text = "Seçili dosya: (yok)"
       }

    @IBAction func btnDosyaSec(_ sender: Any) {
        // Her tür dosyayı seçmek için .item (iOS 14+)
            let types: [UTType] = [.item]
            let picker = UIDocumentPickerViewController(forOpeningContentTypes: types, asCopy: false)
            picker.delegate = self
            picker.allowsMultipleSelection = false
            present(picker, animated: true)
    }
    
    @IBAction func btnDosyaYukle(_ sender: Any) {
        guard let fileURL = selectedFileURL else {
                    showToast("Önce dosya seçin")
                    return
                }
                tvKontrolSonucu.text = "Yükleniyor…"
                uploadFile(fileURL: fileURL) { [weak self] result in
                    DispatchQueue.main.async {
                        switch result {
                        case .success(let resp):
                            if resp.ok == true {
                                self?.lastDownloadURL = resp.downloadUrl
                                self?.tvKontrolSonucu.text =
        """
        Yüklendi!
        Kod: \(resp.code ?? "-")
        İndirme: \(resp.downloadUrl ?? "-")
        Bilgi: \(resp.infoUrl ?? "-")
        Geçerlilik: \(resp.expiresAt ?? "-")
        """
                                // Kodu inputa yaz
                                self?.etKod.text = resp.code
                                // İndirme linkini panoya kopyalamak istersen:
                                // UIPasteboard.general.string = resp.downloadUrl
                            } else {
                                self?.tvKontrolSonucu.text = "Hata: \(resp.error ?? "Bilinmeyen")"
                            }
                        case .failure(let err):
                            self?.tvKontrolSonucu.text = "İstek hatası: \(err.localizedDescription)"
                        }
                    }
                }
    }
    
    @IBAction func btnLinkiKontrolEt(_ sender: Any) {
        let code = (etKod.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
                guard code.range(of: #"^[A-Z0-9]{6}$"#, options: .regularExpression) != nil else {
                    showToast("Kod 6 haneli olmalı")
                    return
                }
                tvKontrolSonucu.text = "Sorgulanıyor…"
                getLink(code: code) { [weak self] result in
                    DispatchQueue.main.async {
                        switch result {
                        case .success(let link):
                            if link.ok == true {
                                if link.expired == true {
                                    self?.tvKontrolSonucu.text = "Kod: \(link.code ?? "-") — Süresi dolmuş veya pasif."
                                } else {
                                    self?.tvKontrolSonucu.text =
        """
        Kod: \(link.code ?? "-")
        Dosya: \(link.originalName ?? "-")
        Boyut: \(link.sizeBytes ?? 0)
        Son Kullanım: \(link.expiresAt ?? "-")
        Link: \(link.downloadUrl ?? "-")
        """
                                    self?.lastDownloadURL = link.downloadUrl
                                }
                            } else {
                                self?.tvKontrolSonucu.text = "Hata: \(link.error ?? "Bilinmeyen")"
                            }
                        case .failure(let err):
                            self?.tvKontrolSonucu.text = "İstek hatası: \(err.localizedDescription)"
                        }
                    }
                }
    }
    
    @IBAction func btnIndir(_ sender: Any) {
        // etKod ile indir (Android’deki gibi)
                let code = (etKod.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
                guard code.range(of: #"^[A-Z0-9]{6}$"#, options: .regularExpression) != nil else {
                    showToast("Kod 6 haneli olmalı")
                    return
                }
                let urlStr = "https://alperensaracdeneme.com/api/download.php?code=\(code)"
                guard let url = URL(string: urlStr) else { return }
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
    }
    
    private func uploadFile(fileURL: URL, completion: @escaping (Result<UploadResponse, Error>) -> Void) {
            let url = baseURL.appendingPathComponent("upload.php")
            var request = URLRequest(url: url)
            request.httpMethod = "POST"

            let boundary = "Boundary-\(UUID().uuidString)"
            request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

            // Dosya adı ve MIME
            let filename = fileURL.lastPathComponent
            let mime = mimeType(for: filename)

            // Gövde
            var body = Data()
            body.append("--\(boundary)\r\n")
            body.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\n")
            body.append("Content-Type: \(mime)\r\n\r\n")
            do {
                let fileData = try Data(contentsOf: fileURL)
                body.append(fileData)
            } catch {
                completion(.failure(error))
                return
            }
            body.append("\r\n")
            body.append("--\(boundary)--\r\n")

            request.httpBody = body

            URLSession.shared.dataTask(with: request) { data, res, err in
                if let err = err { completion(.failure(err)); return }
                guard let data = data else {
                    completion(.failure(NSError(domain: "Api", code: -1, userInfo: [NSLocalizedDescriptionKey: "No data"])))
                    return
                }
                do {
                    let decoded = try JSONDecoder().decode(UploadResponse.self, from: data)
                    completion(.success(decoded))
                } catch {
                    completion(.failure(error))
                }
            }.resume()
        }

        private func getLink(code: String, completion: @escaping (Result<LinkResponse, Error>) -> Void) {
            var comps = URLComponents(url: baseURL.appendingPathComponent("get-link.php"), resolvingAgainstBaseURL: false)!
            comps.queryItems = [URLQueryItem(name: "code", value: code)]
            guard let url = comps.url else {
                completion(.failure(NSError(domain: "Api", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"])))
                return
            }
            URLSession.shared.dataTask(with: url) { data, res, err in
                if let err = err { completion(.failure(err)); return }
                guard let data = data else {
                    completion(.failure(NSError(domain: "Api", code: -1, userInfo: [NSLocalizedDescriptionKey: "No data"])))
                    return
                }
                do {
                    let decoded = try JSONDecoder().decode(LinkResponse.self, from: data)
                    completion(.success(decoded))
                } catch {
                    completion(.failure(error))
                }
            }.resume()
        }

        // MARK: - Helpers

        private func showToast(_ msg: String) {
            // Basit uyarı; istersen custom toast yazılabilir
            let alert = UIAlertController(title: nil, message: msg, preferredStyle: .alert)
            present(alert, animated: true)
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
                alert.dismiss(animated: true)
            }
        }

        private func mimeType(for filename: String) -> String {
            let lower = filename.lowercased()
            if lower.hasSuffix(".jpg") || lower.hasSuffix(".jpeg") { return "image/jpeg" }
            if lower.hasSuffix(".png") { return "image/png" }
            if lower.hasSuffix(".pdf") { return "application/pdf" }
            if lower.hasSuffix(".mp4") { return "video/mp4" }
            return "application/octet-stream"
        }

        private func updateSelectedFileLabel(_ url: URL) {
            var sizeText = ""
            if let size = (try? FileManager.default.attributesOfItem(atPath: url.path)[.size]) as? NSNumber {
                sizeText = " — \(size.int64Value) bayt"
            }
            tvSeciliDosya.text = "Seçili dosya: \(url.lastPathComponent)\(sizeText)"
        }
    }

    // MARK: - Document Picker Delegate
    extension ViewController: UIDocumentPickerDelegate, UINavigationControllerDelegate {
        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            guard let url = urls.first else { return }
            // Security-scoped kaynaklara erişim (sandbox)
            let needsAccess = url.startAccessingSecurityScopedResource()
            selectedFileURL = url
            updateSelectedFileLabel(url)
            if needsAccess { url.stopAccessingSecurityScopedResource() }
        }

        func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
            // İptal edildi
        }
    }

    // MARK: - Small Data append helpers
    private extension Data {
        mutating func append(_ string: String) {
            if let d = string.data(using: .utf8) {
                append(d)
            }
        }
    }

