//
//  ChatTableViewCell.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation
import UIKit

final class ChatTableViewCell: UITableViewCell {

    static let identifier = "ChatTableViewCell"

    @IBOutlet weak var usernameLabel: UILabel!
    @IBOutlet weak var messageLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()

        selectionStyle = .none

        usernameLabel.font = .boldSystemFont(ofSize: 13)
        usernameLabel.textColor = .systemBlue

        messageLabel.font = .systemFont(ofSize: 14)
        messageLabel.textColor = .label
        messageLabel.numberOfLines = 0
    }

    func configure(with chat: ChatMessageModel) {
        usernameLabel.text = chat.username
        messageLabel.text = chat.message
    }
}
