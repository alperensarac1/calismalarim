import SwiftUI
import Combine
import UIKit

struct MainScreen: View {
    @StateObject var masalarVM: MasalarViewModel
    @StateObject var urunVM: UrunViewModel
    @StateObject var detayVM: MasaDetayViewModel
    @State private var navigationPath: [Masa] = []
    @State private var selectedMasaForDetail: Masa?
    @State private var showingImagePicker = false
    @State private var imageData: Data?

    var body: some View {
        NavigationStack(path: $navigationPath) {
            VStack(spacing: 0) {
                MainLayoutView(
                    masaListesi: $masalarVM.masalar,
                    onMasaClick: { masa in
                        navigationPath.append(masa) // ✅ geçiş burada gerçekleşiyor
                    }
                )
                .frame(maxHeight: .infinity)
                
                HStack(spacing: 10) {
                    Button("Masa İşlemleri")    { showMasaAlert() }
                    Button("Ürün İşlemleri")    { showUrunAlert() }
                    Button("Masa Birleştir")    { showBirlestirAlert() }
                    Button("Kategori İşlemleri"){ showKategoriAlert() }
                }
                .padding()
            }
            .background(Color(red: 1, green: 186/255, blue: 186/255))
            .onAppear {
                Task {
                    await masalarVM.masalariYukle()
                    await urunVM.kategorileriYukle()
                }
            }
            // ✅ PATH tabanlı geçiş hedefi burada
            .navigationDestination(for: Masa.self) { masa in
                MasaDetayScreen(masaId: masa.id)
            }
        }
        .sheet(isPresented: $showingImagePicker) {
            ImagePicker(imageData: $imageData)
        }
    }
  // MARK: — Alert & Dialog Fonksiyonları

  private func showMasaAlert() {
    let alert = UIAlertController(
      title: "Masa İşlemleri",
      message: "Yeni masa ekleyebilir veya ID girerek silebilirsiniz.",
      preferredStyle: .alert
    )
    alert.addTextField {
      $0.placeholder = "Silinecek masa ID"
      $0.keyboardType = .numberPad
    }
    alert.addAction(.init(title: "Masa Ekle", style: .default) { _ in
      masalarVM.masaEkle {
        Task { await masalarVM.masalariYukle() }
      }
    })
    alert.addAction(.init(title: "Masa Sil", style: .destructive) { _ in
      if let txt = alert.textFields?.first?.text,
         let id = Int(txt) {
        masalarVM.masaSil(masaId: id)
      }
    })
    UIApplication.shared.topMostController?.present(alert, animated: true)
  }

  private func showUrunAlert() {
    let alert = UIAlertController(
      title: "Ürün İşlemleri",
      message: nil,
      preferredStyle: .alert
    )
    alert.addTextField { $0.placeholder = "Ürün Adı" }
    alert.addTextField {
      $0.placeholder = "Fiyat"
      $0.keyboardType = .decimalPad
    }
    alert.addTextField {
      $0.placeholder = "Kategori ID"
      $0.keyboardType = .numberPad
    }
    alert.addAction(.init(title: "Resim Seç", style: .default) { _ in
      showingImagePicker = true
    })
    alert.addAction(.init(title: "Ekle", style: .default) { _ in
      guard
        let ad = alert.textFields?[0].text, !ad.isEmpty,
        let fiyatText = alert.textFields?[1].text, let fiyat = Float(fiyatText),
        let katText = alert.textFields?[2].text, let katId = Int(katText),
        let data = imageData
      else { return }

      let base64 = data.base64EncodedString()
      urunVM.urunEkle(ad: ad, fiyat: fiyat, resimBase64: base64, kategoriId: katId)
    })
    alert.addAction(.init(title: "Sil", style: .destructive) { _ in
      if let ad = alert.textFields?.first?.text {
        urunVM.urunSil(ad: ad)
      }
    })
    UIApplication.shared.topMostController?.present(alert, animated: true)
  }

  private func showBirlestirAlert() {
    let alert = UIAlertController(
      title: "Masa Birleştir",
      message: nil,
      preferredStyle: .alert
    )
    alert.addTextField {
      $0.placeholder = "Ana Masa ID"
      $0.keyboardType = .numberPad
    }
    alert.addTextField {
      $0.placeholder = "Birlestirilecek Masa ID"
      $0.keyboardType = .numberPad
    }
    alert.addAction(.init(title: "Birleştir", style: .default) { _ in
      if
        let aText = alert.textFields?[0].text, let ana = Int(aText),
        let bText = alert.textFields?[1].text, let birles = Int(bText)
      {
        masalarVM.masaBirlestir(anaId: ana, bId: birles)
        Task { await masalarVM.masalariYukle() }
      }
    })
    UIApplication.shared.topMostController?.present(alert, animated: true)
  }

  private func showKategoriAlert() {
    let alert = UIAlertController(
      title: "Kategori İşlemleri",
      message: nil,
      preferredStyle: .alert
    )
    alert.addTextField { $0.placeholder = "Yeni Kategori Adı" }
    alert.addTextField {
      $0.placeholder = "Silinecek Kategori ID"
      $0.keyboardType = .numberPad
    }
    alert.addAction(.init(title: "Kaydet", style: .default) { _ in
      if let ad = alert.textFields?[0].text, !ad.isEmpty {
        Task {
          await urunVM.kategoriEkle(ad: ad)
          await urunVM.kategorileriYukle()
        }
      }
    })
    alert.addAction(.init(title: "Sil", style: .destructive) { _ in
      if let idText = alert.textFields?[1].text, let id = Int(idText) {
        urunVM.kategoriSil(id: id)
      }
    })
    UIApplication.shared.topMostController?.present(alert, animated: true)
  }
}

private extension UIApplication {
  var topMostController: UIViewController? {
    keyWindow?.rootViewController?.topMost()
  }
}

private extension UIViewController {
  func topMost() -> UIViewController {
    presentedViewController?.topMost() ?? self
  }
}

struct ImagePicker: UIViewControllerRepresentable {
  @Binding var imageData: Data?
  @Environment(\.presentationMode) private var presentationMode

  func makeUIViewController(context: Context) -> UIImagePickerController {
    let picker = UIImagePickerController()
    picker.delegate = context.coordinator
    picker.mediaTypes = ["public.image"]
    return picker
  }

  func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

  func makeCoordinator() -> Coordinator { Coordinator(self) }
  class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
    let parent: ImagePicker
    init(_ parent: ImagePicker) { self.parent = parent }
    func imagePickerController(
      _ picker: UIImagePickerController,
      didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]
    ) {
      if let img = info[.originalImage] as? UIImage,
         let data = img.jpegData(compressionQuality: 0.8) {
        parent.imageData = data
      }
      parent.presentationMode.wrappedValue.dismiss()
    }
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
      parent.presentationMode.wrappedValue.dismiss()
    }
  }
}

