//
//  CollectionViewCell.swift
//  KahveQROlusturucu
//
//  Created by Alperen Saraç on 29.03.2025.
//

import UIKit

class UrunCollectionViewCell: UICollectionViewCell {
    @IBOutlet weak var imgUrun: UIImageView!
    
    @IBOutlet weak var tvIndirimYuzde: UILabel!
    @IBOutlet weak var viewIndirimVarMi: UIView!
    @IBOutlet weak var tvUrunAd: UILabel!
    
    @IBOutlet weak var tvIndirimsizFiyat: UILabel!
    
    @IBOutlet weak var tvFiyat: UILabel!
}
