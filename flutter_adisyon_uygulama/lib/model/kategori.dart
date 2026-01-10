class Kategori {
  final int id;
  final String kategoriAd;

  const Kategori({
    required this.id,
    required this.kategoriAd,
  });

  factory Kategori.fromJson(Map<String, dynamic> json) {
    return Kategori(
      id: (json['id'] ?? 0) as int,
      kategoriAd: (json['kategori_ad'] ?? '') as String,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'kategori_ad': kategoriAd,
  };
}