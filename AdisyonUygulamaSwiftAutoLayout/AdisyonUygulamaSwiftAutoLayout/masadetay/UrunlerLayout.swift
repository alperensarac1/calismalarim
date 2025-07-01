import UIKit

final class UrunlerLayout: UIView {

    // MARK: - Data
    private var tumUrunListesi: [Urun] = []
    private var guncelUrunListesi: [Urun] = []
    private var kategoriListesi: [Kategori] = []

    // MARK: - Alt Görünümler

    private lazy var kategoriTableView: UITableView = {
        let tv = UITableView()
        tv.register(UITableViewCell.self, forCellReuseIdentifier: "KategoriCell")
        tv.dataSource = self
        tv.delegate = self
        tv.translatesAutoresizingMaskIntoConstraints = false
        return tv
    }()

    private lazy var productCollectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        let spacing: CGFloat = 8
        layout.minimumInteritemSpacing = spacing
        layout.minimumLineSpacing = spacing
        let cv = UICollectionView(frame: .zero, collectionViewLayout: layout)
        cv.register(UrunCollectionViewCell.self, forCellWithReuseIdentifier: UrunCollectionViewCell.reuseIdentifier)
        cv.dataSource = self
        cv.delegate = self
        cv.backgroundColor = .white
        cv.translatesAutoresizingMaskIntoConstraints = false
        return cv
    }()

    private lazy var bosMesajLabel: UILabel = {
        let lbl = UILabel()
        lbl.text = "Seçilen kategoriye ait ürün bulunamadı."
        lbl.font = .systemFont(ofSize: 18)
        lbl.textColor = .gray
        lbl.textAlignment = .center
        lbl.numberOfLines = 0
        lbl.isHidden = true
        lbl.translatesAutoresizingMaskIntoConstraints = false
        return lbl
    }()

    // MARK: - Init

    init(viewModel: MasaDetayViewModel, urunVM: UrunViewModel) {
        super.init(frame: .zero)
        translatesAutoresizingMaskIntoConstraints = false
        setupViews()
        setupConstraints()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) kullanılmadı")
    }

    // MARK: - Setup

    private func setupViews() {
        // yatay layout
        let container = UIStackView(arrangedSubviews: [kategoriTableView, UIView()])
        container.axis = .horizontal
        container.spacing = 8
        container.translatesAutoresizingMaskIntoConstraints = false

        addSubview(container)
        // sağdaki placeholder view
        let rightContainer = container.arrangedSubviews[1]
        rightContainer.addSubview(productCollectionView)
        rightContainer.addSubview(bosMesajLabel)

        NSLayoutConstraint.activate([
            // stack tüm view'u kaplasın
            container.topAnchor.constraint(equalTo: topAnchor),
            container.leadingAnchor.constraint(equalTo: leadingAnchor),
            container.trailingAnchor.constraint(equalTo: trailingAnchor),
            container.bottomAnchor.constraint(equalTo: bottomAnchor),

            // kategoriTableView genişliği sabit 150pt
            kategoriTableView.widthAnchor.constraint(equalToConstant: 150),

            // productCollectionView pin to rightContainer
            productCollectionView.topAnchor.constraint(equalTo: rightContainer.topAnchor),
            productCollectionView.leadingAnchor.constraint(equalTo: rightContainer.leadingAnchor),
            productCollectionView.trailingAnchor.constraint(equalTo: rightContainer.trailingAnchor),
            productCollectionView.bottomAnchor.constraint(equalTo: rightContainer.bottomAnchor),

            // bosMesajLabel ortala ve sabitle
            bosMesajLabel.centerXAnchor.constraint(equalTo: rightContainer.centerXAnchor),
            bosMesajLabel.centerYAnchor.constraint(equalTo: rightContainer.centerYAnchor),
            bosMesajLabel.leadingAnchor.constraint(greaterThanOrEqualTo: rightContainer.leadingAnchor, constant: 16),
            bosMesajLabel.trailingAnchor.constraint(lessThanOrEqualTo: rightContainer.trailingAnchor, constant: -16),
        ])
    }

    private func setupConstraints() {
        // Hepsi UIStackView üzerinden yapıldı
    }

    // MARK: - Public API

    func setUrunListesi(_ yeniListe: [Urun]) {
        tumUrunListesi = yeniListe
        guncelUrunListesi = yeniListe
        productCollectionView.reloadData()
        bosMesajLabel.isHidden = !guncelUrunListesi.isEmpty
    }

    func setKategoriListesi(_ yeniKategoriler: [Kategori]) {
        kategoriListesi = yeniKategoriler
        // "Tümü" ekle
        kategoriTableView.reloadData()
    }
}

