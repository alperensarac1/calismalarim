/// İlçe modelidir.
///
/// city_id bazı endpointlerde gelebilir,
/// bazı detay endpointlerinde gelmeyebilir.
/// Bu yüzden nullable tuttuk.
class DistrictModel {
  final int id;
  final int? cityId;
  final String name;

  DistrictModel({
    required this.id,
    this.cityId,
    required this.name,
  });

  factory DistrictModel.fromJson(Map<String, dynamic> json) {
    return DistrictModel(
      id: int.tryParse(json['id'].toString()) ?? 0,
      cityId: json['city_id'] == null
          ? null
          : int.tryParse(json['city_id'].toString()),
      name: json['name']?.toString() ?? '',
    );
  }
}