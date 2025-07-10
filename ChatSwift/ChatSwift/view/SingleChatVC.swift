//
//  SingleChatVC.swift
//  ChatSwift
//
//  Created by Alperen Saraç on 3.07.2025.
//

import UIKit
import Combine
class SingleChatVC: UIViewController {
    @IBOutlet weak var tableView: UITableView!
    
    @IBOutlet weak var ivOnizleme: UIImageView!
    @IBOutlet weak var btnMesajGonder: UIButton!
    @IBOutlet weak var btnResimGonder: UIButton!
    @IBOutlet weak var etMesaj: UITextField!
    var aliciId: Int = -1
    var aliciAd: String = ""
       
       private var viewModel = MesajlarViewModel()
       private var mesajlar: [Mesaj] = []
       private var secilenResimBase64: String? = nil
       private var bag = Set<AnyCancellable>()

       override func viewDidLoad() {
           super.viewDidLoad()
           title = aliciAd

           tableView.dataSource = self
           tableView.delegate = self
           tableView.tableFooterView = UIView()

           bindViewModel()
           viewModel.mesajlariYuklePeriyodik(gonderenId: AppConfig.kullaniciId, aliciId: aliciId)
       }

       private func bindViewModel() {
           viewModel.$mesajlar
               .receive(on: DispatchQueue.main)
               .sink { [weak self] liste in
                   self?.mesajlar = liste
                   self?.tableView.reloadData()
                   self?.scrollToBottom()
               }.store(in: &bag)

           viewModel.$hataMesaji
               .compactMap { $0 }
               .receive(on: DispatchQueue.main)
               .sink { [weak self] hata in
                   self?.showToast(hata)
               }.store(in: &bag)
       }

       private func scrollToBottom() {
           if mesajlar.isEmpty { return }
           let indexPath = IndexPath(row: mesajlar.count - 1, section: 0)
           tableView.scrollToRow(at: indexPath, at: .bottom, animated: true)
       }

       @IBAction func btnGonderTapped(_ sender: UIButton) {
           let mesajText = etMesaj.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
           if mesajText.isEmpty && secilenResimBase64 == nil {
               showToast("Boş mesaj gönderilemez")
               return
           }

           viewModel.mesajGonder(
               gonderenId: AppConfig.kullaniciId,
               aliciId: aliciId,
               mesajText: mesajText,
               base64Image: secilenResimBase64
           )

           etMesaj.text = ""
           ivOnizleme.image = nil
           ivOnizleme.isHidden = true
           secilenResimBase64 = nil
       }

       @IBAction func btnResimSecTapped(_ sender: UIButton) {
           let picker = UIImagePickerController()
           picker.sourceType = .photoLibrary
           picker.delegate = self
           present(picker, animated: true)
       }

       private func showToast(_ msg: String) {
           let alert = UIAlertController(title: nil, message: msg, preferredStyle: .alert)
           present(alert, animated: true)
           DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { alert.dismiss(animated: true) }
       }

       private func imageToBase64(_ image: UIImage) -> String? {
           if let data = image.jpegData(compressionQuality: 0.6) {
               return data.base64EncodedString()
           }
           return nil
       }
   }

   extension SingleChatVC: UITableViewDataSource, UITableViewDelegate {
       func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
           mesajlar.count
       }

       func tableView(_ tableView: UITableView,
                      cellForRowAt indexPath: IndexPath) -> UITableViewCell {
           let cell = tableView.dequeueReusableCell(withIdentifier: "SingleChatCell", for: indexPath) as! SingleChatCell
           cell.configure(with: mesajlar[indexPath.row])
           return cell
       }
   }

   extension SingleChatVC: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
       func imagePickerController(_ picker: UIImagePickerController,
                                  didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
           if let secilen = info[.originalImage] as? UIImage {
               ivOnizleme.image = secilen
               ivOnizleme.isHidden = false
               secilenResimBase64 = imageToBase64(secilen)
           }
           picker.dismiss(animated: true)
       }
   }