// MARK: - UITableViewDataSource & Delegate

extension UrunlerLayout: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tv: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1 + kategoriListesi.count // "Tümü" + her kategori
    }

    func tableView(_ tv: UITableView,
                   cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tv.dequeueReusableCell(withIdentifier: "KategoriCell", for: indexPath)
        if indexPath.row == 0 {
            cell.textLabel?.text = "Tümü"
        } else {
            cell.textLabel?.text = kategoriListesi[indexPath.row - 1].kategori_ad
        }
        return cell
    }

    func tableView(_ tv: UITableView, didSelectRowAt indexPath: IndexPath) {
        let filtreli: [Urun]
        if indexPath.row == 0 {
            filtreli = tumUrunListesi
        } else {
            let secKat = kategoriListesi[indexPath.row - 1]
            filtreli = tumUrunListesi.filter { $0.urunKategori.id == secKat.id }
        }
        guncelUrunListesi = filtreli
        productCollectionView.reloadData()
        bosMesajLabel.isHidden = !filtreli.isEmpty
    }
}

// MARK: - UICollectionViewDataSource & DelegateFlowLayout

extension UrunlerLayout: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ cv: UICollectionView,
                        numberOfItemsInSection section: Int) -> Int {
        return guncelUrunListesi.count
    }

    func collectionView(_ cv: UICollectionView,
                        cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = cv.dequeueReusableCell(
            withReuseIdentifier: UrunCollectionViewCell.reuseIdentifier,
            for: indexPath
        ) as! UrunCollectionViewCell
        cell.configure(with: guncelUrunListesi[indexPath.item])
        return cell
    }

    func collectionView(_ cv: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize {
        let totalSpacing: CGFloat = 8 * 2 + 8 * 2 // inset + inter-item
        let width = (cv.bounds.width - totalSpacing) / 3
        return CGSize(width: width, height: width * 1.2)
    }
}

// MARK: - UrunCollectionViewCell

final class UrunCollectionViewCell: UICollectionViewCell {
    static let reuseIdentifier = "UrunCell"

    private let titleLabel = UILabel()
    private let priceLabel = UILabel()
    private let imageView = UIImageView()

    override init(frame: CGRect) {
        super.init(frame: frame)
        contentView.backgroundColor = .white
        contentView.layer.cornerRadius = 8
        contentView.layer.borderWidth = 1
        contentView.layer.borderColor = UIColor.lightGray.cgColor
        contentView.clipsToBounds = true

        let stack = UIStackView(arrangedSubviews: [imageView, titleLabel, priceLabel])
        stack.axis = .vertical
        stack.spacing = 4
        stack.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(stack)

        titleLabel.font = .systemFont(ofSize: 14)
        titleLabel.numberOfLines = 2
        priceLabel.font = .systemFont(ofSize: 12, weight: .medium)

        imageView.contentMode = .scaleAspectFit

        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 8),
            stack.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 8),
            stack.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -8),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: contentView.bottomAnchor, constant: -8),
            imageView.heightAnchor.constraint(equalTo: contentView.heightAnchor, multiplier: 0.5)
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) kullanılmadı")
    }

    func configure(with urun: Urun) {
        titleLabel.text = urun.urunAd
        priceLabel.text = urun.urunFiyat.fiyatYaz()
    }
}
