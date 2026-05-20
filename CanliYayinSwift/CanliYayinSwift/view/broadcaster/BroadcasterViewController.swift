//
//  File.swift
//  CanliYayinSwift
//
//  Created by Alperen Saraç on 13.05.2026.
//

import Foundation
import UIKit
import AVFoundation

final class BroadcasterViewController: UIViewController {

    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var titleTextField: UITextField!
    @IBOutlet weak var startButton: UIButton!
    @IBOutlet weak var viewerCountLabel: UILabel!
    @IBOutlet weak var previewContainerView: UIView!
    @IBOutlet weak var chatTitleLabel: UILabel!
    @IBOutlet weak var chatTableView: UITableView!
    @IBOutlet weak var messageTextField: UITextField!
    @IBOutlet weak var sendButton: UIButton!
    @IBOutlet weak var stopButton: UIButton!

    private var socketManager: LiveSocketManager?

    private var chatMessages: [ChatMessageModel] = []

    private let captureSession = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer?

    private let videoOutput = AVCaptureVideoDataOutput()
    private let videoQueue = DispatchQueue(label: "video.frame.queue")

    private var roomId: String?

    private var lastFrameTime: CFTimeInterval = 0
    private let frameInterval: CFTimeInterval = 0.25

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()
        configureChatTableView()
        connectSocket()
        requestCameraPermission()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        previewLayer?.frame = previewContainerView.bounds
    }

    private func configureUI() {
        view.backgroundColor = .systemBackground
        title = "Yayın Aç"

        statusLabel.text = "Hazırlanıyor..."
        statusLabel.font = .systemFont(ofSize: 14)
        statusLabel.textColor = .secondaryLabel

        titleTextField.placeholder = "Yayın başlığı yaz..."
        titleTextField.borderStyle = .roundedRect

        startButton.setTitle("Yayını Başlat", for: .normal)

        viewerCountLabel.text = "İzleyici: 0"
        viewerCountLabel.font = .systemFont(ofSize: 14)

        previewContainerView.backgroundColor = .black
        previewContainerView.clipsToBounds = true

        chatTitleLabel.text = "Canlı Sohbet"
        chatTitleLabel.font = .boldSystemFont(ofSize: 18)

        messageTextField.placeholder = "Mesaj yaz..."
        messageTextField.borderStyle = .roundedRect

        sendButton.setTitle("Gönder", for: .normal)
        stopButton.setTitle("Yayını Bitir", for: .normal)
    }

    private func configureChatTableView() {
        chatTableView.dataSource = self
        chatTableView.delegate = self

        let nib = UINib(
            nibName: ChatTableViewCell.identifier,
            bundle: nil
        )

        chatTableView.register(
            nib,
            forCellReuseIdentifier: ChatTableViewCell.identifier
        )

        chatTableView.rowHeight = UITableView.automaticDimension
        chatTableView.estimatedRowHeight = 60
    }

    private func connectSocket() {
        let manager = LiveSocketManager(urlString: AppConfig.serverURL)
        manager.delegate = self
        manager.connect()

        socketManager = manager
    }

    private func requestCameraPermission() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            setupCamera()

        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    if granted {
                        self?.setupCamera()
                    } else {
                        self?.statusLabel.text = "Kamera izni verilmedi"
                    }
                }
            }

        default:
            statusLabel.text = "Kamera izni gerekli"
        }
    }

    private func setupCamera() {
        captureSession.beginConfiguration()
        captureSession.sessionPreset = .low

        guard let camera = AVCaptureDevice.default(
            .builtInWideAngleCamera,
            for: .video,
            position: .front
        ) else {
            statusLabel.text = "Ön kamera bulunamadı"
            captureSession.commitConfiguration()
            return
        }

        do {
            let input = try AVCaptureDeviceInput(device: camera)

            if captureSession.canAddInput(input) {
                captureSession.addInput(input)
            }

            videoOutput.alwaysDiscardsLateVideoFrames = true
            videoOutput.setSampleBufferDelegate(self, queue: videoQueue)

            if captureSession.canAddOutput(videoOutput) {
                captureSession.addOutput(videoOutput)
            }

            captureSession.commitConfiguration()

            let layer = AVCaptureVideoPreviewLayer(session: captureSession)
            layer.videoGravity = .resizeAspectFill
            layer.frame = previewContainerView.bounds

            previewContainerView.layer.addSublayer(layer)
            previewLayer = layer

            DispatchQueue.global(qos: .userInitiated).async {
                self.captureSession.startRunning()
            }

            statusLabel.text = "Kamera hazır"

        } catch {
            captureSession.commitConfiguration()
            statusLabel.text = "Kamera başlatılamadı"
        }
    }

    @IBAction func startButtonTapped(_ sender: UIButton) {
        let title = titleTextField.text?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        guard !title.isEmpty else {
            statusLabel.text = "Yayın başlığı yazmalısın"
            return
        }

        socketManager?.sendJson([
            "type": "create_room",
            "title": title,
            "broadcaster_name": "iOS Yayıncı"
        ])

        statusLabel.text = "Oda oluşturuluyor..."
    }

    @IBAction func sendButtonTapped(_ sender: UIButton) {
        let message = messageTextField.text?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        guard !message.isEmpty else { return }

        socketManager?.sendJson([
            "type": "chat_message",
            "message": message
        ])

        messageTextField.text = ""
    }

    @IBAction func stopButtonTapped(_ sender: UIButton) {
        navigationController?.popViewController(animated: true)
    }

    private func handleRoomCreated(_ data: [String: Any]) {
        roomId = data["room_id"] as? String

        DispatchQueue.main.async {
            self.statusLabel.text = "Yayın başladı"
            self.titleTextField.isEnabled = false
            self.startButton.isEnabled = false
        }
    }

    private func handleViewerCount(_ data: [String: Any]) {
        let count = data["viewer_count"] as? Int ?? 0

        DispatchQueue.main.async {
            self.viewerCountLabel.text = "İzleyici: \(count)"
        }
    }

    private func handleChatMessage(_ data: [String: Any]) {
        let chat = ChatMessageModel(json: data)

        DispatchQueue.main.async {
            self.chatMessages.append(chat)
            self.chatTableView.reloadData()

            let lastIndex = IndexPath(
                row: self.chatMessages.count - 1,
                section: 0
            )

            self.chatTableView.scrollToRow(
                at: lastIndex,
                at: .bottom,
                animated: true
            )
        }
    }

    private func sendFrame(_ image: UIImage) {
        guard roomId != nil else { return }

        guard let imageData = image.jpegData(compressionQuality: 0.35) else {
            return
        }

        let base64Frame = imageData.base64EncodedString()

        socketManager?.sendJson([
            "type": "video_frame",
            "frame": base64Frame
        ])
    }

    deinit {
        socketManager?.disconnect()

        if captureSession.isRunning {
            captureSession.stopRunning()
        }
    }
}

