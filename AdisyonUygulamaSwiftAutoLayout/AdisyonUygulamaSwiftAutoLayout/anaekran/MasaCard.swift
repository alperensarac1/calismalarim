//
//  MasaCard.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 7.06.2025.
//

import Foundation
import UIKit

class MasaCard: UIView {
    // MARK: - Alt Görünümler
    private let masaAdiLabel = UILabel()
    private let masaFiyatLabel = UILabel()
    private let masaSureLabel = UILabel()
    
    // Tıklama callback’i
    private var onClick: ((Masa) -> Void)?
    private var masa: Masa?
    
    // MARK: - Başlatıcı
    init(onClick: @escaping (Masa) -> Void) {
        super.init(frame: .zero)
        self.onClick = onClick
        setupView()
        setupGesture()
        setupConstraints()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) kullanılmadı")
    }
    
    // MARK: - Görünüm Kurulumu
    private func setupView() {
        backgroundColor = .white
        layer.cornerRadius = 24
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowOpacity = 0.1
        layer.shadowOffset = CGSize(width: 0, height: 4)
        layer.shadowRadius = 8
        
        // Label ortak ayarlar
        [masaAdiLabel, masaFiyatLabel, masaSureLabel].forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
            addSubview($0)
        }
        
        masaAdiLabel.font = UIFont.systemFont(ofSize: 20, weight: .semibold)
        masaAdiLabel.textColor = .black
        
        masaFiyatLabel.font = UIFont.systemFont(ofSize: 18)
        masaFiyatLabel.textColor = .darkGray
        
        masaSureLabel.font = UIFont.systemFont(ofSize: 16)
        masaSureLabel.textColor = .gray
        
        // İç boşluk
        layoutMargins = UIEdgeInsets(top: 30, left: 30, bottom: 30, right: 30)
    }
    
    // MARK: - Dokunma Algılama
    private func setupGesture() {
        let tap = UITapGestureRecognizer(target: self, action: #selector(cardTapped))
        addGestureRecognizer(tap)
    }
    
    @objc private func cardTapped() {
        guard let masa = masa else { return }
        onClick?(masa)
    }
    
    // MARK: - Auto Layout Kısıtlamaları
    private func setupConstraints() {
        // contentView kullanmadan direkt self.layoutMarginsGuide üzerinden hizalama
        let guide = layoutMarginsGuide
        
        NSLayoutConstraint.activate([
            masaAdiLabel.topAnchor.constraint(equalTo: guide.topAnchor),
            masaAdiLabel.leadingAnchor.constraint(equalTo: guide.leadingAnchor),
            masaAdiLabel.trailingAnchor.constraint(equalTo: guide.trailingAnchor),
            
            masaFiyatLabel.topAnchor.constraint(equalTo: masaAdiLabel.bottomAnchor, constant: 8),
            masaFiyatLabel.leadingAnchor.constraint(equalTo: guide.leadingAnchor),
            masaFiyatLabel.trailingAnchor.constraint(equalTo: guide.trailingAnchor),
            
            masaSureLabel.topAnchor.constraint(equalTo: masaFiyatLabel.bottomAnchor, constant: 8),
            masaSureLabel.leadingAnchor.constraint(equalTo: guide.leadingAnchor),
            masaSureLabel.trailingAnchor.constraint(equalTo: guide.trailingAnchor),
            masaSureLabel.bottomAnchor.constraint(equalTo: guide.bottomAnchor)
        ])
    }
    
    // MARK: - Veri Atama
    func setData(_ masa: Masa) {
        self.masa = masa
        masaAdiLabel.text = "Masa \(masa.id)"
        masaFiyatLabel.text = "Tutar: \(masa.toplamFiyat.fiyatYaz())"
        masaSureLabel.text = "Süre: \(masa.sure)"
    }
    
    // MARK: - Arka Plan Rengi
    func setBackgroundColor(_ color: UIColor) {
        backgroundColor = color
    }
}


// MARK: - Hücre

final class MasaCollectionViewCell: UICollectionViewCell {
    static let reuseIdentifier = "MasaCell"

    private lazy var cardView: MasaCard = {
        let v = MasaCard { [weak self] masa in
            guard let callback = self?.onClick else { return }
            callback(masa)
        }
        v.translatesAutoresizingMaskIntoConstraints = false
        return v
    }()

    // Hücreye dışarıdan set edilecek callback ve data
    private var onClick: ((Masa) -> Void)?
    private var masa: Masa?

    override init(frame: CGRect) {
        super.init(frame: frame)
        contentView.addSubview(cardView)
        NSLayoutConstraint.activate([
            cardView.topAnchor.constraint(equalTo: contentView.topAnchor),
            cardView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            cardView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
            cardView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor)
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) kullanılmadı")
    }

    func configure(with masa: Masa, onClick: @escaping (Masa) -> Void) {
        self.masa = masa
        self.onClick = onClick
        cardView.setData(masa)
        let bgColor: UIColor = (masa.acikMi == 1) ? .cyan : .gray
        cardView.setBackgroundColor(bgColor)
    }
}
