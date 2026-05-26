import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else {
            return
        }

        let window = UIWindow(windowScene: windowScene)

        let homeVC = HomeViewController(
            nibName: "HomeViewController",
            bundle: nil
        )

        let navigationController = MainNavigationController(
            rootViewController: homeVC
        )

        window.rootViewController = navigationController
        window.makeKeyAndVisible()

        self.window = window
    }
}
