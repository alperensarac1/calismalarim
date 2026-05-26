//
//  MainNavigationController.swift
//  CanliQuizSwift
//
//  Created by Alperen Saraç on 21.05.2026.
//

import Foundation
import UIKit

final class MainNavigationController: UINavigationController {

    override func viewDidLoad() {
        super.viewDidLoad()

        navigationBar.prefersLargeTitles = false
        navigationBar.tintColor = UIColor.systemPurple
    }
}
