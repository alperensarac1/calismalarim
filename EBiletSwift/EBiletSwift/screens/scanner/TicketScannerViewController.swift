import UIKit
import AVFoundation
import Foundation

/*
    TicketScannerViewController

    XIB tabanlı QR bilet kontrol ekranıdır.

    Bu ekran:
    - Sadece staff/admin rolündeki kullanıcılar için anlamlıdır.
    - QR kod okutabilir.
    - Manuel bilet kodu kontrol edebilir.
    - Backend'e ticket_code gönderir.
    - Backend sonucu approved / already_used / invalid gibi döner.

    Kullanılan framework:
    AVFoundation

    Kullanılan API:
    check/ticket_check.php

    POST:
    api_token
    ticket_code
*/
final class TicketScannerViewController: UIViewController {

    // MARK: - IBOutlet

    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var staffInfoLabel: UILabel!

    @IBOutlet private weak var cameraContainerView: UIView!
    @IBOutlet private weak var scanButton: UIButton!

    @IBOutlet private weak var manualTitleLabel: UILabel!
    @IBOutlet private weak var ticketCodeTextField: UITextField!
    @IBOutlet private weak var manualCheckButton: UIButton!

    @IBOutlet private weak var resultCardView: UIView!
    @IBOutlet private weak var resultTitleLabel: UILabel!
    @IBOutlet private weak var resultMessageLabel: UILabel!

    @IBOutlet private weak var ticketInfoLabel: UILabel!
    @IBOutlet private weak var userInfoLabel: UILabel!
    @IBOutlet private weak var eventInfoLabel: UILabel!
    @IBOutlet private weak var locationInfoLabel: UILabel!

    @IBOutlet private weak var statusLabel: UILabel!
    @IBOutlet private weak var activityIndicator: UIActivityIndicatorView!

    // MARK: - Camera Properties

    /*
        AVCaptureSession:
        Kamera görüntüsünü ve QR algılamayı yöneten ana nesnedir.
    */
    private var captureSession: AVCaptureSession?

    /*
        Kamera önizlemesini ekrana basar.
    */
    private var previewLayer: AVCaptureVideoPreviewLayer?

    /*
        Aynı QR kodun art arda defalarca okunmasını engellemek için.
    */
    private var isProcessingQRCode = false

    /*
        Kamera aktif mi?
    */
    private var isCameraRunning = false

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
        checkPermissionAndPrepareCamera()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()

