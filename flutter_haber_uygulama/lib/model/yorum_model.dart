// yorum_model.dart
class YorumModel {
  final int id;
  final int haber_id;
  final String takma_ad;
  final String yorum_metni;
  final int onayli;
  final String yorum_tarihi;

  const YorumModel({
    required this.id,
    required this.haber_id,
    required this.takma_ad,
    required this.yorum_metni,
    required this.onayli,
    required this.yorum_tarihi,
  });

  factory YorumModel.fromJson(Map<String, dynamic> json) {
    return YorumModel(
      id: (json['id'] ?? 0) as int,
      haber_id: (json['haber_id'] ?? 0) as int,
      takma_ad: (json['takma_ad'] ?? '') as String,
      yorum_metni: (json['yorum_metni'] ?? '') as String,
      onayli: (json['onayli'] ?? 0) as int,
      yorum_tarihi: (json['yorum_tarihi'] ?? '') as String,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'haber_id': haber_id,
    'takma_ad': takma_ad,
    'yorum_metni': yorum_metni,
    'onayli': onayli,
    'yorum_tarihi': yorum_tarihi,
  };
}
