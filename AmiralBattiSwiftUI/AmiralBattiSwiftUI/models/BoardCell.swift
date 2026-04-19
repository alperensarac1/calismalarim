//
//  BoardCell.swift
//  AmiralBattiSwiftUI
//
//  Created by Alperen Saraç on 12.04.2026.
//

import Foundation
struct BoardCell: Identifiable, Equatable {
    let id = UUID()
    let row: Int
    let col: Int
    var state: CellState
}
