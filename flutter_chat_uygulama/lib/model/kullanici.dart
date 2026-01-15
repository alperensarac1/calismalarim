class Kullanici {
  final int id;
  final String ad;
  final String numara;

  Kullanici({
    required this.id,
    required this.ad,
    required this.numara,
  });

  factory Kullanici.fromJson(Map<String, dynamic> json) {
    return Kullanici(
      id: int.parse(json['id'].toString()),
      ad: json['ad'],
      numara: json['numara'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'ad': ad,
      'numara': numara,
    };
  }
}

class KullaniciListResponse {
  final bool success;
  final List<Kullanici> kullanicilar;

  KullaniciListResponse({
    required this.success,
    required this.kullanicilar,
  });

  factory KullaniciListResponse.fromJson(Map<String, dynamic> json) {
    return KullaniciListResponse(
      success: json['success'],
      kullanicilar: (json['kullanicilar'] as List)
          .map((e) => Kullanici.fromJson(e))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'kullanicilar': kullanicilar.map((e) => e.toJson()).toList(),
    };
  }
}
