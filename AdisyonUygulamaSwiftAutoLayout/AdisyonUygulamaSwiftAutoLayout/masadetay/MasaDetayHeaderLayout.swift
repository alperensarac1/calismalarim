//
//  MasaDetayHeaderLayout.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 7.06.2025.
//

import Foundation
import UIKit

final class MasaDetayHeaderView: UIView {
    private let geriButton: UIButton = {
        let btn = UIButton(type: .system)
        // Replace "ic_remove" with your asset name
        btn.setImage(UIImage(named: "ic_remove"), for: .normal)
        btn.translatesAutoresizingMaskIntoConstraints = false
        return btn
    }()
    
    private let masaAdiLabel: UILabel = {
        let lbl = UILabel()
        lbl.font = .systemFont(ofSize: 20, weight: .medium)
        lbl.translatesAutoresizingMaskIntoConstraints = false
        return lbl
    }()
    private var masa: Masa
    
    private var onBackPressed: () -> Void = {}
    
    init(masa: Masa, onBackPressed: @escaping () -> Void = {}) {
        self.masa = masa
        self.onBackPressed = onBackPressed
        super.init(frame: .zero)
        translatesAutoresizingMaskIntoConstraints = false
        setupView()
        setupConstraints()
        updateMasa(masa)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setupView() {
        backgroundColor = .white
        addSubview(geriButton)
        addSubview(masaAdiLabel)
        geriButton.addTarget(self, action: #selector(geriTapped), for: .touchUpInside)
    }
    
    private func setupConstraints() {
        NSLayoutConstraint.activate([
            heightAnchor.constraint(greaterThanOrEqualToConstant: 100),
            
            geriButton.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 16),
            geriButton.centerYAnchor.constraint(equalTo: centerYAnchor),

            masaAdiLabel.leadingAnchor.constraint(equalTo: geriButton.trailingAnchor, constant: 12),
            masaAdiLabel.centerYAnchor.constraint(equalTo: centerYAnchor),
            masaAdiLabel.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor, constant: -16)
        ])
    }

    @objc private func geriTapped() {
        onBackPressed()
    }
    
    func updateMasa(_ yeniMasa: Masa) {
        self.masa = yeniMasa
        masaAdiLabel.text = "Masa \(yeniMasa.id)"
    }
}
