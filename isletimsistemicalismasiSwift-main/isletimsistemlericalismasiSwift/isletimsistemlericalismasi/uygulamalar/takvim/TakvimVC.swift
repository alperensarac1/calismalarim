//
//  TakvimVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 31.01.2025.
//

import UIKit

class TakvimVC: UIViewController {
    @IBOutlet weak var calendar: UIDatePicker!
    @IBOutlet weak var tvCurrentDate: UILabel!
    
    @IBOutlet weak var tvPickedDate: UILabel!
    override func viewDidLoad() {
        super.viewDidLoad()
        updateCurrentDate()
        // Do any additional setup after loading the view.
    }
    

    @IBAction func calendarChanged(_ sender: Any) {
        let formatter = DateFormatter()
                formatter.dateFormat = "dd/MM/yyyy"
                tvPickedDate.text = "Seçilen Tarih: \(formatter.string(from: calendar.date))"
    }
    func updateCurrentDate() {
            let formatter = DateFormatter()
            formatter.dateFormat = "dd/MM/yyyy"
            tvCurrentDate.text = "Bugünün Tarihi: \(formatter.string(from: Date()))"
        }

}
