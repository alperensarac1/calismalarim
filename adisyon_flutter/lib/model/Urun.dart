import 'Kategori.dart';

class Urun {
  final int id;
  String urunAd;
  double urunFiyat;
  String urunResim;
  int urunAdet;
  Kategori urunKategori;

  Urun({
    required this.id,
    required this.urunAd,
    required this.urunFiyat,
    required this.urunResim,
    required this.urunAdet,
    required this.urunKategori,
  });

  factory Urun.fromJson(Map<String, dynamic> json) => Urun(
    id: json['id'],
    urunAd: json['urun_ad'],
    urunFiyat: (json['urun_fiyat'] as num).toDouble(),
    urunResim: json['urun_resim'],
    urunAdet: json['urun_adet'],
    urunKategori: Kategori.fromJson(json['urunKategori']),
  );

  Map<String, dynamic> toJson() => {
    'id': id,
    'urun_ad': urunAd,
    'urun_fiyat': urunFiyat,
    'urun_resim': urunResim,
    'urun_adet': urunAdet,
    'urunKategori': urunKategori.toJson(),
  };
}
