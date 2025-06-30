//
//  Takvim.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI

struct Takvim: View {
    @State private var selectedDate = Date()
    private let formatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        return formatter
    }()
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Bugünün Tarihi: \(formatter.string(from: Date()))")
                .font(.headline)
                .padding()
                .background(Color.blue.opacity(0.2))
                .cornerRadius(10)

            DatePicker("Tarih Seç", selection: $selectedDate, displayedComponents: .date)
                .datePickerStyle(.graphical)
                .padding()

            Text("Seçilen Tarih: \(formatter.string(from: selectedDate))")
                .font(.headline)
                .padding()
                .background(Color.green.opacity(0.2))
                .cornerRadius(10)
        }
        .padding()
    }
}



struct Takvim_Previews: PreviewProvider {
    static var previews: some View {
        Takvim()
    }
}
