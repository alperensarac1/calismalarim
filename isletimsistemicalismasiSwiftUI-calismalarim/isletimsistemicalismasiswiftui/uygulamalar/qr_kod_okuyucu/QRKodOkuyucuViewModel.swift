
import Foundation
import SwiftUI
import AVFoundation

class QRKodViewModel: NSObject, ObservableObject, AVCaptureMetadataOutputObjectsDelegate {
    @Published var qrKodMetni: String = ""
    private var captureSession: AVCaptureSession?
    private var previewLayer: AVCaptureVideoPreviewLayer?

    override init() {
        super.init()
        checkCameraPermission()
    }

    func setupCaptureSession() {
        let session = AVCaptureSession()

        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video),
              let videoInput = try? AVCaptureDeviceInput(device: videoCaptureDevice) else {
            return
        }

        if session.canAddInput(videoInput) {
            session.addInput(videoInput)
        }

        let metadataOutput = AVCaptureMetadataOutput()
        if session.canAddOutput(metadataOutput) {
            session.addOutput(metadataOutput)
            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            metadataOutput.metadataObjectTypes = [.qr]
        }

        captureSession = session
        DispatchQueue.global(qos: .userInitiated).async {
            session.startRunning()
        }
    }

    func metadataOutput(_ output: AVCaptureMetadataOutput,
                        didOutput metadataObjects: [AVMetadataObject],
                        from connection: AVCaptureConnection) {
        if let metadataObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
           let kod = metadataObject.stringValue {
            DispatchQueue.main.async {
                self.qrKodMetni = kod
                AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
            }
        }
    }

    func getPreviewLayer(frame: CGRect) -> CALayer {
        let previewLayer = AVCaptureVideoPreviewLayer(session: captureSession!)
        previewLayer.frame = frame
        previewLayer.videoGravity = .resizeAspectFill
        self.previewLayer = previewLayer
        return previewLayer
    }

    private func checkCameraPermission() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        switch status {
        case .authorized:
            setupCaptureSession()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                if granted {
                    DispatchQueue.main.async {
                        self.setupCaptureSession()
                    }
                }
            }
        case .denied, .restricted:
            print("⚠️ Kamera izni verilmedi veya kısıtlandı.")
        @unknown default:
            break
        }
    }
}
