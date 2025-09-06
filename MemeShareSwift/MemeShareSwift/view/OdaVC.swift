//
//  OdaVC.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 30.08.2025.
//

import UIKit
import PhotosUI
import Combine
import AVKit
import UniformTypeIdentifiers

class OdaVC: UIViewController {

    @IBOutlet weak var tvOdaId: UILabel!
    @IBOutlet weak var collectionViewGonderi: UICollectionView!
        var roomId: Int = 0
        var userId: Int = 0


        private let baseURL = URL(string: "https://alperensaracdeneme.com/meme/")!
        private let vm = OdaViewModel()
        private var bag = Set<AnyCancellable>()
        private var items: [GonderiModel] = []

        private var pickedVideoFileURL: URL? = nil
        private var pickedImage: UIImage? = nil

        override func viewDidLoad() {
            super.viewDidLoad()

            title = "Oda"
            tvOdaId.text = "Oda #\(roomId)"

            // CollectionView
            collectionViewGonderi.dataSource = self
            collectionViewGonderi.delegate = self
            // Eğer storyboard’da hücreyi register etmediysen:
            // collectionViewGonderi.register(UINib(nibName: "GonderiCell", bundle: nil), forCellWithReuseIdentifier: "GonderiCell")

            if let flow = collectionViewGonderi.collectionViewLayout as? UICollectionViewFlowLayout {
                flow.itemSize = CGSize(width: 200, height: 240)
                flow.minimumLineSpacing = 12
                flow.minimumInteritemSpacing = 8
            }

            // VM bağları
            vm.$gonderiler
                .receive(on: DispatchQueue.main)
                .sink { [weak self] list in
                    guard let self else { return }
                    self.items = list ?? []
                    self.collectionViewGonderi.reloadData()
                }
                .store(in: &bag)

            vm.$uploadResult
                .compactMap { $0 }
                .receive(on: DispatchQueue.main)
                .sink { [weak self] msg in
                    self?.showToast(msg)
                    self?.vm.getAllMedia(roomId: self?.roomId ?? 0)
                }
                .store(in: &bag)

            // İlk yük
            vm.getAllMedia(roomId: roomId)
        }

    
    @IBAction func btnMemeEkle(_ sender: Any) {
        presentPicker()
    }
    // MARK: - Picker
        private func presentPicker() {
            var config = PHPickerConfiguration(photoLibrary: .shared())
            config.filter = .any(of: [.images, .videos])
            config.selectionLimit = 1

            let picker = PHPickerViewController(configuration: config)
            picker.delegate = self
            present(picker, animated: true)
        }

        /// Caption sor ve upload et
        private func askCaptionAndUpload(isVideo: Bool) {
            let alert = UIAlertController(title: "Paylaş", message: "Açıklama ekle", preferredStyle: .alert)
            alert.addTextField { tf in
                tf.placeholder = "Açıklama…"
            }
            alert.addAction(UIAlertAction(title: "İptal", style: .cancel))
            alert.addAction(UIAlertAction(title: "Gönder", style: .default, handler: { [weak self] _ in
                guard let self else { return }
                let caption = alert.textFields?.first?.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

                if isVideo {
                    guard let fileURL = self.pickedVideoFileURL else {
                        self.showToast("Video bulunamadı.")
                        return
                    }
                    VideoUploader.uploadVideo(
                        videoName: UUID().uuidString,
                        fileURL: fileURL,
                        roomId: self.roomId,
                        userId: self.userId,
                        caption: caption,
                        uploadURL: self.baseURL.appendingPathComponent("media-upload-video.php")
                    ) { [weak self] success, response in
                        guard let self else { return }
                        self.showToast(success ? "✅ Video yüklendi" : "⚠️ Yükleme başarısız")
                        self.vm.getAllMedia(roomId: self.roomId)
                    }
                } else {
                    guard let image = self.pickedImage else {
                        self.showToast("Görsel bulunamadı.")
                        return
                    }
                    self.vm.uploadImage(image: image, roomId: self.roomId, userId: self.userId, caption: caption)
                }
            }))
            present(alert, animated: true)
        }


        private func playVideo(at remotePath: String) {
            let url = baseURL.appendingPathComponent(remotePath)
            let player = AVPlayer(url: url)
            let vc = AVPlayerViewController()
            vc.player = player
            present(vc, animated: true) { player.play() }
        }
    }

    // MARK: - UICollectionView DataSource / Delegate
    extension OdaVC: UICollectionViewDataSource, UICollectionViewDelegate {
        func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int { items.count }

        func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
            let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "GonderiCell", for: indexPath) as! GonderiCell
            cell.configure(with: items[indexPath.item], baseURL: baseURL)
            return cell
        }

        func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
            let item = items[indexPath.item]
            if item.mediaType == "video" {
                playVideo(at: item.mediaUrl)
            } else if item.mediaType == "image" {
                let url = baseURL.appendingPathComponent(item.mediaUrl)
                let vc = ImagePreviewVC(url: url)
                present(vc, animated: true)
            }
        }
    }

    // MARK: - PHPicker Delegate
    extension OdaVC: PHPickerViewControllerDelegate {
        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            dismiss(animated: true)

            guard let item = results.first else { return }
            let provider = item.itemProvider

            // Video mu?
            if provider.hasItemConformingToTypeIdentifier(UTType.movie.identifier) {
                provider.loadFileRepresentation(forTypeIdentifier: UTType.movie.identifier) { [weak self] url, error in
                    guard let self else { return }
                    if let error = error {
                        DispatchQueue.main.async { self.showToast(error.localizedDescription) }
                        return
                    }
                    guard let tmpURL = url else { return }

                    // Sandbox'a kopyala (geçici dosya)
                    let dst = FileManager.default.temporaryDirectory.appendingPathComponent("\(UUID().uuidString).mp4")
                    do {
                        if FileManager.default.fileExists(atPath: dst.path) {
                            try FileManager.default.removeItem(at: dst)
                        }
                        try FileManager.default.copyItem(at: tmpURL, to: dst)
                        self.pickedVideoFileURL = dst
                        DispatchQueue.main.async { self.askCaptionAndUpload(isVideo: true) }
                    } catch {
                        DispatchQueue.main.async { self.showToast("Kopyalama hatası: \(error.localizedDescription)") }
                    }
                }
                return
            }

            // Görsel mi?
            if provider.canLoadObject(ofClass: UIImage.self) {
                provider.loadObject(ofClass: UIImage.self) { [weak self] object, error in
                    guard let self else { return }
                    if let error = error {
                        DispatchQueue.main.async { self.showToast(error.localizedDescription) }
                        return
                    }
                    guard let image = object as? UIImage else { return }
                    self.pickedImage = image
                    DispatchQueue.main.async { self.askCaptionAndUpload(isVideo: false) }
                }
            }
        }
    }

    //
    // MARK: - Basit image preview VC (opsiyonel)
    //
    final class ImagePreviewVC: UIViewController {
        private let url: URL
        private let imageView = UIImageView()

        init(url: URL) {
            self.url = url
            super.init(nibName: nil, bundle: nil)
            modalPresentationStyle = .fullScreen
            view.backgroundColor = .black
        }
        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        override func viewDidLoad() {
            super.viewDidLoad()
            imageView.contentMode = .scaleAspectFit
            imageView.frame = view.bounds
            imageView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            view.addSubview(imageView)
            imageView.setRemoteImage(url: url)

            let tap = UITapGestureRecognizer(target: self, action: #selector(close))
            view.addGestureRecognizer(tap)
        }

        @objc private func close() { dismiss(animated: true) }
    }

