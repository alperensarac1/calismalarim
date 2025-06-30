import 'urun.dart'; // Urun sınıfının tanımlı olduğu dosyayı import etmeyi unutma

// UrunCevap
class UrunCevap {
  final List<Urun> kahveUrun;

  UrunCevap({required this.kahveUrun});

  factory UrunCevap.fromJson(Map<String, dynamic> json) {
    return UrunCevap(
      kahveUrun: (json['kahve_urun'] as List)
          .map((e) => Urun.fromJson(e))
          .toList(),
    );
  }
}

