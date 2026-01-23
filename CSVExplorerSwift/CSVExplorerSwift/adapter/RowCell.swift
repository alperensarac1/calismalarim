import UIKit

final class RowCell: UITableViewCell {

    @IBOutlet private weak var lblTitle: UILabel!
    @IBOutlet private weak var lblSub: UILabel!
    @IBOutlet private weak var btnCopy: UIButton!

    var onCopyJson: (() -> Void)?

    override func awakeFromNib() {
        super.awakeFromNib()
        btnCopy.addTarget(self, action: #selector(copyTapped), for: .touchUpInside)
    }

    func bind(row: CsvRow) {
        let id = row.dict["id"] ?? ""
        let first = row.dict["first_name"] ?? row.dict["firstname"] ?? ""
        let last  = row.dict["last_name"] ?? row.dict["lastname"] ?? ""

        let title: String
        if !id.isEmpty, (!first.isEmpty || !last.isEmpty) { title = "#\(id)  \((first + " " + last).trimmingCharacters(in: .whitespaces))" }
        else if !id.isEmpty { title = "#\(id)" }
        else if !first.isEmpty || !last.isEmpty { title = (first + " " + last).trimmingCharacters(in: .whitespaces) }
        else { title = "Row" }

        lblTitle.text = title
        lblSub.text = subtitle(row.dict)
    }

    private func subtitle(_ d: [String: String]) -> String {
        let lastSeen = d["last_seen"] ?? ""
        let country = d["country_title"] ?? ""
        let city = d["city_title"] ?? ""
        var parts: [String] = []
        if !lastSeen.isEmpty { parts.append("Last seen: \(lastSeen)") }
        let loc = [country, city].filter { !$0.isEmpty }.joined(separator: " / ")
        if !loc.isEmpty { parts.append(loc) }
        return parts.isEmpty ? "Tap to view details" : parts.joined(separator: " • ")
    }

    @objc private func copyTapped() {
        onCopyJson?()
    }
}
