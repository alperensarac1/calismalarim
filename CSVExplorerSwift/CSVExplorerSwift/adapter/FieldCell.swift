import UIKit

final class FieldCell: UITableViewCell {
    @IBOutlet private weak var lblKey: UILabel!
    @IBOutlet private weak var lblValue: UILabel!

    func bind(item: FieldItem) {
        lblKey.text = item.key
        lblValue.text = item.value
    }
}
