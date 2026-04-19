import UIKit

final class BoardCellCollectionViewCell: UICollectionViewCell {

    @IBOutlet weak var viewCell: UIView!

    override func awakeFromNib() {
        super.awakeFromNib()

        viewCell.layer.cornerRadius = 4
        viewCell.clipsToBounds = true

        // İlk açılışta boş hücre rengi
        viewCell.backgroundColor = UIColor(
            red: 217/255,
            green: 234/255,
            blue: 247/255,
            alpha: 1
        )
    }

    func configure(with cell: BoardCell) {
        switch cell.state {
        case .empty:
            viewCell.backgroundColor = UIColor(
                red: 217/255,
                green: 234/255,
                blue: 247/255,
                alpha: 1
            )

        case .ship:
            viewCell.backgroundColor = UIColor(
                red: 91/255,
                green: 124/255,
                blue: 153/255,
                alpha: 1
            )

        case .hit:
            viewCell.backgroundColor = .red

        case .miss:
            viewCell.backgroundColor = .white
        }
    }
}
