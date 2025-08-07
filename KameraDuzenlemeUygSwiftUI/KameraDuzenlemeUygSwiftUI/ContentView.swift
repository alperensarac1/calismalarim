import SwiftUI
import AVFoundation
import Photos

struct CameraAllInOneView: View {
    @StateObject private var cameraModel = CameraModel()
    @State private var overlayText = ""
    @State private var showTextField = false

    var body: some View {
        ZStack {
            if let image = cameraModel.capturedImage {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFit()
                    .edgesIgnoringSafeArea(.all)
            } else {
                CameraPreview(session: cameraModel.session)
                    .edgesIgnoringSafeArea(.all)
            }

            VStack {
                Spacer()

                if cameraModel.capturedImage == nil {
                    Button("📸 Fotoğraf Çek") {
                        cameraModel.takePhoto()
                    }
                    .padding()
                    .background(.white.opacity(0.7))
                    .cornerRadius(10)
                } else {
                    if showTextField {
                        TextField("Yazınızı girin", text: $overlayText)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .padding()
                    }

                    HStack {
                        Button("📝 Yazı Ekle") {
                            showTextField = true
                        }

                        Button("💾 Kaydet") {
                            cameraModel.addTextToImage(text: overlayText)
                            showTextField = false
                        }

                        Button("↩️ Yeniden Çek") {
                            overlayText = ""
                            showTextField = false
                            cameraModel.reset()
                        }
                    }
                    .padding()
                    .background(.white.opacity(0.7))
                    .cornerRadius(10)
                }
            }
        }
        .onAppear {
            cameraModel.checkPermissions()
            cameraModel.configure()
        }
    }
}

// MARK: - Camera Model

class CameraModel: NSObject, ObservableObject, AVCapturePhotoCaptureDelegate {
    @Published var capturedImage: UIImage?
    let session = AVCaptureSession()
    private var output = AVCapturePhotoOutput()

    func checkPermissions() {
        AVCaptureDevice.requestAccess(for: .video) { _ in }
        PHPhotoLibrary.requestAuthorization { _ in }
    }

    func configure() {
        session.beginConfiguration()
        guard let device = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: device),
              session.canAddInput(input),
              session.canAddOutput(output) else {
            print("Kamera eklenemedi.")
            return
        }

        session.addInput(input)
        session.addOutput(output)
        session.commitConfiguration()
        session.startRunning()
    }

    func takePhoto() {
        let settings = AVCapturePhotoSettings()
        output.capturePhoto(with: settings, delegate: self)
    }

    func photoOutput(_ output: AVCapturePhotoOutput,
                     didFinishProcessingPhoto photo: AVCapturePhoto,
                     error: Error?) {
        guard let data = photo.fileDataRepresentation(),
              let image = UIImage(data: data) else { return }

        DispatchQueue.main.async {
            self.capturedImage = image
        }
    }

    func addTextToImage(text: String) {
        guard let image = capturedImage else { return }

        let renderer = UIGraphicsImageRenderer(size: image.size)
        let newImage = renderer.image { context in
            image.draw(at: .zero)

            let attributes: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: 64),
                .foregroundColor: UIColor.red
            ]

            let textSize = text.size(withAttributes: attributes)
            let point = CGPoint(x: (image.size.width - textSize.width) / 2, y: 100)
            text.draw(at: point, withAttributes: attributes)
        }

        // Galeriye kaydet
        UIImageWriteToSavedPhotosAlbum(newImage, nil, nil, nil)

        DispatchQueue.main.async {
            self.capturedImage = newImage
        }
    }

    func reset() {
        capturedImage = nil
    }
}

// MARK: - Camera Preview for SwiftUI

struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> UIView {
        let view = UIView(frame: UIScreen.main.bounds)
        let previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = .resizeAspectFill
        previewLayer.frame = view.bounds
        view.layer.addSublayer(previewLayer)
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {}
}

// MARK: - Preview

struct CameraAllInOneView_Previews: PreviewProvider {
    static var previews: some View {
        CameraAllInOneView()
    }
}
