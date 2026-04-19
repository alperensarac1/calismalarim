//
//  ShareSheetHelper.swift
//  ResimArkaplanKaldirmaSwift
//
//  Created by Alperen Saraç on 1.04.2026.
//

import Foundation
import UIKit

enum ShareSheetHelper {
    static func present(from viewController: UIViewController, items: [Any]) {
        let vc = UIActivityViewController(activityItems: items, applicationActivities: nil)

        if let popover = vc.popoverPresentationController {
            popover.sourceView = viewController.view
            popover.sourceRect = CGRect(
                x: viewController.view.bounds.midX,
                y: viewController.view.bounds.midY,
                width: 1,
                height: 1
            )
        }

        viewController.present(vc, animated: true)
    }
}
