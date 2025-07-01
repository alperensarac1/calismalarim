//
//  MainController.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 7.06.2025.
//

import Foundation
import UIKit
import Combine

class MainViewController: UIViewController {
    // MARK: - UI & Layout
    private var mainLayout: MainLayout!
    
    // MARK: - ViewModels
    private let masalarViewModel = MasalarViewModel()
    private let urunViewModel = UrunViewModel()
    
    // MARK: - Combine Subscriptions
    private var subscriptions = Set<AnyCancellable>()
    
    // MARK: - Image Picker
    private var imageBase64: String?
    private lazy var imagePicker: UIImagePickerController = {
        let picker = UIImagePickerController()
        picker.delegate = self
        picker.mediaTypes = ["public.image"]
        return picker
    }()
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white

        // 1) Setup layout container
        setupMainLayout()
        
        // Disable autoresizing masks and pin `mainLayout` on all sides
        mainLayout.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            mainLayout.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10),
            mainLayout.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            mainLayout.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),
            mainLayout.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -10)
        ])

        // 2) Observe view model outputs
        bindViewModels()

        // 3) Trigger initial loads
        Task {
            await masalarViewModel.masalariYukle()
            await urunViewModel.kategorileriYukle()
        }
    }

    
    private func setupMainLayout() {
        // instantiate MainLayout with placeholder empty list; we'll update when data arrives
        mainLayout = MainLayout(
            masaListesi: [],
            viewModel: MasaDetayViewModel(masaId: 0),
            onMasaClick: { [weak self] masa in
                self?.showMasaDetail(masa)
            }
        )
        
        view.addSubview(mainLayout)
        NSLayoutConstraint.activate([
            mainLayout.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            mainLayout.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            mainLayout.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            mainLayout.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        // add bottom action buttons
        addActionButtons()
    }
    
    private func bindViewModels() {
        masalarViewModel.$masalar
          .receive(on: DispatchQueue.main)
          .sink { [weak self] masalar in
              self?.mainLayout.updateMasaList(masalar)
          }
          .store(in: &subscriptions)
        urunViewModel.$kategoriler
            .receive(on: DispatchQueue.main)
            .sink { [weak self] kategoriler in
                self?.mainLayout.updateCategories(kategoriler)
            }
            .store(in: &subscriptions)
    }
    
    private func addActionButtons() {
        let buttonContainer = UIStackView()
        buttonContainer.axis = .horizontal
        buttonContainer.distribution = .fillEqually
        buttonContainer.spacing = 10
        buttonContainer.translatesAutoresizingMaskIntoConstraints = false
        
        let btnMasa = makeButton(title: "Masa İşlemleri") { [weak self] in
            self?.showMasaEkleSilDialog()
        }
        let btnUrun = makeButton(title: "Ürün İşlemleri") { [weak self] in
            guard let cats = self?.urunViewModel.kategoriler else { return }
            self?.showUrunEkleSilDialog(categories: cats)
        }
        let btnBirlestir = makeButton(title: "Masa Birleştir") { [weak self] in
            self?.showMasaBirlestirDialog()
        }
        let btnKategori = makeButton(title: "Kategori İşlemleri") { [weak self] in
            guard let cats = self?.urunViewModel.kategoriler else { return }
            self?.showKategoriEkleSilDialog(categories: cats)
        }
        
        [btnMasa, btnUrun, btnBirlestir, btnKategori].forEach { buttonContainer.addArrangedSubview($0) }
        view.addSubview(buttonContainer)
        NSLayoutConstraint.activate([
            buttonContainer.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            buttonContainer.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),
            buttonContainer.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -10),
            buttonContainer.heightAnchor.constraint(equalToConstant: 44)
        ])
    }
    
    private func makeButton(title: String, action: @escaping ()->Void) -> UIButton {
        let btn = UIButton(type: .system)
        btn.setTitle(title, for: .normal)
        btn.addAction(UIAction { _ in action() }, for: .touchUpInside)
        return btn
    }
    
    
    private func showMasaEkleSilDialog() {
        let alert = UIAlertController(title: "Masa İşlemleri",
                                      message: "Yeni masa ekleyebilir veya ID girerek silebilirsiniz.",
                                      preferredStyle: .alert)
        alert.addTextField {
            $0.placeholder = "Silinecek masa ID"
            $0.keyboardType = .numberPad
        }
        alert.addAction(.init(title: "Masa Ekle", style: .default) { _ in
            self.masalarViewModel.masaEkle {
                self.masalarViewModel.masalariYukle()
            }
        })
        alert.addAction(.init(title: "Masa Sil", style: .destructive) { _ in
            if let text = alert.textFields?.first?.text,
               let id = Int(text) {
                self.masalarViewModel.masaSil(masaId: id)
            }
        })
        present(alert, animated: true)
    }
    
    private func showMasaBirlestirDialog() {
        let alert = UIAlertController(title: "Masa Birleştir", message: nil, preferredStyle: .alert)
        alert.addTextField {
            $0.placeholder = "Ana Masa ID"
            $0.keyboardType = .numberPad
        }
        alert.addTextField {
            $0.placeholder = "Birlestirilecek Masa ID"
            $0.keyboardType = .numberPad
        }
        alert.addAction(.init(title: "Birleştir", style: .default) { _ in
            if let a = alert.textFields?[0].text, let ana = Int(a),
               let b = alert.textFields?[1].text, let birles = Int(b) {
                self.masalarViewModel.masaBirlestir(anaId: ana, bId: birles)
                self.masalarViewModel.masalariYukle()
            }
        })
        present(alert, animated: true)
    }
    
    private func showKategoriEkleSilDialog(categories: [Kategori]) {
        let alert = UIAlertController(title: "Kategori İşlemleri",
                                      message: "\n\n\n\n\n\n",
                                      preferredStyle: .alert)

        alert.addTextField { tf in
            tf.placeholder = "Yeni Kategori Adı"
        }

        let picker = UIPickerView()
        picker.dataSource = self
        picker.delegate = self

        let pickerVC = UIViewController()
        pickerVC.view.addSubview(picker)
        picker.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            picker.topAnchor.constraint(equalTo: pickerVC.view.topAnchor),
            picker.leadingAnchor.constraint(equalTo: pickerVC.view.leadingAnchor),
            picker.trailingAnchor.constraint(equalTo: pickerVC.view.trailingAnchor),
            picker.bottomAnchor.constraint(equalTo: pickerVC.view.bottomAnchor)
        ])
        pickerVC.preferredContentSize = CGSize(width: 250, height: 150)

        // 3) Alert’e içerik olarak ata
        alert.setValue(pickerVC, forKey: "contentViewController")

        // 4) Butonlar
        alert.addAction(UIAlertAction(title: "Kaydet", style: .default) { _ in
            guard let ad = alert.textFields?.first?.text, !ad.isEmpty else { return }
            Task {
                await self.urunViewModel.kategoriEkle(ad: ad)
                await self.urunViewModel.kategorileriYukle()
            }
        })
        alert.addAction(UIAlertAction(title: "Sil", style: .destructive) { _ in
            let index = picker.selectedRow(inComponent: 0)
            let id = categories[index].id
            self.urunViewModel.kategoriSil(id: id)
        })
        alert.addAction(UIAlertAction(title: "İptal", style: .cancel))

        present(alert, animated: true)
    }

    
    private func showUrunEkleSilDialog(categories: [Kategori]) {
        // 1) Alert’i oluştur, message’e picker için boşluk ekle
        let alert = UIAlertController(
            title: "Ürün İşlemleri",
            message: "\n\n\n\n\n\n",
            preferredStyle: .alert
        )

        // 2) TextField’ları ekle
        alert.addTextField { tf in
            tf.placeholder = "Ürün Adı"
        }
        alert.addTextField { tf in
            tf.placeholder = "Fiyat"
            tf.keyboardType = .decimalPad
        }

        let picker = UIPickerView()
        picker.dataSource = self
        picker.delegate = self

        let pickerVC = UIViewController()
        pickerVC.preferredContentSize = CGSize(width: 250, height: 100)
        pickerVC.view.addSubview(picker)
        picker.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            picker.topAnchor.constraint(equalTo: pickerVC.view.topAnchor),
            picker.leadingAnchor.constraint(equalTo: pickerVC.view.leadingAnchor),
            picker.trailingAnchor.constraint(equalTo: pickerVC.view.trailingAnchor),
            picker.bottomAnchor.constraint(equalTo: pickerVC.view.bottomAnchor)
        ])

        alert.setValue(pickerVC, forKey: "contentViewController")

        alert.addAction(UIAlertAction(title: "Resim Seç", style: .default) { _ in
            self.present(self.imagePicker, animated: true)
        })
        alert.addAction(UIAlertAction(title: "Ekle", style: .default) { _ in
            guard
                let ad = alert.textFields?[0].text, !ad.isEmpty,
                let fiyatText = alert.textFields?[1].text, let fiyat = Float(fiyatText),
                let base64 = self.imageBase64
            else { return }

            let catIndex = picker.selectedRow(inComponent: 0)
            let catId = categories[catIndex].id
            self.urunViewModel.urunEkle(ad: ad, fiyat: fiyat, resimBase64: base64, kategoriId: catId)
        })
        alert.addAction(UIAlertAction(title: "Sil", style: .destructive) { _ in
            if let ad = alert.textFields?.first?.text {
                self.urunViewModel.urunSil(ad: ad)
            }
        })
        alert.addAction(UIAlertAction(title: "İptal", style: .cancel))

        present(alert, animated: true)
    }

    private func showMasaDetail(_ masa: Masa) {
        let vc = MasaDetayViewController(masaId: Int(masa.id) ?? 0)
        navigationController?.pushViewController(vc, animated: true)
    }
}

extension MainViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerController(
      _ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]
    ) {
        picker.dismiss(animated: true)
        if let image = info[.originalImage] as? UIImage,
           let data = image.jpegData(compressionQuality: 0.8)
        {
            imageBase64 = data.base64EncodedString()
        }
    }
}

extension MainViewController: UIPickerViewDataSource, UIPickerViewDelegate {
    // implement numberOfComponents, numberOfRows, titleForRow...
    func numberOfComponents(in pickerView: UIPickerView) -> Int { 1 }
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        urunViewModel.kategoriler.count
    }
    func pickerView(_ pickerView: UIPickerView,
                    titleForRow row: Int,
                    forComponent component: Int) -> String? {
        urunViewModel.kategoriler[row].kategori_ad
    }
}
