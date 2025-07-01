import UIKit
import Combine

final class MasaAdisyonLayout: UIView {

    
    let masa: Masa
    var viewModel: MasaDetayViewModel
    var subscriptions = Set<AnyCancellable>()

    var urunListesi: [MasaUrun] = []

   
    private lazy var urunTableView: UITableView = {
        let tv = UITableView()
        tv.register(UrunSatirCell.self, forCellReuseIdentifier: UrunSatirCell.reuseIdentifier)
        tv.dataSource = self
        tv.translatesAutoresizingMaskIntoConstraints = false
        return tv
    }()

    lazy var toplamFiyatLabel: UILabel = {
        let lbl = UILabel()
        lbl.font = .systemFont(ofSize: 20)
        lbl.textColor = .gray
        lbl.textAlignment = .left
        lbl.translatesAutoresizingMaskIntoConstraints = false
        return lbl
    }()

    private lazy var odemeButton: UIButton = {
        let btn = UIButton(type: .system)
        btn.setTitle("Ödeme Al", for: .normal)
        btn.backgroundColor = .systemBlue
        btn.setTitleColor(.white, for: .normal)
        btn.layer.cornerRadius = 8
        btn.translatesAutoresizingMaskIntoConstraints = false
        btn.addTarget(self, action: #selector(odemeAlTapped), for: .touchUpInside)
        return btn
    }()

    
    init(masa: Masa, viewModel: MasaDetayViewModel) {
        self.masa = masa
        self.viewModel = viewModel
        super.init(frame: .zero)
        translatesAutoresizingMaskIntoConstraints = false
        setupView()
        setupConstraints()
        bindViewModel()
        viewModel.yukleTumVeriler()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) kullanılmadı")
    }

    private func setupView() {
        let stack = UIStackView(arrangedSubviews: [urunTableView, toplamFiyatLabel, odemeButton])
        stack.axis = .vertical
        stack.spacing = 16
        stack.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stack)

        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: topAnchor, constant: 16),
            stack.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 16),
            stack.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -16),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor, constant: -16),

            odemeButton.heightAnchor.constraint(equalToConstant: 44)
        ])
    }

    private func setupConstraints() {
        urunTableView.heightAnchor.constraint(greaterThanOrEqualToConstant: 200).isActive = true
    }

    private func bindViewModel() {
        viewModel.$urunler
            .receive(on: DispatchQueue.main)
            .sink { [weak self] liste in
                guard let self = self else { return }
                self.urunListesi = liste
                self.urunTableView.reloadData()
                let toplam = liste.reduce(0) { $0 + $1.toplamFiyat }
                self.toplamFiyatLabel.text = String(format: "%.2f TL", toplam)
            }
            .store(in: &subscriptions)
    }
    @objc private func odemeAlTapped() {
        odemeButton.isEnabled = false
        viewModel.odemeAl { [weak self] in
            DispatchQueue.main.async {
                let alert = UIAlertController(title: nil, message: "Ödeme alındı", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "Tamam", style: .default))
                self?.parentViewController?.present(alert, animated: true)
                self?.viewModel.yukleTumVeriler()
                self?.odemeButton.isEnabled = true
            }
        }
    }

    private var parentViewController: UIViewController? {
        var responder: UIResponder? = self
        while let r = responder {
            if let vc = r as? UIViewController { return vc }
            responder = r.next
        }
        return nil
    }
}


extension MasaAdisyonLayout: UITableViewDataSource {
    func tableView(_ tv: UITableView, numberOfRowsInSection section: Int) -> Int {
        return urunListesi.count
    }

    func tableView(_ tv: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let urun = urunListesi[indexPath.row]
        let cell = tv.dequeueReusableCell(withIdentifier: UrunSatirCell.reuseIdentifier, for: indexPath) as! UrunSatirCell
        cell.configure(with: urun)
        return cell
    }
}


private final class UrunSatirCell: UITableViewCell {
    static let reuseIdentifier = "UrunSatirCell"

    private let titleLabel = UILabel()

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        titleLabel.font = .systemFont(ofSize: 16)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(titleLabel)
        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 8),
            titleLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 16),
            titleLabel.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -16),
            titleLabel.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -8)
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) kullanılmadı")
    }

    func configure(with urun: MasaUrun) {
        titleLabel.text = "\(urun.urunAd) (\(urun.adet))"
    }
}
