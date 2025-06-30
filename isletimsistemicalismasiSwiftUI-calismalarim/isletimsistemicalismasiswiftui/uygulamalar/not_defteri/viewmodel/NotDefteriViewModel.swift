//
//  NotDefteriViewModel.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI

class NotDefteriViewModel: ObservableObject {
    @Published var notlar: [Not] = []

    func notKaydet(not: Not) {
        if let index = notlar.firstIndex(where: { $0.id == not.id }) {
            notlar[index] = not
        } else {
            notlar.append(not)
        }
    }

    func notSil(not: Not) {
        notlar.removeAll { $0.id == not.id }
    }
}
