//
//  HistoryVC.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation
import UIKit

final class HistoryVC: UIViewController, UITableViewDataSource {

    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var lblInfo: UILabel!

    private let vm = HistoryVM()
    private var items: [ConfirmedHelpItem] = []
    private var helperId: Int { Session.userId() }

    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.dataSource = self
        tableView.register(UINib(nibName: "HistoryCell", bundle: nil),
                           forCellReuseIdentifier: "HistoryCell")
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        fetch()
    }

    private func fetch() {
        lblInfo.text = "Yükleniyor..."
        Task { @MainActor in
            let res = await vm.fetchHistory(helperId: helperId)
            switch res {
            case .success(let list):
                items = list
                lblInfo.text = "Toplam: \(list.count)"
                tableView.reloadData()
            case .failure(let msg):
                lblInfo.text = msg.localizedDescription
            }
        }
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { items.count }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "HistoryCell", for: indexPath) as! HistoryCell
        cell.bind(item)
        return cell
    }
}
