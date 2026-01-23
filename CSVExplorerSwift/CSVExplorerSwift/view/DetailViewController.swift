import UIKit

final class DetailViewController: UIViewController {

    var rowJson: String = "{}"
    var headers: [String] = []

    @IBOutlet private weak var tfSearch: UITextField!
    @IBOutlet private weak var lblCount: UILabel!
    @IBOutlet private weak var btnCopyJson: UIButton!
    @IBOutlet private weak var btnCopyCsv: UIButton!
    @IBOutlet private weak var tableView: UITableView!

    private var allFields: [FieldItem] = []
    private var filtered: [FieldItem] = []

    override func loadView() {
        let v = Bundle.main.loadNibNamed("DetailViewController", owner: self, options: nil)!.first as! UIView
        self.view = v
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Details"

        tableView.register(UINib(nibName: "FieldCell", bundle: nil), forCellReuseIdentifier: "FieldCell")
        tableView.dataSource = self

        tfSearch.addTarget(self, action: #selector(searchChanged), for: .editingChanged)

        btnCopyJson.addTarget(self, action: #selector(copyJson), for: .touchUpInside)
        btnCopyCsv.addTarget(self, action: #selector(copyCsv), for: .touchUpInside)

        buildFields()
        apply(q: "")
    }

    private func buildFields() {
        let obj = (try? JSONSerialization.jsonObject(with: Data(rowJson.utf8), options: [])) as? [String: Any] ?? [:]
        allFields = []

        if !headers.isEmpty {
            for h in headers {
                let v = (obj[h] as? String) ?? "\(obj[h] ?? "")"
                allFields.append(FieldItem(key: h, value: v.isEmpty ? "-" : v))
            }
            let extras = obj.keys.filter { !headers.contains($0) }.sorted()
            for k in extras {
                let v = (obj[k] as? String) ?? "\(obj[k] ?? "")"
                allFields.append(FieldItem(key: k, value: v.isEmpty ? "-" : v))
            }
        } else {
            for k in obj.keys.sorted() {
                let v = (obj[k] as? String) ?? "\(obj[k] ?? "")"
                allFields.append(FieldItem(key: k, value: v.isEmpty ? "-" : v))
            }
        }
    }

    private func apply(q: String) {
        if q.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            filtered = allFields
        } else {
            let qq = q.lowercased()
            filtered = allFields.filter { $0.key.lowercased().contains(qq) || $0.value.lowercased().contains(qq) }
        }
        lblCount.text = "\(filtered.count) fields"
        tableView.reloadData()
    }

    @objc private func searchChanged() {
        apply(q: tfSearch.text ?? "")
    }

    @objc private func copyJson() {
        UIPasteboard.general.string = rowJson
        lblCount.text = "Copied JSON"
    }

    @objc private func copyCsv() {
        UIPasteboard.general.string = buildCsv()
        lblCount.text = "Copied CSV row"
    }

    private func buildCsv() -> String {
        guard !headers.isEmpty else { return rowJson }
        let obj = (try? JSONSerialization.jsonObject(with: Data(rowJson.utf8), options: [])) as? [String: Any] ?? [:]
        let headerLine = headers.joined(separator: ",")
        let rowLine = headers.map { esc("\(obj[$0] ?? "")") }.joined(separator: ",")
        return headerLine + "\n" + rowLine
    }

    private func esc(_ value0: String) -> String {
        var v = value0
        let needsQuotes = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")
        v = v.replacingOccurrences(of: "\"", with: "\"\"")
        if needsQuotes { v = "\"\(v)\"" }
        return v
    }
}

extension DetailViewController: UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { filtered.count }
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let it = filtered[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "FieldCell", for: indexPath) as! FieldCell
        cell.bind(item: it)
        return cell
    }
}
