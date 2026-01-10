// haber_turu_model.dart
class HaberTuruModel {
  final int id;
  final String tur_adi;

  const HaberTuruModel({
    required this.id,
    required this.tur_adi,
  });

  factory HaberTuruModel.fromJson(Map<String, dynamic> json) {
    return HaberTuruModel(
      id: (json['id'] ?? 0) as int,
      tur_adi: (json['tur_adi'] ?? '') as String,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'tur_adi': tur_adi,
  };

  @override
  String toString() => tur_adi;
}
