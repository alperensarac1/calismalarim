//
//  NotDefteriDetaysayfa.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI

struct NotDefteriDetaysayfa: View {
    @ObservedObject var viewModel: NotDefteriViewModel
    @Environment(\.dismiss) var dismiss
    @State var notBaslik:String = ""
    @State var notIcerik:String = ""
    @State var not:Not!
    var body: some View {
        VStack {
            TextField("Başlık", text: $notBaslik)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
            
            TextEditor(text: $notIcerik)
                .frame(height: 200)
                .border(Color.gray, width: 1)
                .padding()

            HStack {
                Button("Kaydet") {
                    not.not_baslik = notBaslik
                    not.not_icerik = notIcerik
                    viewModel.notKaydet(not: not)
                    dismiss()
                }
                .buttonStyle(.borderedProminent)

                if viewModel.notlar.contains(where: { $0.id == not.id }) {
                    Button("Sil") {
                        viewModel.notSil(not: not)
                        dismiss()
                    }
                    .buttonStyle(.bordered)
                    .foregroundColor(.red)
                }
            }
            .padding()
        }
    }
}


