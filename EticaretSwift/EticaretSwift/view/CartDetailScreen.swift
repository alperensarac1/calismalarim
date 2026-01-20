//
//  CartDetailScreen.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 14.01.2026.
//

import UIKit

class CartDetailScreen: UIViewController {
    
    @IBOutlet private weak var tableView: UITableView!
    @IBOutlet private weak var totalLabel: UILabel!
    @IBOutlet private weak var checkoutButton: UIButton!
    
    private var items: [CartItemUI] = []
    private var totalAmount: Double = 0
    private var isLoading = false
    
    init() {
        super.init(nibName: "CartDetailScreen", bundle: nil)
    }
    required init?(coder: NSCoder) { fatalError("XIB kullanıyoruz") }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupTable()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // login şart
        if !AuthManager.shared.isLoggedIn {
            let vc = LoginViewController()
            navigationController?.pushViewController(vc, animated: true)
            return
        }
        
        loadCart()
    }
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        title = "Sepetim"
        
        if #available(iOS 15.0, *) {
            var cfg = UIButton.Configuration.filled()
            cfg.title = "Ödeme"
            cfg.cornerStyle = .large
            checkoutButton.configuration = cfg
        } else {
            checkoutButton.setTitle("Ödeme", for: .normal)
        }
    }
    
    private func setupTable() {
        tableView.dataSource = self
        tableView.delegate = self
        tableView.separatorStyle = .none
        
        tableView.register(UINib(nibName: "CartItemCell", bundle: nil),
                           forCellReuseIdentifier: "CartItemCell")
        
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 120
    }
    
    private func setLoading(_ loading: Bool) {
        isLoading = loading
        checkoutButton.isEnabled = !loading
        checkoutButton.alpha = loading ? 0.6 : 1.0
    }
    private func showError(_ msg: String) {
        let a = UIAlertController(title: "Hata", message: msg, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
    private func loadCart() {
        guard !isLoading else { return }
        setLoading(true)
        
        Task {
            do {
                let cart = try await ApiClient.shared.getCart()
                
                let mapped: [CartItemUI] = cart.items.map { it in
                    CartItemUI(
                        itemId: it.item_id,
                        productId: it.product_id,
                        title: it.name,
                        unitPrice: it.sale_price,       // indirimli fiyat
                        priceText: String(format: "₺ %.2f", it.sale_price),
                        qty: it.quantity,
                        imageUrl: it.image_url
                    )
                }
                
                await MainActor.run {
                    self.items = mapped
                    self.totalAmount = cart.total
                    self.totalLabel.text = String(format: "₺ %.2f", cart.total)
                    
                    self.tableView.reloadData()
                    self.setLoading(false)
                    
                    let hasItems = !mapped.isEmpty
                    self.checkoutButton.isEnabled = hasItems
                    self.checkoutButton.alpha = hasItems ? 1.0 : 0.4
                }
            } catch {
                await MainActor.run {
                    self.setLoading(false)
                    self.showError(error.localizedDescription)
                }
            }
        }
    }
    
    private func updateItem(itemId: Int, newQty: Int) {
        guard newQty >= 1 else { return }
        guard !isLoading else { return }
        
        setLoading(true)
        
        Task {
            do {
                _ = try await ApiClient.shared.updateCartItem(itemId: itemId, quantity: newQty)
                // en garantisi: serverdan yeniden çek
                let cart = try await ApiClient.shared.getCart()
                
                let mapped = cart.items.map { it in
                    CartItemUI(
                        itemId: it.item_id,
                        productId: it.product_id,
                        title: it.name,
                        unitPrice: it.sale_price,
                        priceText: String(format: "₺ %.2f", it.sale_price),
                        qty: it.quantity,
                        imageUrl: it.image_url
                    )
                }
                
                await MainActor.run {
                    self.items = mapped
                    self.totalAmount = cart.total
                    self.totalLabel.text = String(format: "₺ %.2f", cart.total)
                    self.tableView.reloadData()
                    self.setLoading(false)
                }
            } catch {
                await MainActor.run {
                    self.setLoading(false)
                    self.showError(error.localizedDescription)
                }
            }
        }
    }
    
    private func deleteItem(itemId: Int) {
        guard !isLoading else { return }
        setLoading(true)
        
        Task {
            do {
                _ = try await ApiClient.shared.deleteCartItem(itemId: itemId)
                // yine reload
                let cart = try await ApiClient.shared.getCart()
                let mapped = cart.items.map { it in
                    CartItemUI(
                        itemId: it.item_id,
                        productId: it.product_id,
                        title: it.name,
                        unitPrice: it.sale_price,
                        priceText: String(format: "₺ %.2f", it.sale_price),
                        qty: it.quantity,
                        imageUrl: it.image_url
                    )
                }
                
                await MainActor.run {
                    self.items = mapped
                    self.totalAmount = cart.total
                    self.totalLabel.text = String(format: "₺ %.2f", cart.total)
                    self.tableView.reloadData()
                    self.setLoading(false)
                    
                    let hasItems = !mapped.isEmpty
                    self.checkoutButton.isEnabled = hasItems
                    self.checkoutButton.alpha = hasItems ? 1.0 : 0.4
                }
            } catch {
                await MainActor.run {
                    self.setLoading(false)
                    self.showError(error.localizedDescription)
                }
            }
        }
    }
    
    @IBAction private func checkoutTapped(_ sender: UIButton) {
        if CartManager.shared.items.isEmpty {
            guard !items.isEmpty else {
                showError("Sepet boş.")
                return
            }
            
            // Basit checkout ekranına geç
            let vc = CheckOutViewController(totalText: totalLabel.text ?? "₺ 0.00")
            navigationController?.pushViewController(vc, animated: true)
        }
    }
}

extension CartDetailScreen: UITableViewDataSource, UITableViewDelegate {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        items.count
    }

    func tableView(_ tableView: UITableView,
                   cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        let cell = tableView.dequeueReusableCell(withIdentifier: "CartItemCell", for: indexPath) as! CartItemCell
        let item = items[indexPath.row]

        cell.configure(title: item.title, priceText: item.priceText, qty: item.qty,image: item.imageUrl!)

        cell.onPlus = { [weak self] in
            self?.updateItem(itemId: item.itemId, newQty: item.qty + 1)
        }

        cell.onMinus = { [weak self] in
            if item.qty > 1 {
                self?.updateItem(itemId: item.itemId, newQty: item.qty - 1)
            }
        }

        cell.onDelete = { [weak self] in
            self?.deleteItem(itemId: item.itemId)
        }

        return cell
    }
}

// MARK: - UI model
struct CartItemUI {
    let itemId: Int
    let productId: Int
    let title: String
    let unitPrice: Double
    let priceText: String
    let qty: Int
    let imageUrl: String?
}
