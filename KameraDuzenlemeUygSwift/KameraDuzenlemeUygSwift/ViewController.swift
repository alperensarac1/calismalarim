//
//  ViewController.swift
//  KameraDuzenlemeUygSwift
//
//  Created by Alperen Saraç on 5.08.2025.
//

import UIKit
import AVFoundation

class ViewController: UIViewController,AVCapturePhotoCaptureDelegate {
    
    @IBOutlet weak var imgFotograf: UIImageView!
    
    @IBOutlet weak var prevFotograf: UIView!
    
    var captureSession: AVCaptureSession?
    var videoPreviewLayer: AVCaptureVideoPreviewLayer?
    var photoOutput = AVCapturePhotoOutput()
    var capturedImage: UIImage? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        imgFotograf.isHidden = true
        checkCameraPermissionAndSetup()
    }

    @IBAction func btnFotografCek(_ sender: Any) {
        
        let settings = AVCapturePhotoSettings()
        photoOutput.capturePhoto(with: settings, delegate: self)
        
        
    }
    @IBAction func btnYaziEkle(_ sender: Any) {
        
        let alert = UIAlertController(title: "Yazı Ekle", message: "Fotoğrafın üzerine yazmak istediğiniz metni girin.", preferredStyle: .alert)

           alert.addTextField { textField in
               textField.placeholder = "Metin buraya..."
           }

           let addAction = UIAlertAction(title: "Ekle", style: .default) { _ in
               if let text = alert.textFields?.first?.text, !text.isEmpty {
                   self.addTextToImage(text: text)
               }
           }

           alert.addAction(addAction)
           alert.addAction(UIAlertAction(title: "İptal", style: .cancel, handler: nil))

           present(alert, animated: true)
        
    }
    @IBAction func btnKaydet(_ sender: Any) {
        
        guard let imageToSave = capturedImage else {
                showAlert(title: "Hata", message: "Kaydedilecek bir fotoğraf bulunamadı.")
                return
            }

            UIImageWriteToSavedPhotosAlbum(imageToSave, self, #selector(saveCompleted(_:didFinishSavingWithError:contextInfo:)), nil)
    }
    
    
    func checkCameraPermissionAndSetup() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            setupCamera()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                if granted {
                    DispatchQueue.main.async {
                        self.setupCamera()
                    }
                }
            }
        default:
            // Ayarlara yönlendirilebilir
            print("Kamera izni verilmedi.")
        }
    }
    func setupCamera() {
        captureSession = AVCaptureSession()
        captureSession?.sessionPreset = .photo

        guard let backCamera = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: backCamera),
              captureSession?.canAddInput(input) == true else {
            print("Kamera kullanılamıyor.")
            return
        }

        captureSession?.addInput(input)

        if captureSession?.canAddOutput(photoOutput) == true {
            captureSession?.addOutput(photoOutput)
        }

        videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession!)
        videoPreviewLayer?.videoGravity = .resizeAspectFill
        videoPreviewLayer?.frame = prevFotograf.bounds
        prevFotograf.layer.addSublayer(videoPreviewLayer!)

        captureSession?.startRunning()
    }

    func photoOutput(_ output: AVCapturePhotoOutput,
                     didFinishProcessingPhoto photo: AVCapturePhoto,
                     error: Error?) {

        if let error = error {
            print("Fotoğraf çekme hatası: \(error)")
            return
        }

        if let imageData = photo.fileDataRepresentation(),
           let image = UIImage(data: imageData) {

            // Kamerayı gizle, resmi göster
            prevFotograf.isHidden = true
            imgFotograf.isHidden = false
            imgFotograf.image = image

            // Hafızada sakla (yazı eklenecek ve kaydedilecek işlemler için)
            capturedImage = image
        }
    }

    func addTextToImage(text: String) {
        guard let image = capturedImage else { return }

        let textColor = UIColor.red
        let textFont = UIFont.boldSystemFont(ofSize: 40)

        let scale = UIScreen.main.scale
        UIGraphicsBeginImageContextWithOptions(image.size, false, scale)

        image.draw(in: CGRect(origin: .zero, size: image.size))

        let textFontAttributes: [NSAttributedString.Key: Any] = [
            .font: textFont,
            .foregroundColor: textColor
        ]

        // Yazının konumu (resmin üst kısmı – ayarlanabilir)
        let textRect = CGRect(x: image.size.width / 2,
                              y: 100,
                              width: image.size.width / 2,
                              height: 50)

        // Yazıyı çiz
        NSString(string: text).draw(in: textRect, withAttributes: textFontAttributes)

        // Yeni görseli al
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        // Yeni resmi göster
        if let newImage = newImage {
            capturedImage = newImage
            imgFotograf.image = newImage
        }
    }
    @objc func saveCompleted(_ image: UIImage, didFinishSavingWithError error: Error?, contextInfo: UnsafeRawPointer) {
        if let error = error {
            showAlert(title: "Kaydedilemedi", message: error.localizedDescription)
        } else {
            showAlert(title: "Başarılı", message: "Fotoğraf galeriye kaydedildi.")
        }
    }
    func showAlert(title: String, message: String) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(alert, animated: true)
    }

}

