class MasaUrun {
  final int urunId;
  final String urunAd;
  final double birimFiyat;
  final int adet;
  final double toplamFiyat;

  MasaUrun({
    required this.urunId,
    required this.urunAd,
    required this.birimFiyat,
    required this.adet,
    required this.toplamFiyat,
  });

  factory MasaUrun.fromJson(Map<String, dynamic> json) => MasaUrun(
    urunId: json['urun_id'],
    urunAd: json['urun_ad'],
    birimFiyat: (json['birim_fiyat'] as num).toDouble(),
    adet: json['adet'],
    toplamFiyat: (json['toplam_fiyat'] as num).toDouble(),
  );

  Map<String, dynamic> toJson() => {
    'urun_id': urunId,
    'urun_ad': urunAd,
    'birim_fiyat': birimFiyat,
    'adet': adet,
    'toplam_fiyat': toplamFiyat,
  };
}
