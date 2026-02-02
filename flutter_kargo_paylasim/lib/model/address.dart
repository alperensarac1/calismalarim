class Address {
  final int id;
  final String title;
  final String city;
  final String district;
  final String addressLine;
  final int isDefault;

  Address({
    required this.id,
    required this.title,
    required this.city,
    required this.district,
    required this.addressLine,
    required this.isDefault,
  });

  factory Address.fromJson(Map<String, dynamic> j) => Address(
    id: (j["id"] ?? 0) as int,
    title: (j["title"] ?? "") as String,
    city: (j["city"] ?? "") as String,
    district: (j["district"] ?? "") as String,
    addressLine: (j["address_line"] ?? "") as String,
    isDefault: (j["is_default"] ?? 0) as int,
  );
}
