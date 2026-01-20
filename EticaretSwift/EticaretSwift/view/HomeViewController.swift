import UIKit

final class HomeViewController: UIViewController {


    @IBOutlet private weak var cartButton: UIButton!

    @IBOutlet private weak var searchField: UITextField!
    @IBOutlet private weak var sortButton: UIButton!
    @IBOutlet private weak var discountButton: UIButton!

    @IBOutlet private weak var categoriesCollectionView: UICollectionView!
    @IBOutlet private weak var productsCollectionView: UICollectionView!
    @IBOutlet private weak var loadingIndicator: UIActivityIndicatorView!

        private var selectedCategoryId: Int? = nil
        private var query: String? = nil
        private var sort: String = "newest"
        private var onlyDiscounted: Bool = false

        private var page: Int = 1
        private var total: Int = 0
        private var isLoading: Bool = false
        private var isLoadingNext: Bool = false

        private var categories: [CategoryUI] = []
        private var products: [ProductUI] = []

        private let refreshControl = UIRefreshControl()

    init() {
          super.init(nibName: "HomeViewController", bundle: nil)
      }
      required init?(coder: NSCoder) { fatalError("XIB kullanıyoruz") }

        override func viewDidLoad() {
            super.viewDidLoad()
            if !AuthManager.shared.isLoggedIn {
                   let vc = LoginViewController()
                   navigationController?.pushViewController(vc, animated: true)
               }
            setupUI()
            setupCollections()
            setupSortMenu()

            loadCategories()
            loadProducts(page: 1)
            
        }

        private func setupUI() {
            view.backgroundColor = .systemBackground

            title = "Anasayfa"
           

            searchField.delegate = self
            searchField.returnKeyType = .search
            searchField.clearButtonMode = .whileEditing

            updateDiscountChip()
            setLoading(false)
        }

        private func setupCollections() {
            // Categories (horizontal)
            categoriesCollectionView.dataSource = self
            categoriesCollectionView.delegate = self

            let catLayout = UICollectionViewFlowLayout()
            catLayout.scrollDirection = .horizontal
            catLayout.minimumLineSpacing = 8
            catLayout.minimumInteritemSpacing = 8
            categoriesCollectionView.collectionViewLayout = catLayout
            categoriesCollectionView.showsHorizontalScrollIndicator = false

            categoriesCollectionView.register(UINib(nibName: "CategoryCell", bundle: nil),
                                              forCellWithReuseIdentifier: "CategoryCell")

            // Products (grid)
            productsCollectionView.dataSource = self
            productsCollectionView.delegate = self

            let prodLayout = UICollectionViewFlowLayout()
            prodLayout.scrollDirection = .vertical
            prodLayout.minimumLineSpacing = 10
            prodLayout.minimumInteritemSpacing = 10
            productsCollectionView.collectionViewLayout = prodLayout
            productsCollectionView.showsVerticalScrollIndicator = true

            productsCollectionView.register(UINib(nibName: "ProductCell", bundle: nil),
                                            forCellWithReuseIdentifier: "ProductCell")

            // Pull-to-refresh (Android swipeRefresh)
            productsCollectionView.refreshControl = refreshControl
            refreshControl.addTarget(self, action: #selector(onRefresh), for: .valueChanged)
        }

        private func setupSortMenu() {
            // Android: newest, price_asc, price_desc
            let options: [(key: String, title: String)] = [
                ("newest", "Yeni"),
                ("price_asc", "Fiyat ↑"),
                ("price_desc", "Fiyat ↓")
            ]

            func makeMenu() -> UIMenu {
                let actions = options.map { opt in
                    UIAction(title: opt.title, state: (sort == opt.key ? .on : .off)) { [weak self] _ in
                        guard let self else { return }
                        self.sort = opt.key
                        self.applyFiltersReload()
                        self.setupSortMenu() // menü state güncellensin
                    }
                }
                return UIMenu(title: "Sırala", children: actions)
            }

            sortButton.menu = makeMenu()
            sortButton.showsMenuAsPrimaryAction = true
        }

        private func setLoading(_ loading: Bool) {
            isLoading = loading
            if loading {
                loadingIndicator.startAnimating()
                loadingIndicator.isHidden = false
            } else {
                loadingIndicator.stopAnimating()
                loadingIndicator.isHidden = true
            }
        }

        private func updateDiscountChip() {
            discountButton.isSelected = onlyDiscounted

            if #available(iOS 15.0, *) {
                var cfg = UIButton.Configuration.tinted()
                cfg.title = "Sadece indirimliler"
                cfg.cornerStyle = .capsule
                cfg.baseBackgroundColor = onlyDiscounted ? .systemGreen : .systemGray5
                cfg.baseForegroundColor = onlyDiscounted ? .white : .label
                discountButton.configuration = cfg
            } else {
                discountButton.setTitle("Sadece indirimliler", for: .normal)
                discountButton.backgroundColor = onlyDiscounted ? .systemGreen : .systemGray5
            }
        }
   
    @IBAction private func cartTapped(_ sender: UIButton) {
        let vc = CartDetailScreen()
        navigationController?.pushViewController(vc, animated: true)
    }

    @IBAction private func discountTapped(_ sender: UIButton) {
        onlyDiscounted.toggle()
        updateDiscountChip()
        applyFiltersReload()
    }

    @objc private func onRefresh() {
        loadProducts(page: 1)
    }

    private func applyFiltersReload() {
        loadProducts(page: 1)
    }


