//
//  AnasayfaVC.swift
//  MemeShareSwift
//
//  Created by Alperen Saraç on 30.08.2025.
//

import UIKit
import Combine
class AnasayfaVC: UIViewController {

    @IBOutlet weak var tableViewOdalar: UITableView!
    var userId: Int = 0


    private var rooms: [OdaModel] = []
    private let vm = OdaViewModel()
    private var bag = Set<AnyCancellable>()

    // Boş liste mesajı
    private lazy var emptyStateLabel: UILabel = {
        let lbl = UILabel()
        lbl.text = "Henüz oda yok"
        lbl.textColor = .secondaryLabel
        lbl.textAlignment = .center
        lbl.numberOfLines = 0
        lbl.isHidden = true
        return lbl
    }()
    override func viewDidLoad() {
          super.viewDidLoad()

          title = "Odalar"
          setupTableView()
          setupBindings()

          Task { await fetchOdalar() }
      }
    private func setupTableView() {
            tableViewOdalar.dataSource = self
            tableViewOdalar.delegate = self
            tableViewOdalar.backgroundColor = .systemBackground
            tableViewOdalar.separatorStyle = .singleLine

            // Önce sabit yükseklikle görünür kıl; sonra dilersen automatic'e al
            tableViewOdalar.rowHeight = 72
            tableViewOdalar.estimatedRowHeight = 72
            // İleride: tableViewOdalar.rowHeight = UITableView.automaticDimension

            // Basic hücre fallback (identifier: "basic")
            tableViewOdalar.register(UITableViewCell.self, forCellReuseIdentifier: "basic")

            // Empty state
            emptyStateLabel.translatesAutoresizingMaskIntoConstraints = false
            view.addSubview(emptyStateLabel)
            NSLayoutConstraint.activate([
                emptyStateLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
                emptyStateLabel.centerYAnchor.constraint(equalTo: view.centerYAnchor),
                emptyStateLabel.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor, constant: 24),
                emptyStateLabel.trailingAnchor.constraint(lessThanOrEqualTo: view.trailingAnchor, constant: -24)
            ])
        }

    private func setupBindings() {
        vm.$odaOlusturmaSonucu
            .receive(on: DispatchQueue.main)
            .sink { [weak self] resOpt in
                guard let self = self, let res = resOpt else { return }

                if res.success, let roomId = res.roomId, let code = res.roomCode {
                    self.showToast("Oda oluşturuldu: \(code)")
                    let yeni = OdaModel(odaId: roomId, roomCode: code, createdBy: self.userId)

                    self.rooms.append(yeni)
                    self.emptyStateLabel.isHidden = !self.rooms.isEmpty

                    let newIndex = IndexPath(row: self.rooms.count - 1, section: 0)
                    if self.tableViewOdalar.window != nil {
                        self.tableViewOdalar.performBatchUpdates({
                            self.tableViewOdalar.insertRows(at: [newIndex], with: .automatic)
                        })
                    } else {
                        self.tableViewOdalar.reloadData()
                    }

                    print("✅ oda eklendi, toplam: \(self.rooms.count)")
                } else {
                    self.showToast("Hata: \(res.message ?? "Bilinmeyen hata")")
                }
            }
            .store(in: &bag)
    }



    @IBAction func btnOdaOlustur(_ sender: Any) {
        vm.createRoom(userId: userId)
    }
    @IBAction func btnOda(_ sender: Any) {
        let alert = UIAlertController(title: "Oda Katılım", message: "Oda kodunu girin", preferredStyle: .alert)
                alert.addTextField { tf in
                    tf.placeholder = "Oda kodu"
                    tf.autocapitalizationType = .allCharacters
                }
                alert.addAction(UIAlertAction(title: "İptal", style: .cancel))
                alert.addAction(UIAlertAction(title: "Katıl", style: .default, handler: { [weak self] _ in
                    guard let self else { return }
                    let code = alert.textFields?.first?.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                    guard !code.isEmpty else {
                        self.showToast("Kod boş olamaz")
                        return
                    }
                    Task {
                        do {
                            let res = try await APIService.shared.joinRoom(userId: self.userId, roomCode: code)
                            if res.success {
                                self.showToast("Odaya katıldınız")
                                await self.fetchOdalar()
                            } else {
                                self.showToast("Katılım başarısız")
                            }
                        } catch {
                            self.showToast("Hata: \(error.localizedDescription)")
                        }
                    }
                }))
                present(alert, animated: true)
    }
    private func fetchOdalar() async {
        do {
            let list = try await APIService.shared.getJoinedRooms(userId: userId)
            await MainActor.run {
                self.rooms = list
                self.emptyStateLabel.isHidden = !list.isEmpty
                self.tableViewOdalar.reloadData()
                print("🔄 rooms.count =", list.count)
            }
        } catch let apiErr as APIError {
            await MainActor.run {
                switch apiErr {
                case .server(let status):
                    self.showToast("Sunucu hatası (\(status))")
                case .decodeFailed:
                    self.showToast("Yanıt çözümlenemedi (decode)")
                default:
                    self.showToast("Bağlantı hatası: \(apiErr.localizedDescription)")
                }
            }
        } catch {
            await MainActor.run {
                self.showToast("Hata: \(error.localizedDescription)")
            }
        }
    }


        // MARK: - Geçiş

        private func navigateToOda(oda: OdaModel) {
            guard let vc = storyboard?.instantiateViewController(withIdentifier: "OdaVC") as? OdaVC else { return }
            vc.roomId = oda.odaId
            vc.userId = userId
            navigationController?.pushViewController(vc, animated: true)
        }
    }

    // MARK: - Table

    extension AnasayfaVC: UITableViewDataSource, UITableViewDelegate {

        func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
            rooms.count
        }

        func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
            let oda = rooms[indexPath.row]

            // Önce custom hücreyi dene
            if let cell = tableView.dequeueReusableCell(withIdentifier: "OdaCell") as? OdaCell {
                cell.configure(with: oda)
                cell.selectionStyle = .none
                return cell
            }

            // Fallback: basic cell (identifier: "basic")
            let cell = tableView.dequeueReusableCell(withIdentifier: "basic", for: indexPath)
            var content = cell.defaultContentConfiguration()
            content.text = "Oda: \(oda.roomCode)"
            content.secondaryText = "ID: \(oda.odaId)  •  by \(oda.createdBy)"
            cell.contentConfiguration = content
            cell.selectionStyle = .none
            return cell
        }

        func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
            tableView.deselectRow(at: indexPath, animated: true)
            navigateToOda(oda: rooms[indexPath.row])
        }
    }
