class MasaUrun {
  final int urunId;
  final String urunAd;
  final double birimFiyat;
  final int adet;
  final double toplamFiyat;

  const MasaUrun({
    required this.urunId,
    required this.urunAd,
    required this.birimFiyat,
    required this.adet,
    required this.toplamFiyat,
  });

  factory MasaUrun.fromJson(Map<String, dynamic> json) {
    return MasaUrun(
      urunId: (json['urun_id'] ?? 0) as int,
      urunAd: (json['urun_ad'] ?? '') as String,
      birimFiyat: ((json['birim_fiyat'] ?? 0) as num).toDouble(),
      adet: (json['adet'] ?? 0) as int,
      toplamFiyat: ((json['toplam_fiyat'] ?? 0) as num).toDouble(),
    );
  }

  Map<String, dynamic> toJson() => {
    'urun_id': urunId,
    'urun_ad': urunAd,
    'birim_fiyat': birimFiyat,
    'adet': adet,
    'toplam_fiyat': toplamFiyat,
  };
}