    private func loadCategories() {
        Task {
            do {
                let dtos = try await ApiClient.shared.getCategories()

                let mapped: [CategoryUI] =
                    [CategoryUI(id: nil, name: "Tümü")] +
                    dtos.map {
                        CategoryUI(id: $0.id, name: $0.name)
                    }

                await MainActor.run {
                    self.categories = mapped
                    self.categoriesCollectionView.reloadData()
                }
            } catch {
                await MainActor.run {
                    self.showError(error.localizedDescription)
                }
            }
        }
    }


    private func showError(_ msg: String) {
        let a = UIAlertController(title: "Hata", message: msg, preferredStyle: .alert)
        a.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(a, animated: true)
    }
    private func loadProducts(page: Int) {
        if isLoading || isLoadingNext { return }

        if page == 1 {
            setLoading(true)
        } else {
            isLoadingNext = true
        }

        let per = 10
        let discountParam: Int? = onlyDiscounted ? 1 : nil
        let catParam: Int? = selectedCategoryId
        let qParam: String? = query

        Task {
            do {
                let res = try await ApiClient.shared.getProducts(
                    cat: catParam,
                    q: qParam,
                    discount: discountParam,
                    sort: sort,
                    page: page,
                    per: per
                )

                let mapped: [ProductUI] = res.items.map { p in
                    let finalPrice: Double
                    if let disc = p.discount_percent, disc > 0 {
                        finalPrice = p.price * (1.0 - (disc / 100.0))
                    } else {
                        finalPrice = p.price
                    }

                    return ProductUI(
                        id: p.id,
                        title: p.name,
                        priceText: String(format: "₺ %.2f", finalPrice),
                        imageUrl: p.image_url
                    )
                }

                await MainActor.run {
                    if page == 1 { self.products.removeAll() }
                    self.products.append(contentsOf: mapped)

                    self.total = res.total
                    self.page = res.page

                    self.setLoading(false)
                    self.isLoadingNext = false
                    self.refreshControl.endRefreshing()
                    self.productsCollectionView.reloadData()
                }
            } catch {
                await MainActor.run {
                    self.setLoading(false)
                    self.isLoadingNext = false
                    self.refreshControl.endRefreshing()
                    self.showError(error.localizedDescription)
                }
            }
        }
        func applyFiltersReload() {
            loadProducts(page: 1)
        }

    }


    private func addToCart(productId: Int) {
        // TODO: API/DB -> sepete ekle
        let alert = UIAlertController(title: "Sepet", message: "Ürün \(productId) sepete eklendi", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Tamam", style: .default))
        present(alert, animated: true)
    }
}

// MARK: - UITextFieldDelegate
extension HomeViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        let q = (textField.text ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        query = q.isEmpty ? nil : q
        textField.resignFirstResponder()
        applyFiltersReload()
        return true
    }
}

// MARK: - UICollectionView
extension HomeViewController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        collectionView == categoriesCollectionView ? categories.count : products.count
    }
    private func parsePrice(_ text: String) -> Double {
        let cleaned = text
            .replacingOccurrences(of: "₺", with: "")
            .replacingOccurrences(of: " ", with: "")
            .replacingOccurrences(of: ",", with: ".")
        return Double(cleaned) ?? 0
    }


    func collectionView(_ collectionView: UICollectionView,
                        cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {

        if collectionView == categoriesCollectionView {
            let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "CategoryCell", for: indexPath) as! CategoryCell
            let item = categories[indexPath.item]
            let selected = (item.id == selectedCategoryId) || (item.id == nil && selectedCategoryId == nil)
            cell.configure(title: item.name, selected: selected)
            return cell
        } else {
            let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "ProductCell", for: indexPath) as! ProductCell
            let p = products[indexPath.item]
            cell.configure(with: p)

            cell.onAddToCart = { [weak self] in
                guard let self else { return }

                if !AuthManager.shared.isLoggedIn {
                    let vc = LoginViewController()
                    self.navigationController?.pushViewController(vc, animated: true)
                    return
                }

                Task {
                    do {
                        _ = try await ApiClient.shared.addToCart(productId: p.id, quantity: 1)

                        await MainActor.run {
                            let a = UIAlertController(title: "Sepete eklendi ✅",
                                                      message: p.title,
                                                      preferredStyle: .alert)
                            a.addAction(UIAlertAction(title: "Tamam", style: .default))
                            self.present(a, animated: true)
                        }
                    } catch {
                        await MainActor.run {
                            self.showError(error.localizedDescription)
                        }
                    }
                }
            }

            return cell
        }
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if collectionView == categoriesCollectionView {
            selectedCategoryId = categories[indexPath.item].id
            categoriesCollectionView.reloadData()
            applyFiltersReload()
        } else {
            let product = products[indexPath.item]
            let vc = ProductDetailViewController(productId: product.id)
            navigationController?.pushViewController(vc, animated: true)
        }
    }

    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize {

        if collectionView == categoriesCollectionView {
            return CGSize(width: 110, height: 40)
        } else {
            let w = collectionView.bounds.width
            let spacing: CGFloat = 10
            let cols: CGFloat = 2
            let totalSpacing = (cols - 1) * spacing
            let cellW = floor((w - totalSpacing) / cols)
            return CGSize(width: cellW, height: 230)
        }
    }

    // Infinite scroll
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        guard scrollView == productsCollectionView else { return }

        let offsetY = scrollView.contentOffset.y
        let contentH = scrollView.contentSize.height
        let viewH = scrollView.bounds.height

        if offsetY > contentH - viewH - 400 {
            let hasMore = products.count < total
            if hasMore && !isLoadingNext && !isLoading {
                loadProducts(page: page + 1)
            }
        }
    }
}

// MARK: - UI Models
struct CategoryUI {
    let id: Int?
    let name: String
}
