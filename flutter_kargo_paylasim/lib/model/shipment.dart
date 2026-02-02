class Shipment {
  final int id;
  final String status;
  final String pickupCode;
  final String? cargoCompanyName;

  Shipment({
    required this.id,
    required this.status,
    required this.pickupCode,
    this.cargoCompanyName,
  });

  factory Shipment.fromJson(Map<String, dynamic> j) => Shipment(
    id: (j["id"] ?? 0) as int,
    status: (j["status"] ?? "") as String,
    pickupCode: (j["pickup_code"] ?? "") as String,
    cargoCompanyName: j["cargo_company_name"]?.toString(),
  );
}
