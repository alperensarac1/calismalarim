import Foundation

struct Urun: Codable,Identifiable {
    let id: String
      let urun_ad: String
      let urun_resim: String
      let urun_aciklama: String
      let urun_kategori_id: String
      let urun_indirim: String
      let urun_fiyat: String
      let urun_indirimli_fiyat: String
}
