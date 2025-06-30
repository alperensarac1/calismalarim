
import SwiftUI

struct QRKodOkuyucu: View {
    @StateObject private var viewModel = QRKodViewModel()

    var body: some View {
        ZStack {
            KameraGorunumu(viewModel: viewModel)
                .edgesIgnoringSafeArea(.all)

            VStack {
                Spacer()
                if !viewModel.qrKodMetni.isEmpty {
                    Text("QR Kod: \(viewModel.qrKodMetni)")
                        .font(.headline)
                        .foregroundColor(.white)
                        .padding()
                        .background(Color.black.opacity(0.7))
                        .cornerRadius(10)
                }
            }
            .padding()
        }
    }
}


struct KameraGorunumu: UIViewControllerRepresentable {
    @ObservedObject var viewModel: QRKodViewModel

    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = UIViewController()
        let cameraLayer = viewModel.getPreviewLayer(frame: UIScreen.main.bounds)
        viewController.view.layer.addSublayer(cameraLayer)
        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}


struct QRKodOkuyucu_Previews: PreviewProvider {
    static var previews: some View {
        QRKodOkuyucu()
    }
}
