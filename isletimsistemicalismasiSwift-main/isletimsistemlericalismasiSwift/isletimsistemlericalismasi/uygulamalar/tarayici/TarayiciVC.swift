//
//  TarayiciVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 31.01.2025.
//

import UIKit
import WebKit


class TarayiciVC: UIViewController {

    @IBOutlet weak var webView: WKWebView!
    @IBOutlet weak var etArama: UITextField!
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
    @IBAction func btnAra(_ sender: Any) {
        let aranacakMetin = etArama.text!
        let url = URL(string: "https://www.google.com/search?q=\(aranacakMetin)")
        webView.load(URLRequest(url: url!))
    }
    
}
