//
//  MainViewController.swift
//  CSVExplorerSwift
//
//  Created by Alperen Saraç on 22.01.2026.
//

import UIKit
import UniformTypeIdentifiers

final class MainViewController: UIViewController {

    // XIB Outlets
    @IBOutlet private weak var btnSelect: UIButton!
    @IBOutlet private weak var btnUpload: UIButton!
    @IBOutlet private weak var btnFilter: UIButton!
    @IBOutlet private weak var btnClear: UIButton!
    @IBOutlet private weak var btnClearDb: UIButton!

    @IBOutlet private weak var tfSearch: UITextField!
    @IBOutlet private weak var tvInfo: UILabel!
    @IBOutlet private weak var columnPicker: UIPickerView!
    @IBOutlet private weak var tableView: UITableView!

    private var headers: [String] = []
    private var allRows: [CsvRow] = []
    private var filtered: [CsvRow] = []

    private var selectedColumn: String = "ALL_COLUMNS"
    private var lastPickedFileUrl: URL?

    override func loadView() {
        // ✅ View XIB’den gelsin
        let v = Bundle.main.loadNibNamed("MainViewController", owner: self, options: nil)!.first as! UIView
        self.view = v
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "CSV Explorer"

        tableView.register(UINib(nibName: "RowCell", bundle: nil), forCellReuseIdentifier: "RowCell")
        tableView.dataSource = self
        tableView.delegate = self

        columnPicker.dataSource = self
        columnPicker.delegate = self

        tfSearch.addTarget(self, action: #selector(onSearchChanged), for: .editingChanged)

        btnSelect.addTarget(self, action: #selector(onSelectCsv), for: .touchUpInside)
        btnFilter.addTarget(self, action: #selector(onFilter), for: .touchUpInside)
        btnClear.addTarget(self, action: #selector(onClearFilter), for: .touchUpInside)
        btnClearDb.addTarget(self, action: #selector(onClearDb), for: .touchUpInside)
        btnUpload.addTarget(self, action: #selector(onUpload), for: .touchUpInside)

        refreshInfo()
        updateUploadEnabled()
    }

    private func refreshInfo() {
        tvInfo.text = "\(filtered.count) records"
    }

    private func updateUploadEnabled() {
        btnUpload.isEnabled = (lastPickedFileUrl != nil)
        btnUpload.alpha = btnUpload.isEnabled ? 1.0 : 0.5
    }

    @objc private func onSelectCsv() {
        let types: [UTType] = [.commaSeparatedText, .plainText, .text]
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: types, asCopy: true)
        picker.delegate = self
        present(picker, animated: true)
    }

    @objc private func onFilter() {
        let q = (tfSearch.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        if q.isEmpty {
            filtered = allRows
        } else if selectedColumn == "ALL_COLUMNS" {
            filtered = allRows.filter { $0.json.localizedCaseInsensitiveContains(q) }
        } else {
            filtered = allRows.filter {
                ($0.dict[selectedColumn] ?? "").localizedCaseInsensitiveContains(q)
            }
        }
        tableView.reloadData()
        tvInfo.text = "\(filtered.count) records (filter: \(selectedColumn))"
    }

    @objc private func onClearFilter() {
        tfSearch.text = ""
        selectedColumn = "ALL_COLUMNS"
        columnPicker.selectRow(0, inComponent: 0, animated: true)
        filtered = allRows
        tableView.reloadData()
        refreshInfo()
    }

    @objc private func onClearDb() {
        headers = []
        allRows = []
        filtered = []
        lastPickedFileUrl = nil
        selectedColumn = "ALL_COLUMNS"
        tfSearch.text = ""
        columnPicker.reloadAllComponents()
        tableView.reloadData()
        tvInfo.text = "Database cleared"
        updateUploadEnabled()
    }

    @objc private func onUpload() {
        guard let url = lastPickedFileUrl else { return }

        Task {
            do {
                let downloadUrl = try await UploadClient.uploadCsv(fileUrl: url)
                await MainActor.run {
                    UIApplication.shared.open(downloadUrl, options: [:])
                }
            } catch {
                await MainActor.run {
                    showAlert("Upload error", "\(error.localizedDescription)")
                }
            }
        }
    }


    @objc private func onSearchChanged() {
        // istersen anlık filtre
    }

    private func showAlert(_ title: String, _ msg: String) {
        let a = UIAlertController(title: title, message: msg, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "OK", style: .default))
        present(a, animated: true)
    }

    private func loadCsv(from url: URL) {
        do {
            let text = try String(contentsOf: url, encoding: .utf8)
            let res = CsvParser.parse(text: text)
            headers = res.headers
            allRows = res.rows
            filtered = allRows

            selectedColumn = "ALL_COLUMNS"
            columnPicker.reloadAllComponents()
            tableView.reloadData()

            tvInfo.text = "Imported: \(res.rows.count) rows"
        } catch {
            showAlert("Import error", "\(error)")
        }
    }
}

extension MainViewController: UIDocumentPickerDelegate {
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let url = urls.first else { return }

        let canAccess = url.startAccessingSecurityScopedResource()
        defer { if canAccess { url.stopAccessingSecurityScopedResource() } }

        lastPickedFileUrl = url
        updateUploadEnabled()
        loadCsv(from: url)
    }

}
extension MainViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { filtered.count }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = filtered[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "RowCell", for: indexPath) as! RowCell
        cell.bind(row: item)
        cell.onCopyJson = { [weak self] in
            UIPasteboard.general.string = item.json
            self?.tvInfo.text = "Copied JSON"
        }
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let item = filtered[indexPath.row]
        let sb = UIStoryboard(name: "Main", bundle: nil)
        let vc = sb.instantiateViewController(withIdentifier: "DetailViewController") as! DetailViewController
        vc.rowJson = item.json
        vc.headers = headers
        navigationController?.pushViewController(vc, animated: true)
    }
}

extension MainViewController: UIPickerViewDataSource, UIPickerViewDelegate {
    func numberOfComponents(in pickerView: UIPickerView) -> Int { 1 }

    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return 1 + headers.count // ALL_COLUMNS + headers
    }

    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if row == 0 { return "ALL_COLUMNS" }
        return headers[row - 1]
    }

    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        selectedColumn = (row == 0) ? "ALL_COLUMNS" : headers[row - 1]
    }
}
