import UIKit

final class HomeViewController: UIViewController {

    @IBOutlet private weak var tableView: UITableView!
    @IBOutlet private weak var btnNew: UIButton!
    @IBOutlet private weak var btnAddAddress: UIButton!

    private let tokenStore = TokenStore()
    private lazy var api = APIClient(
        baseURL: URL(string: "https://alperensaracdeneme.com/cargo/")!,
        tokenStore: tokenStore
    )
    private lazy var vm = HomeViewModel(api: api)

    private let refresh = UIRefreshControl()

    private enum Section: Int, CaseIterable {
        case shipments = 0
        case addresses = 1
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Home"

        tableView.dataSource = self
        tableView.delegate = self

        tableView.register(UINib(nibName: "ShipmentCell", bundle: nil), forCellReuseIdentifier: "ShipmentCell")
        tableView.register(UINib(nibName: "AddressCell", bundle: nil), forCellReuseIdentifier: "AddressCell")

        tableView.refreshControl = refresh
        refresh.addTarget(self, action: #selector(pulledToRefresh), for: .valueChanged)

        vm.onState = { [weak self] st in
            guard let self else { return }
            switch st {
            case .idle:
                break
            case .loading:
                break
            case .loaded:
                self.refresh.endRefreshing()
                self.tableView.reloadData()
            case .error(let msg):
                self.refresh.endRefreshing()
                self.showAlert(title: "Hata", message: msg)
            }
        }

        vm.refresh()
    }

    @objc private func pulledToRefresh() {
        vm.refresh()
    }

    @IBAction private func newShipmentTapped(_ sender: UIButton) {
        let vc = CreateShipmentViewController(nibName: "CreateShipmentViewController", bundle: nil)
        vc.onCreated = { [weak self] in
            self?.vm.refresh()
        }
        navigationController?.pushViewController(vc, animated: true)
    }

    @IBAction private func addAddressTapped(_ sender: UIButton) {
        let vc = AddressCreateViewController(nibName: "AddressCreateViewController", bundle: nil)
          vc.onCreated = { [weak self] in
              self?.vm.refresh()   // address_list + shipment_list yeniden çeker
          }
          navigationController?.pushViewController(vc, animated: true)
    }

    private func showAlert(title: String, message: String) {
        let a = UIAlertController(title: title, message: message, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
}

extension HomeViewController: UITableViewDataSource, UITableViewDelegate {

    func numberOfSections(in tableView: UITableView) -> Int {
        Section.allCases.count
    }

    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        let sec = Section(rawValue: section)!
        return sec == .shipments ? "Gönderiler" : "Adresler"
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let sec = Section(rawValue: section)!
        return (sec == .shipments) ? vm.shipments.count : vm.addresses.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let sec = Section(rawValue: indexPath.section)!

        if sec == .shipments {
            let cell = tableView.dequeueReusableCell(withIdentifier: "ShipmentCell", for: indexPath) as! ShipmentCell
            cell.bind(vm.shipments[indexPath.row])
            return cell
        } else {
            let cell = tableView.dequeueReusableCell(withIdentifier: "AddressCell", for: indexPath) as! AddressCell
            let addr = vm.addresses[indexPath.row]
            cell.bind(addr)

            cell.onSetDefault = { [weak self] in
                self?.setDefaultAddress(addr.id)
            }
            cell.onDelete = { [weak self] in
                self?.confirmDeleteAddress(addr.id)
            }
            return cell
        }
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)

        if indexPath.section == Section.shipments.rawValue {
            let s = vm.shipments[indexPath.row]
            let vc = ShipmentDetailViewController(nibName: "ShipmentDetailViewController", bundle: nil)
            vc.shipment = s
            navigationController?.pushViewController(vc, animated: true)
        }
    }

    private func setDefaultAddress(_ id: Int) {
        Task { @MainActor in
            do {
                try await vm.setDefaultAddress(id: id)
                vm.refresh()
            } catch {
                showAlert(title: "Hata", message: error.localizedDescription)
            }
        }
    }

    private func confirmDeleteAddress(_ id: Int) {
        let a = UIAlertController(title: "Adres Sil", message: "Bu adres silinsin mi?", preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "İptal", style: .cancel))
        a.addAction(UIAlertAction(title: "Sil", style: .destructive) { [weak self] _ in
            self?.deleteAddress(id)
        })
        present(a, animated: true)
    }

    private func deleteAddress(_ id: Int) {
        Task { @MainActor in
            do {
                try await vm.deleteAddress(id: id)
                vm.refresh()
            } catch {
                showAlert(title: "Hata", message: error.localizedDescription)
            }
        }
    }
}
