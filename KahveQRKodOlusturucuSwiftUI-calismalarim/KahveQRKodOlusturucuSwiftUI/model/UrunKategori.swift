import Foundation

enum UrunKategori: Int {
    case INDIRIMLI = 0
    case ATISTIRMALIKLAR = 1
    case ICECEKLER = 2

    func kategoriKodu() -> Int {
        return self.rawValue
    }
}

