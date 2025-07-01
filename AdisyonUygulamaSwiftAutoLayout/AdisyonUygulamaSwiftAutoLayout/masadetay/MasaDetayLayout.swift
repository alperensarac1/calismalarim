//
//  MasaDetayLayout.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 7.06.2025.
//

import Foundation
import UIKit


final class MasaDetayLayout: UIView {
    let headerView: MasaDetayHeaderView
    let adisyonView: MasaAdisyonLayout
    let urunlerView: UrunlerLayout

    init(
        masa: Masa,
        viewModel: MasaDetayViewModel,
        urunVM: UrunViewModel,
        onBackPressed: @escaping () -> Void = {}
    ) {
        headerView = MasaDetayHeaderView(masa: masa, onBackPressed: onBackPressed)

        adisyonView = MasaAdisyonLayout(masa: masa, viewModel: viewModel)

        urunlerView = UrunlerLayout(viewModel: viewModel, urunVM: urunVM)

        super.init(frame: .zero)
        translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = UIColor(red: 1.0, green: 186/255, blue: 186/255, alpha: 1.0) // #FFBABA

        setupLayout()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) not implemented")
    }

    private func setupLayout() {
        addSubview(headerView)
        NSLayoutConstraint.activate([
            headerView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor),
            headerView.leadingAnchor.constraint(equalTo: leadingAnchor),
            headerView.trailingAnchor.constraint(equalTo: trailingAnchor),
            headerView.heightAnchor.constraint(equalToConstant: 60)
        ])

        let bodyContainer = UIStackView(arrangedSubviews: [
            adisyonView,
            urunlerView
        ])
        bodyContainer.axis = .horizontal
        bodyContainer.spacing = 8
        bodyContainer.translatesAutoresizingMaskIntoConstraints = false
        addSubview(bodyContainer)

        NSLayoutConstraint.activate([
            bodyContainer.topAnchor.constraint(equalTo: headerView.bottomAnchor),
            bodyContainer.leadingAnchor.constraint(equalTo: leadingAnchor),
            bodyContainer.trailingAnchor.constraint(equalTo: trailingAnchor),
            bodyContainer.bottomAnchor.constraint(equalTo: bottomAnchor),

            adisyonView.widthAnchor.constraint(equalToConstant: 200),
        ])

        bodyContainer.setCustomSpacing(0, after: adisyonView)
    }

    func updateMasa(_ yeniMasa: Masa) {
          headerView.updateMasa(yeniMasa)
          adisyonView.viewModel.yukleTumVeriler()  // veya uygun güncelleme
        urunlerView.setUrunListesi( adisyonView.viewModel.tumUrunler ) // örnek
      }
}
