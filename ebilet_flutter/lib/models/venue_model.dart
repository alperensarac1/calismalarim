/// Sahne / mekan modelidir.
class VenueModel {
  final int id;
  final int? cityId;
  final int? districtId;
  final String name;
  final String? address;
  final int? capacity;

  VenueModel({
    required this.id,
    this.cityId,
    this.districtId,
    required this.name,
    this.address,
    this.capacity,
  });

  factory VenueModel.fromJson(Map<String, dynamic> json) {
    return VenueModel(
      id: int.tryParse(json['id'].toString()) ?? 0,
      cityId: json['city_id'] == null
          ? null
          : int.tryParse(json['city_id'].toString()),
      districtId: json['district_id'] == null
          ? null
          : int.tryParse(json['district_id'].toString()),
      name: json['name']?.toString() ?? '',
      address: json['address']?.toString(),
      capacity: json['capacity'] == null
          ? null
          : int.tryParse(json['capacity'].toString()),
    );
  }
}