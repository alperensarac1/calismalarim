class Kategori {
  final int id;
  final String kategoriAd;

  Kategori({
    required this.id,
    required this.kategoriAd,
  });

  factory Kategori.fromJson(Map<String, dynamic> json) => Kategori(
    id: int.parse(json['id'].toString()),
    kategoriAd: json['kategori_ad'] ?? '',
  );

  Map<String, dynamic> toJson() => {
    'id': id,
    'kategori_ad': kategoriAd,
  };
}
