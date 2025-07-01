//
//  MasaDetayViewController.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 7.06.2025.
//

import Foundation
import UIKit
import Combine

final class MasaDetayViewController: UIViewController {
    
    
    let masaId: Int
    let viewModel: MasaDetayViewModel
    let urunViewModel = UrunViewModel()
    
    var layoutView: MasaDetayLayout!
    var subscriptions = Set<AnyCancellable>()
    
    // MARK: – Init
    
    init(masaId: Int) {
        self.masaId = masaId
        self.viewModel = MasaDetayViewModel(masaId: masaId)
        super.init(nibName: nil, bundle: nil)
    }
    required init?(coder: NSCoder) { fatalError("init(coder:) not used") }
    
    // MARK: – Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        
       
        let başlangıçMasa = Masa(
            id: masaId,            // Int
            toplamFiyat: 0.0,      // Float
            acikMi: 1,             // Int
            sure: "00:00"          // String
        )

        layoutView = MasaDetayLayout(
            masa: başlangıçMasa,
            viewModel: viewModel,
            urunVM: urunViewModel
        )

         { [weak self] in
            self?.navigationController?.popViewController(animated: true)
        }
        view.addSubview(layoutView)
        NSLayoutConstraint.activate([
            layoutView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            layoutView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            layoutView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            layoutView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        bindViewModels()
        Task{
            await urunViewModel.kategorileriYukle()
            await urunViewModel.urunleriYukle()
        }
  
        viewModel.yukleTumVeriler()
    }

    
    private func bindViewModels() {
        urunViewModel.$urunler
            .receive(on: DispatchQueue.main)
            .sink { [weak self] liste in
                self?.layoutView.urunlerView.setUrunListesi(liste)
            }
            .store(in: &subscriptions)
        
        urunViewModel.$kategoriler
            .receive(on: DispatchQueue.main)
            .sink { [weak self] kategoriler in
                self?.layoutView.urunlerView.setKategoriListesi(kategoriler)
            }
            .store(in: &subscriptions)
        
        viewModel.$toplamFiyat
            .receive(on: DispatchQueue.main)
            .sink { [weak self] fiyat in
                self?.layoutView.adisyonView.toplamFiyatLabel.text = String(format: "%.2f TL", fiyat)
            }
            .store(in: &subscriptions)

        viewModel.$odemeTamamlandi
            .receive(on: DispatchQueue.main)
            .sink { [weak self] tamamlandi in
                guard tamamlandi else { return }
                self?.handleOdemeTamamlandi()
            }
            .store(in: &subscriptions)
    }
    
    private func handleOdemeTamamlandi() {
        let alert = UIAlertController(title: nil, message: "Ödeme tamamlandı", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Tamam", style: .default) { _ in
            // güncellenen masa bilgisini ana listenin VM’ine ilet
            if let guncel = self.viewModel.masa {
                let parentVM = MasalarViewModel()
                parentVM.guncelleMasa(guncel)
            }
            self.navigationController?.popViewController(animated: true)
        })
        present(alert, animated: true)
    }
}