extension BroadcasterViewController: LiveSocketManagerDelegate {

    func socketDidConnect() {
        DispatchQueue.main.async {
            self.statusLabel.text = "Sunucuya bağlandı. Başlık yazıp yayını başlat."
        }
    }

    func socketDidReceiveMessage(_ message: String) {
        guard let data = message.toJsonDictionary(),
              let type = data["type"] as? String else {
            return
        }

        switch type {
        case "room_created":
            handleRoomCreated(data)

        case "viewer_count":
            handleViewerCount(data)

        case "chat_message":
            handleChatMessage(data)

        case "error":
            DispatchQueue.main.async {
                self.statusLabel.text = data["message"] as? String ?? "Bilinmeyen hata"
            }

        default:
            break
        }
    }

    func socketDidDisconnect() {
        DispatchQueue.main.async {
            self.statusLabel.text = "Bağlantı kapandı"
        }
    }

    func socketDidReceiveError(_ error: String) {
        DispatchQueue.main.async {
            self.statusLabel.text = "Hata: \(error)"
        }
    }
}

extension BroadcasterViewController: UITableViewDataSource, UITableViewDelegate {

    func tableView(
        _ tableView: UITableView,
        numberOfRowsInSection section: Int
    ) -> Int {
        return chatMessages.count
    }

    func tableView(
        _ tableView: UITableView,
        cellForRowAt indexPath: IndexPath
    ) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(
            withIdentifier: ChatTableViewCell.identifier,
            for: indexPath
        ) as? ChatTableViewCell else {
            return UITableViewCell()
        }

        cell.configure(with: chatMessages[indexPath.row])
        return cell
    }
}

extension BroadcasterViewController: AVCaptureVideoDataOutputSampleBufferDelegate {

    func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        let currentTime = CACurrentMediaTime()

        guard currentTime - lastFrameTime >= frameInterval else {
            return
        }

        lastFrameTime = currentTime

        guard roomId != nil else {
            return
        }

        guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else {
            return
        }

        let ciImage = CIImage(cvImageBuffer: imageBuffer)

        let context = CIContext()

        guard let cgImage = context.createCGImage(
            ciImage,
            from: ciImage.extent
        ) else {
            return
        }

        let image = UIImage(
            cgImage: cgImage,
            scale: 1,
            orientation: .right
        )

        sendFrame(image)
    }
}
