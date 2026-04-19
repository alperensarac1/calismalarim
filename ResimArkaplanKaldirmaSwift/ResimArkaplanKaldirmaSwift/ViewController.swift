import UIKit
import PhotosUI

final class ViewController: UIViewController {

    @IBOutlet weak var btnPick: UIButton!
    @IBOutlet weak var btnUndo: UIButton!
    @IBOutlet weak var btnReset: UIButton!
    @IBOutlet weak var btnSave: UIButton!

    @IBOutlet weak var lblInfo: UILabel!
    @IBOutlet weak var lblTolerance: UILabel!
    @IBOutlet weak var toleranceSlider: UISlider!

    @IBOutlet weak var imageEditorView: ZoomableImageView!

    private let viewModel = EditorViewModel()

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupCallbacks()
        applyState()
    }

    private func setupUI() {
        title = "Background Remover"

        btnPick.setTitle("Fotoğraf Seç", for: .normal)
        btnUndo.setTitle("Geri Al", for: .normal)
        btnReset.setTitle("Sıfırla", for: .normal)
        btnSave.setTitle("PNG Kaydet", for: .normal)

        lblInfo.numberOfLines = 0

        toleranceSlider.minimumValue = 0
        toleranceSlider.maximumValue = 255
        toleranceSlider.value = Float(viewModel.tolerance)

        imageEditorView.layer.cornerRadius = 12
        imageEditorView.clipsToBounds = true
        imageEditorView.backgroundColor = UIColor.systemGray5
    }

    private func setupCallbacks() {
        viewModel.onStateChanged = { [weak self] in
            DispatchQueue.main.async {
                self?.applyState()
            }
        }

        imageEditorView.onImageTap = { [weak self] x, y in
            self?.viewModel.onImageTapped(x: x, y: y)
        }
    }

 

    private func applyState() {
         lblInfo.text = viewModel.infoText
         lblTolerance.text = "Tolerans: \(Int(viewModel.tolerance))"
         toleranceSlider.value = Float(viewModel.tolerance)

         imageEditorView.setImage(viewModel.workingImage)

         btnUndo.isEnabled = viewModel.canUndo && !viewModel.isProcessing
         btnReset.isEnabled = viewModel.workingImage != nil && !viewModel.isProcessing
         btnSave.isEnabled = viewModel.workingImage != nil && !viewModel.isProcessing
         btnPick.isEnabled = !viewModel.isProcessing
         toleranceSlider.isEnabled = viewModel.workingImage != nil
     }

    @IBAction func pickTapped(_ sender: UIButton) {
        var config = PHPickerConfiguration()
        config.filter = .images
        config.selectionLimit = 1

        let picker = PHPickerViewController(configuration: config)
        picker.delegate = self
        present(picker, animated: true)
    }

    @IBAction func undoTapped(_ sender: UIButton) {
        viewModel.undo()
    }

    @IBAction func resetTapped(_ sender: UIButton) {
        viewModel.reset()
        imageEditorView.resetZoom()
    }

    @IBAction func saveTapped(_ sender: UIButton) {
        viewModel.exportAsPNG { [weak self] exportedURL in
                  guard let self, let exportedURL else { return }

                  let picker = UIDocumentPickerViewController(
                      forExporting: [exportedURL],
                      asCopy: true
                  )

                  picker.shouldShowFileExtensions = true
                  self.present(picker, animated: true)
              }
    }

    @IBAction func toleranceChanged(_ sender: UISlider) {
        viewModel.onToleranceChanged(CGFloat(sender.value))
    }
}

extension ViewController: PHPickerViewControllerDelegate {
    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)

        guard let itemProvider = results.first?.itemProvider else { return }
        guard itemProvider.canLoadObject(ofClass: UIImage.self) else { return }

        viewModel.setLoadingState()

        itemProvider.loadObject(ofClass: UIImage.self) { [weak self] object, error in
            guard let self else { return }

            DispatchQueue.main.async {
                if let image = object as? UIImage {
                    let normalized = image.normalizedImage()
                    self.viewModel.loadImage(normalized)
                    self.imageEditorView.resetZoom()
                } else {
                    self.viewModel.setErrorState("Resim yüklenemedi.")
                }
            }
        }
    }
}
