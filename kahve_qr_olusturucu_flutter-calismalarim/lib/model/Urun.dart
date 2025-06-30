
class Urun {
  final int id;
  final String urunAd;
  final String urunResim;
  final String urunAciklama;
  final int urunKategoriId;
  final int urunIndirim;
  final double urunFiyat;
  final double urunIndirimliFiyat;

  Urun({
    required this.id,
    required this.urunAd,
    required this.urunResim,
    required this.urunAciklama,
    required this.urunKategoriId,
    required this.urunIndirim,
    required this.urunFiyat,
    required this.urunIndirimliFiyat,
  });

  factory Urun.fromJson(Map<String, dynamic> json) {
    return Urun(
      id: int.parse(json['id'].toString()),
      urunAd: json['urun_ad'],
      urunResim: json['urun_resim'],
      urunAciklama: json['urun_aciklama'],
      urunKategoriId: int.parse(json['urun_kategori_id'].toString()),
      urunIndirim: int.parse(json['urun_indirim'].toString()),
      urunFiyat: double.parse(json['urun_fiyat'].toString()),
      urunIndirimliFiyat: double.parse(json['urun_indirimli_fiyat'].toString()),
    );
  }
}
