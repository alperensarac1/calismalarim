// haber_model.dart
class HaberModel {
  final int id;
  final String baslik;
  final String icerik;
  final String media_type;
  final String media_url;
  final String yayinlanma_tarihi;
  final int sondakika;
  final int yazar_id;
  final int tur_id;
  final String ad;
  final String soyad;
  final String unvan;
  final String tur_adi;

  const HaberModel({
    required this.id,
    required this.baslik,
    required this.icerik,
    required this.media_type,
    required this.media_url,
    required this.yayinlanma_tarihi,
    required this.sondakika,
    required this.yazar_id,
    required this.tur_id,
    required this.ad,
    required this.soyad,
    required this.unvan,
    required this.tur_adi,
  });

  factory HaberModel.fromJson(Map<String, dynamic> json) {
    return HaberModel(
      id: (json['id'] ?? 0) as int,
      baslik: (json['baslik'] ?? '') as String,
      icerik: (json['icerik'] ?? '') as String,
      media_type: (json['media_type'] ?? '') as String,
      media_url: (json['media_url'] ?? '') as String,
      yayinlanma_tarihi: (json['yayinlanma_tarihi'] ?? '') as String,
      sondakika: (json['sondakika'] ?? 0) as int,
      yazar_id: (json['yazar_id'] ?? 0) as int,
      tur_id: (json['tur_id'] ?? 0) as int,
      ad: (json['ad'] ?? '') as String,
      soyad: (json['soyad'] ?? '') as String,
      unvan: (json['unvan'] ?? '') as String,
      tur_adi: (json['tur_adi'] ?? '') as String,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'baslik': baslik,
    'icerik': icerik,
    'media_type': media_type,
    'media_url': media_url,
    'yayinlanma_tarihi': yayinlanma_tarihi,
    'sondakika': sondakika,
    'yazar_id': yazar_id,
    'tur_id': tur_id,
    'ad': ad,
    'soyad': soyad,
    'unvan': unvan,
    'tur_adi': tur_adi,
  };
}
