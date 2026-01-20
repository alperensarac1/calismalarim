import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene,
               willConnectTo session: UISceneSession,
               options connectionOptions: UIScene.ConnectionOptions) {

        guard let windowScene = (scene as? UIWindowScene) else { return }

        let window = UIWindow(windowScene: windowScene)

        // İlk ekranın XIB’li VC’si
        let homeVC = HomeViewController() // init nibName ile birazdan ayarlayacağız
        let nav = UINavigationController(rootViewController: homeVC)

        window.rootViewController = nav
        window.makeKeyAndVisible()
        self.window = window
    }
    
}
