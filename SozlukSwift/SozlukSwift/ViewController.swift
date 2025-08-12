//
//  ViewController.swift
//  SozlukSwift
//
//  Created by Alperen Saraç on 8.08.2025.
//

import UIKit

class ViewController: UIViewController {

    private let testApi = TestApi()

    override func viewDidLoad() {
        super.viewDidLoad()

        // Log’u ekranda görmek istersen:
        

        // Testleri başlat
        testApi.testLogin()                 // kullanıcı adı/şifreyi gerekirse güncelle
        testApi.testEntryList()
        testApi.testSingleEntry(entryId: 1) // istediğin ID

        // NOT: iOS’ta RunLoop.main.run() kullanma — ana thread’i kilitler.
    }


}

