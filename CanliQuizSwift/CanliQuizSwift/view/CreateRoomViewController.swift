import UIKit

final class CreateRoomViewController: UIViewController {

    /*
        Oda oluşturma ekranı.

        Akış:
        1. Kullanıcı adını girer
        2. Soru süresini girer
        3. WebSocket bağlantısı kurulur
        4. create_room mesajı Python server'a gönderilir
        5. Server room_created cevabı döner
        6. OwnerRoomViewController ekranına geçilir
    */

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var usernameTextField: UITextField!
    @IBOutlet weak var questionTimeTextField: UITextField!
    @IBOutlet weak var createButton: UIButton!
    @IBOutlet weak var statusLabel: UILabel!

    private var pendingUsername: String = ""
    private var pendingQuestionTime: Int = 20
    private var shouldSendCreateRoomAfterConnect = false

    override func viewDidLoad() {
        super.viewDidLoad()

        configureUI()

        WebSocketManager.shared.delegate = self
    }

    deinit {
        if WebSocketManager.shared.delegate === self {
            WebSocketManager.shared.delegate = nil
        }
    }

    private func configureUI() {
        title = "Oda Oluştur"

        view.backgroundColor = UIColor(
            red: 248 / 255,
            green: 250 / 255,
            blue: 252 / 255,
            alpha: 1
        )

        titleLabel.text = "Oda Oluştur"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 28)
        titleLabel.textColor = UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1)

        usernameTextField.placeholder = "Kullanıcı adı"
        usernameTextField.borderStyle = .roundedRect
        usernameTextField.autocapitalizationType = .none

        questionTimeTextField.placeholder = "Soru süresi örn: 20"
        questionTimeTextField.borderStyle = .roundedRect
        questionTimeTextField.keyboardType = .numberPad

        createButton.setTitle("Odayı Oluştur", for: .normal)
        createButton.backgroundColor = UIColor.systemPurple
        createButton.tintColor = .white
        createButton.layer.cornerRadius = 12
        createButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 17)

        statusLabel.text = ""
        statusLabel.font = UIFont.systemFont(ofSize: 15)
        statusLabel.textColor = UIColor(red: 55/255, green: 65/255, blue: 81/255, alpha: 1)
        statusLabel.numberOfLines = 0
    }

    @IBAction func createButtonTapped(_ sender: UIButton) {
        createRoom()
    }

    private func createRoom() {
        let username = usernameTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let timeText = questionTimeTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        guard username.isEmpty == false else {
            statusLabel.text = "Kullanıcı adı boş olamaz."
            return
        }

        let questionTime = Int(timeText) ?? 20

        guard questionTime >= 5 else {
            statusLabel.text = "Soru süresi en az 5 saniye olmalı."
            return
        }

        pendingUsername = username
        pendingQuestionTime = questionTime

        statusLabel.text = "Sunucuya bağlanılıyor..."

        if WebSocketManager.shared.isConnected {
            sendCreateRoomMessage()
        } else {
            shouldSendCreateRoomAfterConnect = true
            WebSocketManager.shared.connect()
        }
    }

    private func sendCreateRoomMessage() {
        shouldSendCreateRoomAfterConnect = false

        let message = SocketMessageFactory.createRoom(
            username: pendingUsername,
            questionTime: pendingQuestionTime
        )

        WebSocketManager.shared.send(message)

        statusLabel.text = "Oda oluşturma isteği gönderildi..."
    }

    private func handleSocketMessage(_ message: String) {
        guard let data = message.data(using: .utf8) else {
            return
        }

        do {
            guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
                return
            }

            let type = json["type"] as? String ?? ""

            if type == SocketMessageType.roomCreated.rawValue {
                let roomCode = json["room_code"] as? String ?? ""
                let username = json["username"] as? String ?? pendingUsername
                let questionTime = json["question_time"] as? Int ?? pendingQuestionTime

                statusLabel.text = "Oda oluşturuldu: \(roomCode)"

                let vc = OwnerRoomViewController(
                    nibName: "OwnerRoomViewController",
                    bundle: nil
                )

                vc.roomCode = roomCode
                vc.username = username
                vc.questionTime = questionTime

                navigationController?.pushViewController(vc, animated: true)

            } else if type == SocketMessageType.error.rawValue {
                statusLabel.text = json["message"] as? String ?? "Bilinmeyen hata oluştu."
            }

        } catch {
            statusLabel.text = "JSON okuma hatası: \(error.localizedDescription)"
        }
    }
}

extension CreateRoomViewController: WebSocketManagerDelegate {

    func webSocketDidConnect() {
        statusLabel.text = "Sunucuya bağlandı."

        if shouldSendCreateRoomAfterConnect {
            sendCreateRoomMessage()
        }
    }

    func webSocketDidReceiveMessage(_ message: String) {
        handleSocketMessage(message)
    }

    func webSocketDidDisconnect() {
        statusLabel.text = "Sunucu bağlantısı kapandı."
    }

    func webSocketDidReceiveError(_ error: String) {
        statusLabel.text = "Bağlantı hatası: \(error)"
    }
}
