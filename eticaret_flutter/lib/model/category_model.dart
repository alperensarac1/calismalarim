class CategoryDto {
  final int id;
  final String name;

  CategoryDto({required this.id, required this.name});

  factory CategoryDto.fromJson(Map<String, dynamic> j) => CategoryDto(
    id: (j["id"] as num).toInt(),
    name: j["name"]?.toString() ?? j["tur_adi"]?.toString() ?? "",
  );
}
