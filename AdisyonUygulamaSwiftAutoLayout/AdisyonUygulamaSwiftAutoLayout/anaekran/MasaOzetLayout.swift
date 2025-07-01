//
//  MasaOzetLayout.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 7.06.2025.
//

import Foundation
import UIKit

// Helper to find the owning view controller
private extension UIView {
    func parentViewController() -> UIViewController? {
        var responder: UIResponder? = self
        while let r = responder {
            if let vc = r as? UIViewController { return vc }
            responder = r.next
        }
        return nil
    }
}

class MasaOzetLayout: UIView, UITableViewDataSource, UITableViewDelegate {

    // MARK: - Properties

    private var masaListesi: [Masa]
    private var viewModel: MasaDetayViewModel
    private var onMasaDetayTikla: (Masa) -> Void

    private lazy var descriptionLabel: UILabel = {
        let label = UILabel()
        let acikMasaSayisi = masaListesi.filter { $0.acikMi == 1 }.count
        label.text = "\(acikMasaSayisi) adet masa açık."
        label.font = .systemFont(ofSize: 20)
        label.textColor = .red
        label.textAlignment = .center
        return label
    }()

    private lazy var tableView: UITableView = {
        let tv = UITableView()
        tv.register(UITableViewCell.self, forCellReuseIdentifier: "Cell")
        tv.dataSource = self
        tv.delegate = self
        return tv
    }()

    private var acikMasalar: [Masa] {
        masaListesi.filter { $0.acikMi == 1 }
    }

    // MARK: - Init

    init(
        masaListesi: [Masa],
        viewModel: MasaDetayViewModel,
        onMasaDetayTikla: @escaping (Masa) -> Void
    ) {
        self.masaListesi = masaListesi
        self.viewModel = viewModel
        self.onMasaDetayTikla = onMasaDetayTikla
        super.init(frame: .zero)
        setupViews()
        setupConstraints()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // MARK: - Setup

    private func setupViews() {
        translatesAutoresizingMaskIntoConstraints = false

        // Container stack for vertical layout
        let stack = UIStackView(arrangedSubviews: [descriptionLabel, tableView])
        stack.axis = .vertical
        stack.spacing = 20
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.isLayoutMarginsRelativeArrangement = true
        stack.layoutMargins = UIEdgeInsets(top: 50, left: 50, bottom: 50, right: 50)

        addSubview(stack)
    }

    private func setupConstraints() {
        guard let stack = subviews.first as? UIStackView else { return }
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: topAnchor),
            stack.leadingAnchor.constraint(equalTo: leadingAnchor),
            stack.trailingAnchor.constraint(equalTo: trailingAnchor),
            stack.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
    }

    // MARK: - UITableViewDataSource

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return acikMasalar.count
    }

    func tableView(_ tv: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let masa = acikMasalar[indexPath.row]
        let cell = tv.dequeueReusableCell(withIdentifier: "Cell", for: indexPath)
        let price = String(format: "%.2f ₺", masa.toplamFiyat)
        cell.textLabel?.text = "Masa \(masa.id)    \(price)"
        return cell
    }

    // MARK: - UITableViewDelegate

    func tableView(_ tv: UITableView, didSelectRowAt indexPath: IndexPath) {
        tv.deselectRow(at: indexPath, animated: true)
        let selected = acikMasalar[indexPath.row]
        showMasaOptions(for: selected)
    }

    // MARK: - Alert

    private func showMasaOptions(for masa: Masa) {
        guard let vc = parentViewController() else { return }
        let ac = UIAlertController(title: "Masa \(masa.id)", message: nil, preferredStyle: .actionSheet)

        ac.addAction(.init(title: "Ürün Ekle", style: .default) { _ in
            self.onMasaDetayTikla(masa)
        })

        ac.addAction(.init(title: "Ödeme Al", style: .default) { _ in
            Task {
                await self.viewModel.odemeAl {
                    // Show simple confirmation
                    let ok = UIAlertController(title: nil, message: "Ödeme alındı", preferredStyle: .alert)
                    ok.addAction(.init(title: "Tamam", style: .default))
                    vc.present(ok, animated: true)
                }
            }
        })

        ac.addAction(.init(title: "İptal", style: .cancel))
        vc.present(ac, animated: true)
    }
    func update(masaListesi yeniListe: [Masa]) {
           self.masaListesi = yeniListe
           
        self.descriptionLabel.text = "\(yeniListe.filter { $0.acikMi == 1 }.count) adet masa açık."
           tableView.reloadData()
       }
}
