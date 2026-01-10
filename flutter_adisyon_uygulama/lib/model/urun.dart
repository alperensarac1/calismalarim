import 'kategori.dart';

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

  factory Urun.fromJson(Map<String, dynamic> json) {
    return Urun(
      id: (json['id'] ?? 0) as int,
      urunAd: (json['urun_ad'] ?? '') as String,
      urunFiyat: ((json['urun_fiyat'] ?? 0) as num).toDouble(),
      urunResim: (json['urun_resim'] ?? '') as String,
      urunAdet: (json['urun_adet'] ?? 0) as int,
      urunKategori: Kategori.fromJson(
        (json['urunKategori'] ?? const <String, dynamic>{}) as Map<String, dynamic>,
      ),
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'urun_ad': urunAd,
    'urun_fiyat': urunFiyat,
    'urun_resim': urunResim,
    'urun_adet': urunAdet,
    'urunKategori': urunKategori.toJson(),
  };
}