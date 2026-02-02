import UIKit

final class AddressCell: UITableViewCell {

    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var defaultBadgeLabel: UILabel!
    @IBOutlet private weak var addressLabel: UILabel!
    @IBOutlet private weak var setDefaultButton: UIButton!
    @IBOutlet private weak var deleteButton: UIButton!

    var onSetDefault: (() -> Void)?
    var onDelete: (() -> Void)?

    override func awakeFromNib() {
        super.awakeFromNib()
        defaultBadgeLabel.layer.cornerRadius = 8
        defaultBadgeLabel.clipsToBounds = true
        defaultBadgeLabel.textAlignment = .center
    }

    func bind(_ a: Address) {
        titleLabel.text = a.title
        addressLabel.text = "\(a.address_line)\n\(a.district) / \(a.city)"

        let isDef = a.is_default == 1
        defaultBadgeLabel.text = isDef ? " Varsayılan " : ""
        defaultBadgeLabel.isHidden = !isDef
        defaultBadgeLabel.isHidden = isDef
    }

    @IBAction private func setDefaultTapped(_ sender: UIButton) { onSetDefault?() }
    @IBAction private func deleteTapped(_ sender: UIButton) { onDelete?() }
}
