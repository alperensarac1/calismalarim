class AddToCartRequest {
  final int productId;
  final int quantity;

  AddToCartRequest({required this.productId, required this.quantity});

  Map<String, dynamic> toJson() => {
    "product_id": productId,
    "quantity": quantity,
  };
}

class UpdateCartItemRequest {
  final int quantity;
  UpdateCartItemRequest({required this.quantity});

  Map<String, dynamic> toJson() => {"quantity": quantity};
}

class BasicOk {
  final bool ok;
  BasicOk({required this.ok});

  factory BasicOk.fromJson(Map<String, dynamic> j) => BasicOk(ok: j["ok"] == true);
}

class AddToCartResponse {
  final int? itemId;
  AddToCartResponse({this.itemId});

  factory AddToCartResponse.fromJson(Map<String, dynamic> j) =>
      AddToCartResponse(itemId: (j["item_id"] as num?)?.toInt());
}

class CartItemDto {
  final int itemId;
  final int productId;
  final String name;
  final int quantity;
  final double salePrice;
  final String? imageUrl;

  CartItemDto({
    required this.itemId,
    required this.productId,
    required this.name,
    required this.quantity,
    required this.salePrice,
    required this.imageUrl,
  });

  factory CartItemDto.fromJson(Map<String, dynamic> j) => CartItemDto(
    itemId: (j["item_id"] as num).toInt(),
    productId: (j["product_id"] as num).toInt(),
    name: j["name"]?.toString() ?? "",
    quantity: (j["quantity"] as num).toInt(),
    salePrice: (j["sale_price"] as num).toDouble(),
    imageUrl: j["image_url"]?.toString() ?? j["imageUrl"]?.toString(),
  );
}

class CartDto {
  final List<CartItemDto> items;
  final int totalItems;
  final double total;

  CartDto({required this.items, required this.totalItems, required this.total});

  factory CartDto.fromJson(Map<String, dynamic> j) => CartDto(
    items: (j["items"] as List).map((e) => CartItemDto.fromJson(e)).toList(),
    totalItems: (j["total_items"] as num?)?.toInt() ?? 0,
    total: (j["total"] as num?)?.toDouble() ?? 0.0,
  );
}
