//
//  HistoryCell.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation
import UIKit

final class HistoryCell: UITableViewCell {
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblPhone: UILabel!
    @IBOutlet weak var lblService: UILabel!
    @IBOutlet weak var lblRoom: UILabel!
    @IBOutlet weak var lblTime: UILabel!

    func bind(_ item: ConfirmedHelpItem) {
        lblName.text = item.patient_name ?? "-"
        lblPhone.text = "Telefon: \(item.patient_phone ?? "-")"
        lblService.text = "Servis: \(item.servis_adi ?? "-")"
        lblRoom.text = "Oda: \(item.oda_no ?? "-")"
        lblTime.text = item.confirmed_at ?? "-"
    }
}
