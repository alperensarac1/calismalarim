//
//  GaleriVC.swift
//  isletimsistemlericalismasi
//
//  Created by Alperen Saraç on 30.01.2025.
//

import UIKit
import PhotosUI
class GaleriVC: UIViewController,PHPickerViewControllerDelegate {

    @IBOutlet weak var imgGaleri: UIImageView!
    override func viewDidLoad() {
        super.viewDidLoad()

        checkPhotoLibraryPermission()
        
    }
    

   
    @IBAction func btnGaleri(_ sender: Any) {
         presentPicker()
    }
    
    func presentPicker() {
            var config = PHPickerConfiguration()
            config.selectionLimit = 1  // Seçilecek fotoğraf sayısı
            config.filter = .images    // Sadece fotoğrafları göster

            let picker = PHPickerViewController(configuration: config)
            picker.delegate = self
            present(picker, animated: true)
        }

        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            picker.dismiss(animated: true)

            if let result = results.first {
                let itemProvider = result.itemProvider
                if itemProvider.canLoadObject(ofClass: UIImage.self) {
                    itemProvider.loadObject(ofClass: UIImage.self) { (image, error) in
                        DispatchQueue.main.async {
                            if let selectedImage = image as? UIImage {
                                self.imgGaleri.image = selectedImage
                            }
                        }
                    }
                }
            }
        }
    func checkPhotoLibraryPermission() {
        let status = PHPhotoLibrary.authorizationStatus()
        
        switch status {
        case .notDetermined:
            PHPhotoLibrary.requestAuthorization { newStatus in
                if newStatus == .authorized {
                    print("İzin verildi!")
                }
            }
        case .authorized, .limited:
            print("Galeriyi kullanabilirsiniz.")
        case .denied, .restricted:
            print("İzin verilmedi!")
        @unknown default:
            break
        }
    }
}
