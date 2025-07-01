import UIKit
import Foundation

final class MainLayout: UIView {
    private var masaListesi: [Masa]
    private let viewModel: MasaDetayViewModel
    private let onMasaClick: (Masa) -> Void
    
    private let ozetView: MasaOzetLayout
    private let masalarView: MasalarLayout
    
    // Dışarıya erişilebilsin diye public yapıyoruz
    public var collectionView: UICollectionView { masalarView.collectionView }
    
    init(
        masaListesi: [Masa],
        viewModel: MasaDetayViewModel,
        onMasaClick: @escaping (Masa) -> Void
    ) {
        self.masaListesi = masaListesi
        self.viewModel = viewModel
        self.onMasaClick = onMasaClick
        
        self.ozetView = MasaOzetLayout(
            masaListesi: masaListesi,
            viewModel: viewModel,
            onMasaDetayTikla: onMasaClick
        )
        self.masalarView = MasalarLayout(
            masaList: masaListesi,
            onMasaClick: onMasaClick
        )
        
        super.init(frame: .zero)
        setupView()
        setupConstraints()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) kullanılmadı")
    }
    
    private func setupView() {
        backgroundColor = UIColor(red: 1.0, green: 186/255, blue: 186/255, alpha: 1.0)
        layoutMargins = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
        preservesSuperviewLayoutMargins = true
        
        [ozetView, masalarView.collectionView].forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
            addSubview($0)
        }
    }
    
    private func setupConstraints() {
        NSLayoutConstraint.activate([
            ozetView.topAnchor.constraint(equalTo: layoutMarginsGuide.topAnchor),
            ozetView.leadingAnchor.constraint(equalTo: layoutMarginsGuide.leadingAnchor),
            ozetView.bottomAnchor.constraint(equalTo: layoutMarginsGuide.bottomAnchor),
            ozetView.widthAnchor.constraint(equalTo: widthAnchor, multiplier: 1.0/3.0),
            
            masalarView.collectionView.topAnchor.constraint(equalTo: layoutMarginsGuide.topAnchor),
            masalarView.collectionView.trailingAnchor.constraint(equalTo: layoutMarginsGuide.trailingAnchor),
            masalarView.collectionView.bottomAnchor.constraint(equalTo: layoutMarginsGuide.bottomAnchor),
            masalarView.collectionView.leadingAnchor.constraint(equalTo: ozetView.trailingAnchor, constant: 10)
        ])
    }
    
   
    func updateMasaList(_ yeniListe: [Masa]) {
        
        self.masaListesi = yeniListe
        
        
        ozetView.update(masaListesi: yeniListe)
        masalarView.update(masaList: yeniListe)
    }
    
    func updateCategories(_ kategoriler: [Kategori]) {
        viewModel.kategoriler = kategoriler
    }
}
