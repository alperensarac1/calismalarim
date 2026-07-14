import UIKit

/*
    SceneDelegate

    Storyboard kullanmadığımız için uygulamanın ilk ekranını burada kodla açıyoruz.

    Eğer kullanıcı daha önce giriş yaptıysa:
    - HomeViewController açılacak.

    Eğer kullanıcı giriş yapmadıysa:
    - LoginViewController açılacak.

    Şimdilik HomeViewController'ı sonraki adımda yapacağımız için
    her durumda LoginViewController açıyoruz.
*/
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

        /*
            XIB dosyasından LoginViewController açıyoruz.

            LoginViewController.swift
            LoginViewController.xib

            İsimler aynı olduğu için:
            LoginViewController()
            dediğimizde XIB otomatik bulunur.
        */
        let loginVC = LoginViewController()

        /*
            NavigationController kullanıyoruz.
            Böylece ekran geçişleri daha düzenli olur.
        */
        let navigationController = UINavigationController(rootViewController: loginVC)

        window.rootViewController = navigationController
        window.makeKeyAndVisible()

        self.window = window
    }
}
