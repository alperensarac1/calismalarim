class Mesaj {
  final int id;
  final int gonderenId;
  final int aliciId;
  final String? mesajText;
  final int resimVar;
  final String? resimUrl;
  final String tarih;

  Mesaj({
    required this.id,
    required this.gonderenId,
    required this.aliciId,
    this.mesajText,
    required this.resimVar,
    this.resimUrl,
    required this.tarih,
  });

  factory Mesaj.fromJson(Map<String, dynamic> json) {
    return Mesaj(
      id: int.parse(json['id'].toString()),
      gonderenId: int.parse(json['gonderen_id'].toString()),
      aliciId: int.parse(json['alici_id'].toString()),
      mesajText: json['mesaj_text'],
      resimVar: int.parse(json['resim_var'].toString()),
      resimUrl: json['resim_url'],
      tarih: json['tarih'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'gonderen_id': gonderenId,
      'alici_id': aliciId,
      'mesaj_text': mesajText,
      'resim_var': resimVar,
      'resim_url': resimUrl,
      'tarih': tarih,
    };
  }
}

class MesajListResponse {
  final bool success;
  final List<Mesaj> mesajlar;

  MesajListResponse({
    required this.success,
    required this.mesajlar,
  });

  factory MesajListResponse.fromJson(Map<String, dynamic> json) {
    return MesajListResponse(
      success: json['success'],
      mesajlar: (json['mesajlar'] as List)
          .map((e) => Mesaj.fromJson(e))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'mesajlar': mesajlar.map((e) => e.toJson()).toList(),
    };
  }
}
