import UIKit

final class ShipmentCell: UITableViewCell {

    @IBOutlet private weak var trackingLabel: UILabel!
    @IBOutlet private weak var statusLabel: UILabel!
    @IBOutlet private weak var peerLabel: UILabel!
    @IBOutlet private weak var metaLabel: UILabel!

    func bind(_ s: Shipment) {
        trackingLabel.text = "#\(s.id)"
        statusLabel.text = s.status

        if s.role == "RECEIVER" {
            if s.visible {
                peerLabel.text = s.sender_initials ?? "Gönderici"
            } else {
                peerLabel.text = "Bu gönderi henüz görünür değil"
            }
        } else {
            peerLabel.text = "Gönderilen"
        }

        var metaParts: [String] = []
        metaParts.append(s.created_at)

        if let name = s.cargo_company_name, !name.isEmpty {
            metaParts.append(name)
        }

        metaLabel.text = metaParts.joined(separator: " • ")
        accessoryType = .disclosureIndicator
    }
}
