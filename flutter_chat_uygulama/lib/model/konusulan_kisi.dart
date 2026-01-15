class KonusulanKisi {
  final int id;
  final String ad;
  final String numara;
  final String sonMesaj;
  final String tarih;

  KonusulanKisi({
    required this.id,
    required this.ad,
    required this.numara,
    required this.sonMesaj,
    required this.tarih,
  });

  factory KonusulanKisi.fromJson(Map<String, dynamic> json) {
    return KonusulanKisi(
      id: int.parse(json['id'].toString()),
      ad: json['ad'],
      numara: json['numara'],
      sonMesaj: json['son_mesaj'],
      tarih: json['tarih'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'ad': ad,
      'numara': numara,
      'son_mesaj': sonMesaj,
      'tarih': tarih,
    };
  }
}

class KonusulanKisiListResponse {
  final bool success;
  final List<KonusulanKisi> kisiler;

  KonusulanKisiListResponse({
    required this.success,
    required this.kisiler,
  });

  factory KonusulanKisiListResponse.fromJson(Map<String, dynamic> json) {
    return KonusulanKisiListResponse(
      success: json['success'],
      kisiler: (json['kisiler'] as List)
          .map((e) => KonusulanKisi.fromJson(e))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'kisiler': kisiler.map((e) => e.toJson()).toList(),
    };
  }
}
