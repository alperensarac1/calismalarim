//
//  HomeViewController.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 21.05.2026.
//

import Foundation
import UIKit

final class HomeViewController: UIViewController {

    /*
        Ana ekran.

        Kullanıcı burada:
        - Oda oluştur
        - Odaya giriş yap

        seçeneklerinden birini seçer.
    */

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var createRoomButton: UIButton!
    @IBOutlet weak var joinRoomButton: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()
    }

    private func configureUI() {
        view.backgroundColor = UIColor(
            red: 248 / 255,
            green: 250 / 255,
            blue: 252 / 255,
            alpha: 1
        )

        title = "Canlı Quiz"

        titleLabel.text = "Canlı Quiz"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 34)
        titleLabel.textAlignment = .center
        titleLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        subtitleLabel.text = "Oda oluştur veya oda kodu ile quize katıl."
        subtitleLabel.font = UIFont.systemFont(ofSize: 16)
        subtitleLabel.textAlignment = .center
        subtitleLabel.textColor = UIColor(red: 107/255, green: 114/255, blue: 128/255, alpha: 1)

        stylePrimaryButton(createRoomButton, title: "Oda Oluştur")
        stylePrimaryButton(joinRoomButton, title: "Odaya Giriş Yap")
    }

    private func stylePrimaryButton(_ button: UIButton, title: String) {
        button.setTitle(title, for: .normal)
        button.backgroundColor = UIColor.systemPurple
        button.tintColor = .white
        button.layer.cornerRadius = 12
        button.titleLabel?.font = UIFont.boldSystemFont(ofSize: 17)
    }

    @IBAction func createRoomButtonTapped(_ sender: UIButton) {
        let vc = CreateRoomViewController(
            nibName: "CreateRoomViewController",
            bundle: nil
        )

        navigationController?.pushViewController(vc, animated: true)
    }

    @IBAction func joinRoomButtonTapped(_ sender: UIButton) {
        let vc = JoinRoomViewController(
            nibName: "JoinRoomViewController",
            bundle: nil
        )

        navigationController?.pushViewController(vc, animated: true)
    }
}
