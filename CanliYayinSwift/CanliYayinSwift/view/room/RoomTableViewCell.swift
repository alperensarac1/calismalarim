//
//  RoomTableViewCell.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation
import UIKit

final class RoomTableViewCell: UITableViewCell {

    static let identifier = "RoomTableViewCell"

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var broadcasterLabel: UILabel!
    @IBOutlet weak var viewerCountLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()

        titleLabel.font = .boldSystemFont(ofSize: 17)
        broadcasterLabel.font = .systemFont(ofSize: 14)
        viewerCountLabel.font = .systemFont(ofSize: 14)

        selectionStyle = .none
    }

    func configure(with room: RoomModel) {
        titleLabel.text = room.title
        broadcasterLabel.text = "Yayıncı: \(room.broadcasterName)"
        viewerCountLabel.text = "İzleyici: \(room.viewerCount)"
    }
}
