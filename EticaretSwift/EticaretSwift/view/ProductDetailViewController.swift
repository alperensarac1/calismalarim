//
//  ProductDetailViewController.swift
//  EticaretSwift
//
//  Created by Alperen Saraç on 14.01.2026.
//

import UIKit

final class ProductDetailViewController: UIViewController {

    @IBOutlet private weak var productImageView: UIImageView!
    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var priceLabel: UILabel!
    @IBOutlet private weak var descriptionLabel: UILabel!

    @IBOutlet private weak var minusButton: UIButton!
    @IBOutlet private weak var plusButton: UIButton!
    @IBOutlet private weak var qtyLabel: UILabel!
    @IBOutlet private weak var addToCartButton: UIButton!

    private let productId: Int
    private var qty: Int = 1 { didSet { updateQtyUI() } }

    // Ürün detayı (şimdilik stub, sonra API)
    private var product: ProductDetailUI?

    init(productId: Int) {
        self.productId = productId
        super.init(nibName: "ProductDetailViewController", bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("XIB kullanıyoruz")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        loadProductDetail()
    }

    private func setupUI() {
        view.backgroundColor = .systemBackground
        title = "Ürün Detayı"

        productImageView.contentMode = .scaleAspectFill
        productImageView.clipsToBounds = true
        productImageView.layer.cornerRadius = 14

        minusButton.layer.cornerRadius = 10
        plusButton.layer.cornerRadius = 10
        minusButton.backgroundColor = .systemGray6
        plusButton.backgroundColor = .systemGray6

        qtyLabel.textAlignment = .center
        qtyLabel.font = .systemFont(ofSize: 16, weight: .semibold)

        if #available(iOS 15.0, *) {
            var cfg = UIButton.Configuration.filled()
            cfg.title = "Sepete Ekle"
            cfg.cornerStyle = .large
            addToCartButton.configuration = cfg
        } else {
            addToCartButton.setTitle("Sepete Ekle", for: .normal)
        }

        updateQtyUI()
    }

    private func updateQtyUI() {
        qtyLabel.text = "\(qty)"
        minusButton.isEnabled = qty > 1
        minusButton.alpha = qty > 1 ? 1.0 : 0.4
    }

    private func loadProductDetail() {
        setScreenLoading(true)

        Task {
            do {
                let dto = try await ApiClient.shared.getProduct(id: productId)

                let disc = dto.discount_percent
                let finalPrice: Double
                if let disc, disc > 0 {
                    finalPrice = dto.price * (1.0 - disc / 100.0)
                } else {
                    finalPrice = dto.price
                }

                let desc: String
                if let d = dto.description, !d.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    desc = d
                } else {
                    desc = "SKU: \(dto.sku)\nStok: \(dto.stock_qty)"
                }

                let ui = ProductDetailUI(
                    id: dto.id,
                    title: dto.name,
                    price: dto.price,
                    discountPercent: dto.discount_percent,
                    finalPrice: dto.price,   // ✅
                    description: desc,
                    imageUrl: dto.image_url,
                    stockQty: dto.stock_qty
                )


                await MainActor.run {
                    self.product = ui
                    self.bind(ui)
                    self.setScreenLoading(false)
                }
            } catch {
                await MainActor.run {
                    self.setScreenLoading(false)
                    self.showError(error.localizedDescription)
                }
            }
        }
    }


    private func bind(_ p: ProductDetailUI) {
        titleLabel.text = p.title
        priceLabel.text = String(format: "₺ %.2f", p.finalPrice)
        descriptionLabel.text = p.description

        productImageView.image = UIImage(systemName: "photo")

        if let s = p.imageUrl, let url = URL(string: s) {
            Task {
                do {
                    let (data, _) = try await URLSession.shared.data(from: url)
                    if let img = UIImage(data: data) {
                        await MainActor.run { self.productImageView.image = img }
                    }
                } catch { }
            }
        }

        // stok 0 ise butonu kapat
        addToCartButton.isEnabled = p.stockQty > 0
        addToCartButton.alpha = p.stockQty > 0 ? 1.0 : 0.4
    }


    @IBAction private func minusTapped(_ sender: UIButton) {
        if qty > 1 { qty -= 1 }
    }

    @IBAction private func plusTapped(_ sender: UIButton) {
        qty += 1
    }

    @IBAction private func addToCartTapped(_ sender: UIButton) {
        guard let p = product else { return }

         // Login zorunlu (cart.php Bearer istiyor)
         if !AuthManager.shared.isLoggedIn {
             let vc = LoginViewController()
             navigationController?.pushViewController(vc, animated: true)
             return
         }

         addToCartButton.isEnabled = false
         addToCartButton.alpha = 0.6

         Task {
             do {
                 _ = try await ApiClient.shared.addToCart(productId: p.id, quantity: qty)

                 // İstersen CartManager'ı server yerine local kullanmayacaksan tamamen kaldırabiliriz,
                 // ama şimdilik badge vb yok; yine de local state tutmak istersen:
                 // CartManager.shared.add(productId: p.id, title: p.title, unitPrice: p.finalPrice, imageUrl: p.imageUrl, qty: qty)

                 await MainActor.run {
                     self.addToCartButton.isEnabled = true
                     self.addToCartButton.alpha = 1.0

                     let a = UIAlertController(title: "Sepete eklendi ✅",
                                               message: "\(p.title)\nAdet: \(self.qty)",
                                               preferredStyle: .alert)
                     a.addAction(UIAlertAction(title: "Tamam", style: .default))
                     self.present(a, animated: true)
                 }
             } catch {
                 await MainActor.run {
                     self.addToCartButton.isEnabled = true
                     self.addToCartButton.alpha = 1.0
                     self.showError(error.localizedDescription)
                 }
             }
         }
      }
    private func setScreenLoading(_ loading: Bool) {
        view.isUserInteractionEnabled = !loading
    }

    private func showError(_ msg: String) {
        let a = UIAlertController(title: "Hata", message: msg, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }

      private func parsePrice(_ text: String) -> Double {
          let cleaned = text
              .replacingOccurrences(of: "₺", with: "")
              .replacingOccurrences(of: " ", with: "")
              .replacingOccurrences(of: ",", with: ".")
          return Double(cleaned) ?? 0
      }
}
struct ProductDetailUI {
    let id: Int
    let title: String
    let price: Double
    let discountPercent: Double?
    let finalPrice: Double
    let description: String
    let imageUrl: String?
    let stockQty: Int
}
