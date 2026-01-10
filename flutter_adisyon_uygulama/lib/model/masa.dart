import 'package:flutter_adisyon_uygulama/model/urun.dart';

class Masa {
  final int id;
  final String masaAdi;

  /// Kotlin: @SerializedName("acik_mi") val acikMi: Int
  final int acikMi;

  final String sure;
  final double toplamFiyat;

  /// Kotlin: @Transient var urunler = mutableListOf<Urun>()
  /// JSON'a yazmıyoruz.
  final List<Urun> urunler;

  Masa({
    required this.id,
    required this.masaAdi,
    required this.acikMi,
    required this.sure,
    required this.toplamFiyat,
    List<Urun>? urunler,
  }) : urunler = urunler ?? <Urun>[];

  factory Masa.fromJson(Map<String, dynamic> json) {
    return Masa(
      id: (json['id'] ?? 0) as int,
      masaAdi: (json['masa_adi'] ?? '') as String,
      acikMi: (json['acik_mi'] ?? 0) as int,
      sure: (json['sure'] ?? '') as String,
      toplamFiyat: ((json['toplam_fiyat'] ?? 0) as num).toDouble(),

      // Eğer backend bazen urunler gönderiyorsa al, yoksa boş bırak.
      urunler: (json['urunler'] is List)
          ? (json['urunler'] as List)
          .whereType<Map<String, dynamic>>()
          .map(Urun.fromJson)
          .toList()
          : <Urun>[],
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'masa_adi': masaAdi,
    'acik_mi': acikMi,
    'sure': sure,
    'toplam_fiyat': toplamFiyat,

    // urunler özellikle yazılmıyor (Transient mantığı)
  };

}