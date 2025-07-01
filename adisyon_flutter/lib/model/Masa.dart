class Masa {
  final int id;
  final String masaAdi;
  final int acikMi;
  final int sure;
  final double toplamFiyat;

  Masa({
    required this.id,
    required this.masaAdi,
    required this.acikMi,
    required this.sure,
    required this.toplamFiyat,
  });

  factory Masa.fromJson(Map<String, dynamic> json) {
    return Masa(
      id: int.parse(json['id'].toString()), // 👈 string olabilir
      masaAdi: json['masa_adi'] ?? 'Masa',
      acikMi: int.parse(json['acik_mi'].toString()), // 👈
      sure: int.tryParse(json['sure'].toString()) ?? 0, // 👈 optional
      toplamFiyat: double.tryParse(json['toplam_fiyat'].toString()) ?? 0.0, // 👈
    );
  }
}
