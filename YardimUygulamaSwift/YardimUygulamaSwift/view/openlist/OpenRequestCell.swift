//
//  OpenRequestCell.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation
import UIKit

final class OpenRequestCell: UITableViewCell {
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblAge: UILabel!
    @IBOutlet weak var lblTime: UILabel!
    @IBOutlet weak var btnAccept: UIButton!

    var onAccept: (() -> Void)?

    @IBAction func acceptTapped(_ sender: UIButton) { onAccept?() }

    func bind(_ item: OpenHelpItem) {
        lblName.text = item.patient_name ?? "-"
        lblAge.text = "Yaş: \(item.patient_age ?? 0)"
        lblTime.text = item.created_at ?? "-"
    }
}
