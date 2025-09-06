//
//  OdaCell.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 30.08.2025.
//

import UIKit

class OdaCell: UITableViewCell {

    @IBOutlet weak var tvTarih: UILabel!
    @IBOutlet weak var tvOdaId: UILabel!
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    override func prepareForReuse() {
           super.prepareForReuse()
           tvTarih.text = nil
           tvOdaId.text = nil
       }

       func configure(with oda: OdaModel) {
           tvOdaId.text = oda.roomCode    
           tvTarih.text = "Created by: \(oda.createdBy)"
       }
}