        /*
            XIB layout tamamlandıktan sonra kamera preview layer boyutunu
            container view ile eşitliyoruz.
        */
        previewLayer?.frame = cameraContainerView.bounds
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        stopCamera()
    }

    // MARK: - Setup

    private func setupUI() {
        view.backgroundColor = UIColor(
            red: 245/255,
            green: 246/255,
            blue: 250/255,
            alpha: 1
        )

        title = "QR Bilet Kontrol"

        titleLabel.text = "QR Bilet Kontrol"
        titleLabel.font = .boldSystemFont(ofSize: 26)
        titleLabel.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )

        let role = SessionManager.shared.role
        let fullName = SessionManager.shared.fullName

        staffInfoLabel.text = "Görevli: \(fullName) | Rol: \(role)"
        staffInfoLabel.font = .systemFont(ofSize: 14)
        staffInfoLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
        staffInfoLabel.numberOfLines = 0

        cameraContainerView.backgroundColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )
        cameraContainerView.layer.cornerRadius = 16
        cameraContainerView.layer.masksToBounds = true

        setupButton(
            scanButton,
            title: "Kamerayı Başlat",
            backgroundColor: UIColor(
                red: 22/255,
                green: 163/255,
                blue: 74/255,
                alpha: 1
            )
        )

        manualTitleLabel.text = "Manuel Kod Kontrolü"
        manualTitleLabel.font = .boldSystemFont(ofSize: 18)
        manualTitleLabel.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )

        setupTextField(
            ticketCodeTextField,
            placeholder: "Bilet kodu"
        )

        setupButton(
            manualCheckButton,
            title: "Kodu Kontrol Et",
            backgroundColor: UIColor(
                red: 37/255,
                green: 99/255,
                blue: 235/255,
                alpha: 1
            )
        )

        resultCardView.backgroundColor = .white
        resultCardView.layer.cornerRadius = 16
        resultCardView.layer.masksToBounds = true

        resultTitleLabel.text = "Henüz kontrol yapılmadı"
        resultTitleLabel.font = .boldSystemFont(ofSize: 20)
        resultTitleLabel.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )

        resultMessageLabel.text = "QR kod okutulduğunda veya manuel kod girildiğinde sonuç burada görünecek."
        resultMessageLabel.font = .systemFont(ofSize: 14)
        resultMessageLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
        resultMessageLabel.numberOfLines = 0

        setupInfoLabel(ticketInfoLabel)
        setupInfoLabel(userInfoLabel)
        setupInfoLabel(eventInfoLabel)
        setupInfoLabel(locationInfoLabel)

        clearDetailLabels()

        statusLabel.text = ""
        statusLabel.font = .systemFont(ofSize: 14)
        statusLabel.textColor = UIColor(
            red: 100/255,
            green: 116/255,
            blue: 139/255,
            alpha: 1
        )
        statusLabel.numberOfLines = 0

        activityIndicator.hidesWhenStopped = true
        activityIndicator.stopAnimating()

        /*
            Normal kullanıcı bir şekilde bu ekrana gelirse işlemleri kapatıyoruz.
        */
        if !SessionManager.shared.isStaffOrAdmin {
            disableForUnauthorizedUser()
        }
    }

    private func setupButton(
        _ button: UIButton,
        title: String,
        backgroundColor: UIColor
    ) {
        button.setTitle(title, for: .normal)
        button.backgroundColor = backgroundColor
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .boldSystemFont(ofSize: 16)
        button.layer.cornerRadius = 12
        button.layer.masksToBounds = true
    }

    private func setupTextField(
        _ textField: UITextField,
        placeholder: String
    ) {
        textField.placeholder = placeholder
        textField.backgroundColor = UIColor(
            red: 238/255,
            green: 242/255,
            blue: 255/255,
            alpha: 1
        )
        textField.layer.cornerRadius = 12
        textField.layer.masksToBounds = true
        textField.autocapitalizationType = .none
        textField.autocorrectionType = .no

        let paddingView = UIView(
            frame: CGRect(
                x: 0,
                y: 0,
                width: 14,
                height: 44
            )
        )

        textField.leftView = paddingView
        textField.leftViewMode = .always
    }

    private func setupInfoLabel(_ label: UILabel) {
        label.font = .systemFont(ofSize: 14)
        label.textColor = UIColor(
            red: 51/255,
            green: 65/255,
            blue: 85/255,
            alpha: 1
        )
        label.numberOfLines = 0
    }

    private func clearDetailLabels() {
        ticketInfoLabel.text = ""
        userInfoLabel.text = ""
        eventInfoLabel.text = ""
        locationInfoLabel.text = ""
    }

    private func disableForUnauthorizedUser() {
        scanButton.isEnabled = false
        manualCheckButton.isEnabled = false
        ticketCodeTextField.isEnabled = false

        resultCardView.backgroundColor = UIColor(
            red: 254/255,
            green: 226/255,
            blue: 226/255,
            alpha: 1
        )

        resultTitleLabel.text = "Yetkisiz Erişim"
        resultTitleLabel.textColor = UIColor(
            red: 153/255,
            green: 27/255,
            blue: 27/255,
            alpha: 1
        )

        resultMessageLabel.text = "Bu ekran sadece staff veya admin hesabıyla kullanılabilir."
    }

    // MARK: - Camera Permission

    private func checkPermissionAndPrepareCamera() {
        guard SessionManager.shared.isStaffOrAdmin else {
            return
        }

        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            prepareCamera()

        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    if granted {
                        self?.prepareCamera()
                    } else {
                        self?.statusLabel.text = "Kamera izni verilmedi."
                    }
                }
            }

        case .denied, .restricted:
            statusLabel.text = "Kamera izni kapalı. Ayarlardan kamera izni vermen gerekir."

        @unknown default:
            statusLabel.text = "Kamera izni durumu bilinmiyor."
        }
    }

    // MARK: - Camera Setup

    private func prepareCamera() {
        /*
            Kamera cihazını alıyoruz.
        */
        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else {
            statusLabel.text = "Kamera bulunamadı."
            return
        }

        do {
            /*
                Kamera input oluşturulur.
            */
            let videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)

            let captureSession = AVCaptureSession()

            if captureSession.canAddInput(videoInput) {
                captureSession.addInput(videoInput)
            } else {
                statusLabel.text = "Kamera input eklenemedi."
                return
            }

            /*
                Metadata output:
                QR kod verisini buradan alıyoruz.
            */
            let metadataOutput = AVCaptureMetadataOutput()

            if captureSession.canAddOutput(metadataOutput) {
                captureSession.addOutput(metadataOutput)

                metadataOutput.setMetadataObjectsDelegate(
                    self,
                    queue: DispatchQueue.main
                )

                /*
                    Sadece QR kod okutmak istiyoruz.
                */
                metadataOutput.metadataObjectTypes = [.qr]
            } else {
                statusLabel.text = "QR okuyucu başlatılamadı."
                return
            }

            /*
                Kamera görüntüsü ekrana basılır.
            */
            let previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
            previewLayer.videoGravity = .resizeAspectFill
            previewLayer.frame = cameraContainerView.bounds

            cameraContainerView.layer.insertSublayer(
                previewLayer,
                at: 0
            )

            self.captureSession = captureSession
            self.previewLayer = previewLayer

            statusLabel.text = "Kamera hazır. Başlat butonuna basabilirsin."

        } catch {
            statusLabel.text = "Kamera başlatma hatası: \(error.localizedDescription)"
        }
    }

    private func startCamera() {
        guard let captureSession else {
            statusLabel.text = "Kamera hazır değil."
            return
        }

        guard !captureSession.isRunning else {
            return
        }

        isProcessingQRCode = false
        isCameraRunning = true

        /*
            startRunning ana thread üzerinde çağrılırsa kısa donma yapabilir.
            Bu yüzden background queue kullanıyoruz.
        */
        DispatchQueue.global(qos: .userInitiated).async {
            captureSession.startRunning()

            DispatchQueue.main.async {
                self.scanButton.setTitle("Kamerayı Durdur", for: .normal)
                self.statusLabel.text = "QR kod okutuluyor..."
            }
        }
    }

    private func stopCamera() {
        guard let captureSession else {
            return
        }

        guard captureSession.isRunning else {
            return
        }

        isCameraRunning = false

        DispatchQueue.global(qos: .userInitiated).async {
            captureSession.stopRunning()

            DispatchQueue.main.async {
                self.scanButton.setTitle("Kamerayı Başlat", for: .normal)
            }
        }
    }

    // MARK: - Actions

    @IBAction private func scanButtonTapped(_ sender: UIButton) {
        if isCameraRunning {
            stopCamera()
        } else {
            startCamera()
        }
    }

    @IBAction private func manualCheckButtonTapped(_ sender: UIButton) {
        let code = ticketCodeTextField.text?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        guard !code.isEmpty else {
            showAlert(message: "Bilet kodu zorunludur.")
            return
        }

        checkTicket(code: code)
    }

    // MARK: - Ticket Check

    private func checkTicket(code: String) {
        /*
            Aynı QR kodun art arda işlenmesini engelliyoruz.
        */
        if isProcessingQRCode {
            return
        }

        isProcessingQRCode = true

        setLoading(true)
        clearDetailLabels()

        resultCardView.backgroundColor = .white
        resultTitleLabel.text = "Kontrol ediliyor..."
        resultTitleLabel.textColor = UIColor(
            red: 15/255,
            green: 23/255,
            blue: 42/255,
            alpha: 1
        )
        resultMessageLabel.text = "Bilet bilgisi backend üzerinden doğrulanıyor."
        statusLabel.text = "Bilet kontrol ediliyor..."

        APIService.shared.checkTicket(
            apiToken: SessionManager.shared.apiToken,
            ticketCode: code
        ) { [weak self] result in
            guard let self else { return }

            self.setLoading(false)
            self.isProcessingQRCode = false
            self.statusLabel.text = "Kontrol tamamlandı."

            switch result {
            case .success(let response):

                guard response.success else {
                    self.showFailedResult(
                        title: "Giriş Reddedildi",
                        message: response.message
                    )
                    return
                }

                guard let ticket = response.data else {
                    self.showFailedResult(
                        title: "Bilet Kontrol Edildi",
                        message: response.message
                    )
                    return
                }

                let resultValue = ticket.result ?? "approved"

                if resultValue == "approved" {
                    self.showSuccessResult(
                        message: response.message,
                        ticket: ticket
                    )
                } else {
                    self.showFailedResult(
                        title: "Giriş Reddedildi",
                        message: response.message
                    )
                }

            case .failure(let error):
                self.showFailedResult(
                    title: "Bağlantı Hatası",
                    message: error.localizedDescription
                )
            }
        }
    }

    private func showSuccessResult(
        message: String,
        ticket: Ticket
    ) {
        resultCardView.backgroundColor = UIColor(
            red: 220/255,
            green: 252/255,
            blue: 231/255,
            alpha: 1
        )

        resultTitleLabel.text = "Giriş Onaylandı"
        resultTitleLabel.textColor = UIColor(
            red: 22/255,
            green: 101/255,
            blue: 52/255,
            alpha: 1
        )

        resultMessageLabel.text = message

        ticketInfoLabel.text = buildTicketInfo(ticket)
        userInfoLabel.text = buildUserInfo(ticket)
        eventInfoLabel.text = buildEventInfo(ticket)
        locationInfoLabel.text = buildLocationInfo(ticket)

        /*
            Başarılı okumadan sonra kamera dursun.
            Görevli isterse tekrar başlatır.
        */
        stopCamera()
    }

    private func showFailedResult(
        title: String,
        message: String
    ) {
        resultCardView.backgroundColor = UIColor(
            red: 254/255,
            green: 226/255,
            blue: 226/255,
            alpha: 1
        )

        resultTitleLabel.text = title
        resultTitleLabel.textColor = UIColor(
            red: 153/255,
            green: 27/255,
            blue: 27/255,
            alpha: 1
        )

        resultMessageLabel.text = message

        clearDetailLabels()

        /*
            Hatalı okumada da kamera dursun.
            Aynı QR tekrar tekrar okunmasın.
        */
        stopCamera()
    }

    private func setLoading(_ isLoading: Bool) {
        scanButton.isEnabled = !isLoading
        manualCheckButton.isEnabled = !isLoading
        ticketCodeTextField.isEnabled = !isLoading

        if isLoading {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }
    }

    // MARK: - Result Text Builders

    private func buildTicketInfo(_ ticket: Ticket) -> String {
        let ticketId = ticket.resolvedTicketId.map { String($0) } ?? "-"
        let ticketCode = ticket.ticketCode ?? "-"
        let status = ticket.ticketStatus ?? ticket.status ?? "-"

        return """
        Bilet ID: \(ticketId)
        Bilet Kodu: \(ticketCode)
        Durum: \(status)
        """
    }

    private func buildUserInfo(_ ticket: Ticket) -> String {
        let fullName = ticket.user?.fullName ?? "-"
        let email = ticket.user?.email ?? "-"
        let phone = ticket.user?.phone ?? "-"

        return """
        Kullanıcı: \(fullName)
        E-posta: \(email)
        Telefon: \(phone)
        """
    }

    private func buildEventInfo(_ ticket: Ticket) -> String {
        let eventTitle = ticket.event?.title ?? ticket.eventTitle ?? "-"
        let eventDate = ticket.event?.eventDate ?? "-"

        return """
        Etkinlik: \(eventTitle)
        Tarih: \(eventDate)
        """
    }

    private func buildLocationInfo(_ ticket: Ticket) -> String {
        let cityName = ticket.location?.cityName ?? ticket.city?.name ?? "-"
        let districtName = ticket.location?.districtName ?? ticket.district?.name ?? "-"
        let venueName = ticket.location?.venueName ?? ticket.venue?.name ?? "-"
        let address = ticket.location?.venueAddress ?? ticket.venue?.address ?? "-"

        return """
        Konum: \(cityName) / \(districtName)
        Sahne: \(venueName)
        Adres: \(address)
        """
    }
}

// MARK: - AVCaptureMetadataOutputObjectsDelegate

extension TicketScannerViewController: AVCaptureMetadataOutputObjectsDelegate {

    /*
        QR kod algılandığında bu fonksiyon çalışır.
    */
    func metadataOutput(
        _ output: AVCaptureMetadataOutput,
        didOutput metadataObjects: [AVMetadataObject],
        from connection: AVCaptureConnection
    ) {
        guard !isProcessingQRCode else {
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

        /*
            Okunan QR içeriğini manuel input alanına da yazıyoruz.
        */
        ticketCodeTextField.text = cleanCode

        /*
            QR okunduğu anda kontrol başlatılır.
        */
        checkTicket(code: cleanCode)
    }
}
