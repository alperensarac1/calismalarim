class ProductListItem {
  final int id;
  final String name;
  final double price;
  final String? imageUrl;
  final int? discountPercent;

  ProductListItem({
    required this.id,
    required this.name,
    required this.price,
    required this.imageUrl,
    required this.discountPercent,
  });

  factory ProductListItem.fromJson(Map<String, dynamic> j) => ProductListItem(
    id: (j["id"] as num).toInt(),
    name: j["name"]?.toString() ?? "",
    price: (j["price"] as num).toDouble(),
    imageUrl: j["imageUrl"]?.toString() ?? j["image_url"]?.toString(),
    discountPercent: j["discountPercent"] is num ? (j["discountPercent"] as num).toInt() : null,
  );
}


class ProductDto {
  final int id;
  final String name;
  final double price;
  final String? imageUrl;
  final String? description;

  ProductDto({
    required this.id,
    required this.name,
    required this.price,
    required this.imageUrl,
    required this.description,
  });

  factory ProductDto.fromJson(Map<String, dynamic> j) => ProductDto(
    id: (j["id"] as num).toInt(),
    name: j["name"]?.toString() ?? "",
    price: (j["price"] as num).toDouble(),
    imageUrl: j["imageUrl"]?.toString() ?? j["image_url"]?.toString(),
    description: j["description"]?.toString() ?? j["desc"]?.toString(),
  );
}

class ProductListDto {
  final int id;
  final String name;
  final double price;
  final String? imageUrl;

  ProductListDto({
    required this.id,
    required this.name,
    required this.price,
    required this.imageUrl,
  });

  factory ProductListDto.fromJson(Map<String, dynamic> j) => ProductListDto(
    id: (j["id"] as num).toInt(),
    name: j["name"]?.toString() ?? "",
    price: (j["price"] as num).toDouble(),
    imageUrl: j["imageUrl"]?.toString() ?? j["image_url"]?.toString(),
  );
}

class ProductListPage {
  final List<ProductListDto> items; // <-- KRİTİK
  final int total;
  final int page;
  final int per;

  ProductListPage({
    required this.items,
    required this.total,
    required this.page,
    required this.per,
  });

  factory ProductListPage.fromJson(Map<String, dynamic> j) => ProductListPage(
    items: (j["items"] as List)
        .map((e) => ProductListDto.fromJson(e as Map<String, dynamic>))
        .toList(),
    total: (j["total"] as num?)?.toInt() ?? 0,
    page: (j["page"] as num?)?.toInt() ?? 1,
    per: (j["per"] as num?)?.toInt() ?? 12,
  );
}
