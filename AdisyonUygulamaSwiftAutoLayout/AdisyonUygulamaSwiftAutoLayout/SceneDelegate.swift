//
//  SceneDelegate.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 6.06.2025.
//

import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
      _ scene: UIScene,
      willConnectTo session: UISceneSession,
      options connectionOptions: UIScene.ConnectionOptions
    ) {
        // 1) Convert to UIWindowScene
        guard let windowScene = scene as? UIWindowScene else { return }

        // 2) Create the window for that scene
        let window = UIWindow(windowScene: windowScene)

        // 3) Build your root view controller
        let mainVC = MainViewController()
        let nav = UINavigationController(rootViewController: mainVC)
        window.rootViewController = nav

        // 4) Make it visible
        self.window = window
        window.makeKeyAndVisible()
    }
}
