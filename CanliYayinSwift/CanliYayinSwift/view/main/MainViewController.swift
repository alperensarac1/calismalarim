//
//  MainViewController.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation
import UIKit

final class MainViewController: UIViewController {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var startBroadcastButton: UIButton!
    @IBOutlet weak var watchBroadcastsButton: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()
        configureUI()
    }

    private func configureUI() {
        view.backgroundColor = UIColor.systemBackground

        titleLabel.text = "Canlı Yayın Uygulaması"
        titleLabel.textAlignment = .center
        titleLabel.font = .boldSystemFont(ofSize: 26)

        startBroadcastButton.setTitle("Yayın Aç", for: .normal)
        watchBroadcastsButton.setTitle("Yayınları İzle", for: .normal)
    }

    @IBAction func startBroadcastTapped(_ sender: UIButton) {
        let vc = BroadcasterViewController(
            nibName: "BroadcasterViewController",
            bundle: nil
        )

        navigationController?.pushViewController(vc, animated: true)
    }

    @IBAction func watchBroadcastsTapped(_ sender: UIButton) {
        let vc = RoomListViewController(
            nibName: "RoomListViewController",
            bundle: nil
        )

        navigationController?.pushViewController(vc, animated: true)
    }
}
