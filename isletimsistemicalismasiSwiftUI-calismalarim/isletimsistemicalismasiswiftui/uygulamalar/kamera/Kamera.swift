//
//  Kamera.swift
//  isletimsistemicalismasiswiftui
//
//  Created by Alperen Saraç on 23.02.2025.
//

import SwiftUI
import AVFoundation
import UIKit

struct Kamera: View {
    @State private var kameraIzinVerildi = false
    @State private var kamerayiAc = false
    @State private var secilenGorsel: UIImage?

    var body: some View {
        VStack {
            if let image = secilenGorsel {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFit()
                    .frame(height: 300)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding()
            } else {
                Text("Henüz Fotoğraf Seçilmedi")
                    .foregroundColor(.gray)
                    .padding()
            }

            if kameraIzinVerildi {
                Button(action: {
                    kamerayiAc.toggle()
                }) {
                    Label("Kamerayı Aç", systemImage: "camera")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                .fullScreenCover(isPresented: $kamerayiAc) {
                    KameraPickerView(secilenGorsel: $secilenGorsel)
                }
            } else {
                Button("Kamera İzni Al") {
                    kameraIzniniKontrolEt()
                }
                .padding()
                .background(Color.red)
                .foregroundColor(.white)
                .cornerRadius(8)
            }
        }
        .onAppear {
            kameraIzniniKontrolEt()
        }
    }
    
    func kameraIzniniKontrolEt() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        
        switch status {
        case .authorized:
            kameraIzinVerildi = true
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                DispatchQueue.main.async {
                    kameraIzinVerildi = granted
                }
            }
        case .denied, .restricted:
            kameraIzinVerildi = false
        @unknown default:
            kameraIzinVerildi = false
        }
    }
}

struct KameraPickerView: UIViewControllerRepresentable {
    @Environment(\.presentationMode) var presentationMode
    @Binding var secilenGorsel: UIImage?

    class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        var parent: KameraPickerView

        init(_ parent: KameraPickerView) {
            self.parent = parent
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.presentationMode.wrappedValue.dismiss()
        }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let image = info[.originalImage] as? UIImage {
                parent.secilenGorsel = image
            }
            parent.presentationMode.wrappedValue.dismiss()
        }
    }

    func makeCoordinator() -> Coordinator {
        return Coordinator(self)
    }

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) { }
}


struct Kamera_Previews: PreviewProvider {
    static var previews: some View {
        Kamera()
    }
}
