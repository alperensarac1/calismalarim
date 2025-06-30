//
//  KameraVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 30.01.2025.
//

import UIKit

class KameraVC: UIViewController {

    @IBOutlet weak var imgKamera: UIImageView!
    override func viewDidLoad() {
        super.viewDidLoad()

       
    }
    

    @IBAction func btnKamera(_ sender: Any) {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = self
        present(picker, animated: true)
    }
    

}
extension KameraVC:UIImagePickerControllerDelegate,UINavigationControllerDelegate{
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        guard let image = info[UIImagePickerController.InfoKey.originalImage] as? UIImage else {return}
        imgKamera.image = image
    }
}
