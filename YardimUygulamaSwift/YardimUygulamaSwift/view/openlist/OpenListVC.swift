//
//  OpenListVC.swift
//  YardimUygulamaSwift
//
//  Created by Alperen Saraç on 9.03.2026.
//

import Foundation
import UIKit

final class OpenListVC: UIViewController, UITableViewDataSource {

    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var lblInfo: UILabel!

    private let vm = OpenListVM()
    private var timer: Timer?
    private var items: [OpenHelpItem] = []
    private var helperId: Int { Session.userId() }

    override func viewDidLoad() {
        super.viewDidLoad()
        lblInfo.text = "Yenileniyor..."
        tableView.dataSource = self

        tableView.register(UINib(nibName: "OpenRequestCell", bundle: nil),
                           forCellReuseIdentifier: "OpenRequestCell")
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        startPolling()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopPolling()
    }

    private func startPolling() {
        stopPolling()
        timer = Timer.scheduledTimer(withTimeInterval: 4.0, repeats: true) { [weak self] _ in
            self?.fetch()
        }
        fetch()
    }

    private func stopPolling() {
        timer?.invalidate()
        timer = nil
    }

    private func fetch() {
        Task { @MainActor in
            let res = await vm.fetchOpen(helperId: helperId)
            switch res {
            case .success(let list):
                items = list
                lblInfo.text = "Bulunan: \(list.count)"
                tableView.reloadData()
            case .failure(let msg):
                lblInfo.text = msg.localizedDescription
            }
        }
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { items.count }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "OpenRequestCell", for: indexPath) as! OpenRequestCell
        cell.bind(item)
        cell.onAccept = { [weak self] in
            guard let self else { return }
            Task { @MainActor in
                let r = await self.vm.accept(requestId: item.id, helperId: self.helperId)
                if case .failure(let msg) = r { self.lblInfo.text = msg.localizedDescription }
                self.fetch()
                // Tab2'ye (Accepted) geçmek istersen:
                self.tabBarController?.selectedIndex = 1
            }
        }
        return cell
    }
}
