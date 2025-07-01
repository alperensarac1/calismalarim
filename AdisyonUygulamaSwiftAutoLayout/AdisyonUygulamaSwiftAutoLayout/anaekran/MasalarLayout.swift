//
//  MasalarLayout.swift
//  AdisyonUygulamaSwiftAutoLayout
//
//  Created by Alperen Saraç on 7.06.2025.
//

import Foundation
import UIKit
final class MasalarLayout: UIView {

    var masaList: [Masa]
    var onMasaClick: (Masa) -> Void

    private(set) lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        let spacing: CGFloat = 10
        let totalSpacing = spacing * 2 + spacing * 2 / 3
        let itemWidth = (UIScreen.main.bounds.width - totalSpacing) / 3
        layout.itemSize = CGSize(width: itemWidth, height: itemWidth * 1.2)
        layout.minimumLineSpacing = spacing
        layout.minimumInteritemSpacing = spacing
        layout.sectionInset = UIEdgeInsets(top: spacing, left: spacing, bottom: spacing, right: spacing)

        let cv = UICollectionView(frame: .zero, collectionViewLayout: layout)
        cv.register(MasaCollectionViewCell.self,
                    forCellWithReuseIdentifier: MasaCollectionViewCell.reuseIdentifier)
        cv.backgroundColor = .white
        cv.translatesAutoresizingMaskIntoConstraints = false
        cv.dataSource = self
        return cv
    }()

    init(masaList: [Masa], onMasaClick: @escaping (Masa) -> Void) {
        self.masaList = masaList
        self.onMasaClick = onMasaClick
        super.init(frame: .zero)
        translatesAutoresizingMaskIntoConstraints = false
        addSubview(collectionView)
        NSLayoutConstraint.activate([
            collectionView.topAnchor.constraint(equalTo: topAnchor),
            collectionView.leadingAnchor.constraint(equalTo: leadingAnchor),
            collectionView.trailingAnchor.constraint(equalTo: trailingAnchor),
            collectionView.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) kullanılmadı")
    }
    func update(masaList yeniListe: [Masa]) {
            self.masaList = yeniListe
            collectionView.reloadData()
        }
}


extension MasalarLayout: UICollectionViewDataSource {
    func collectionView(_ cv: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return masaList.count
    }

    func collectionView(_ cv: UICollectionView,
                        cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let masa = masaList[indexPath.item]
        let cell = cv.dequeueReusableCell(withReuseIdentifier: MasaCollectionViewCell.reuseIdentifier,
                                          for: indexPath) as! MasaCollectionViewCell
        cell.configure(with: masa, onClick: onMasaClick)
        return cell
    }
}
