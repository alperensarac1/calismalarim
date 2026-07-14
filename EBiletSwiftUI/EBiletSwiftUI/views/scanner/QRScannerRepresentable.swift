//
//  QRScannerRepresentable.swift
//  EBiletSwiftUI
//
//  Created by Alperen Saraç on 2.07.2026.
//

import Foundation
import SwiftUI
import AVFoundation

/*
    QRScannerRepresentable

    SwiftUI içinde direkt AVCaptureSession kullanmak zordur.
    Çünkü AVCaptureVideoPreviewLayer UIKit/CoreAnimation tarafına aittir.

    Bu yüzden UIViewControllerRepresentable kullanıyoruz.

    Mantık:

    SwiftUI View
    ↓
    QRScannerRepresentable
    ↓
    UIKit tabanlı ScannerViewController
    ↓
    AVCaptureSession
    ↓
    QR okununca SwiftUI tarafına callback döner

    onCodeScanned:
    QR kod okunduğunda TicketScannerView'e sonucu gönderir.
*/
struct QRScannerRepresentable: UIViewControllerRepresentable {

    let onCodeScanned: (String) -> Void

    func makeUIViewController(context: Context) -> ScannerViewController {
        let controller = ScannerViewController()
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(
        _ uiViewController: ScannerViewController,
        context: Context
    ) {
        /*
            Bu projede her state değişiminde kamera ayarı değiştirmiyoruz.
            Bu yüzden update içinde özel işlem yok.
        */
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onCodeScanned: onCodeScanned)
    }

    /*
        Coordinator

        UIKit controller'dan gelen QR sonucunu SwiftUI closure'a aktarır.
    */
    final class Coordinator: NSObject, ScannerViewControllerDelegate {

        private let onCodeScanned: (String) -> Void

        init(onCodeScanned: @escaping (String) -> Void) {
            self.onCodeScanned = onCodeScanned
        }

        func scannerViewController(
            _ controller: ScannerViewController,
            didScanCode code: String
        ) {
            onCodeScanned(code)
        }
    }
}

/*
    ScannerViewControllerDelegate

    QR kod okunduğunda ScannerViewController bu delegate'i çağırır.
*/
protocol ScannerViewControllerDelegate: AnyObject {

    func scannerViewController(
        _ controller: ScannerViewController,
        didScanCode code: String
    )
}

/*
    ScannerViewController

    UIKit tarafında çalışan kamera controller'ıdır.

    Görevleri:
    - Kamera izni kontrolü
    - AVCaptureSession başlatma
    - QR metadata okuma
    - Okunan QR kodu delegate ile SwiftUI'a gönderme
*/
final class ScannerViewController: UIViewController {

    weak var delegate: ScannerViewControllerDelegate?

    private var captureSession: AVCaptureSession?
    private var previewLayer: AVCaptureVideoPreviewLayer?

    /*
        Aynı QR kodun art arda defalarca okunmasını engeller.
    */
    private var isCodeAlreadyScanned = false

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .black
        prepareCamera()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()

        previewLayer?.frame = view.bounds
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        stopCamera()
    }

    private func prepareCamera() {
        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else {
            showErrorLabel("Kamera bulunamadı.")
            return
        }

        do {
            let videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)

            let captureSession = AVCaptureSession()

            if captureSession.canAddInput(videoInput) {
                captureSession.addInput(videoInput)
            } else {
                showErrorLabel("Kamera input eklenemedi.")
                return
            }

            let metadataOutput = AVCaptureMetadataOutput()

            if captureSession.canAddOutput(metadataOutput) {
                captureSession.addOutput(metadataOutput)

                metadataOutput.setMetadataObjectsDelegate(
                    self,
                    queue: DispatchQueue.main
                )

                /*
                    Sadece QR kod okutuyoruz.
                */
                metadataOutput.metadataObjectTypes = [.qr]
            } else {
                showErrorLabel("QR okuyucu başlatılamadı.")
                return
            }

            let previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
            previewLayer.videoGravity = .resizeAspectFill
            previewLayer.frame = view.bounds

            view.layer.insertSublayer(previewLayer, at: 0)

            self.captureSession = captureSession
            self.previewLayer = previewLayer

            startCamera()

        } catch {
            showErrorLabel("Kamera hatası: \(error.localizedDescription)")
        }
    }

    private func startCamera() {
        guard let captureSession else {
            return
        }

        guard !captureSession.isRunning else {
            return
        }

        DispatchQueue.global(qos: .userInitiated).async {
            captureSession.startRunning()
        }
    }

    private func stopCamera() {
        guard let captureSession else {
            return
        }

        guard captureSession.isRunning else {
            return
        }

        DispatchQueue.global(qos: .userInitiated).async {
            captureSession.stopRunning()
        }
    }

    /*
        Kamera hatasını ekranda göstermek için basit UILabel.
    */
    private func showErrorLabel(_ text: String) {
        let label = UILabel()
        label.text = text
        label.textColor = .white
        label.textAlignment = .center
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(label)

        NSLayoutConstraint.activate([
            label.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            label.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            label.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            label.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24)
        ])
    }
}

// MARK: - AVCaptureMetadataOutputObjectsDelegate

extension ScannerViewController: AVCaptureMetadataOutputObjectsDelegate {

    func metadataOutput(
        _ output: AVCaptureMetadataOutput,
        didOutput metadataObjects: [AVMetadataObject],
        from connection: AVCaptureConnection
    ) {
        guard !isCodeAlreadyScanned else {
            return
        }

        guard let metadataObject = metadataObjects.first else {
            return
        }

        guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else {
            return
        }

        guard let qrString = readableObject.stringValue else {
            return
        }

        let cleanCode = qrString.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !cleanCode.isEmpty else {
            return
        }

        isCodeAlreadyScanned = true

        /*
            QR okunduktan sonra kamera dursun.
            Tekrar okutmak için SwiftUI ekranda tekrar scanner gösterecek.
        */
        stopCamera()

        delegate?.scannerViewController(
            self,
            didScanCode: cleanCode
        )
    }
}
