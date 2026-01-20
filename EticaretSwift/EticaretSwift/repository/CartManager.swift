import Foundation

final class CartManager {

    static let shared = CartManager()
    private init() {}

    struct Item {
        let productId: Int
        var title: String
        var unitPrice: Double
        var imageUrl: String?
        var qty: Int
    }

    private(set) var items: [Item] = []

    // Basit observer: sepet değişince ekranlara haber ver
    static let changedNotification = Notification.Name("CartManagerChanged")

    // MARK: - Public API

    func add(productId: Int, title: String, unitPrice: Double, imageUrl: String?, qty: Int = 1) {
        guard qty > 0 else { return }

        if let i = items.firstIndex(where: { $0.productId == productId }) {
            items[i].qty += qty
        } else {
            items.append(Item(productId: productId, title: title, unitPrice: unitPrice, imageUrl: imageUrl, qty: qty))
        }
        notifyChanged()
    }

    func setQty(productId: Int, qty: Int) {
        guard qty >= 1 else { remove(productId: productId); return }
        guard let i = items.firstIndex(where: { $0.productId == productId }) else { return }
        items[i].qty = qty
        notifyChanged()
    }

    func increase(productId: Int) {
        guard let i = items.firstIndex(where: { $0.productId == productId }) else { return }
        items[i].qty += 1
        notifyChanged()
    }

    func decrease(productId: Int) {
        guard let i = items.firstIndex(where: { $0.productId == productId }) else { return }
        items[i].qty -= 1
        if items[i].qty <= 0 { items.remove(at: i) }
        notifyChanged()
    }

    func remove(productId: Int) {
        items.removeAll { $0.productId == productId }
        notifyChanged()
    }

    func clear() {
        items.removeAll()
        notifyChanged()
    }

    var totalAmount: Double {
        items.reduce(0) { $0 + (Double($1.qty) * $1.unitPrice) }
    }

    var totalCount: Int {
        items.reduce(0) { $0 + $1.qty }
    }

    private func notifyChanged() {
        NotificationCenter.default.post(name: CartManager.changedNotification, object: nil)
    }
}
