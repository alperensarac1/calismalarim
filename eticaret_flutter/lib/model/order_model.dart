class CheckoutRequest {
  final String addressName;
  final String addressLine1;
  final String city;
  final String district;
  final String postalCode;

  CheckoutRequest({
    required this.addressName,
    required this.addressLine1,
    required this.city,
    required this.district,
    required this.postalCode,
  });

  Map<String, dynamic> toJson() => {
    "addressName": addressName,
    "addressLine1": addressLine1,
    "city": city,
    "district": district,
    "postalCode": postalCode,
  };
}

class CheckoutResponse {
  final int orderId;
  CheckoutResponse({required this.orderId});

  factory CheckoutResponse.fromJson(Map<String, dynamic> j) =>
      CheckoutResponse(orderId: (j["orderId"] as num).toInt());
}

class OrderSummaryDto {
  final int id;
  final String status;
  final String currency;
  final double totalAmount;
  final String createdAt;

  OrderSummaryDto({
    required this.id,
    required this.status,
    required this.currency,
    required this.totalAmount,
    required this.createdAt,
  });

  factory OrderSummaryDto.fromJson(Map<String, dynamic> j) => OrderSummaryDto(
    id: (j["id"] as num).toInt(),
    status: j["status"]?.toString() ?? "",
    currency: j["currency"]?.toString() ?? "TRY",
    totalAmount: (j["totalAmount"] as num).toDouble(),
    createdAt: j["createdAt"]?.toString() ?? "",
  );
}

class OrderLineDto {
  final String name;
  final int quantity;
  final double unitPrice;
  final double lineTotal;

  OrderLineDto({
    required this.name,
    required this.quantity,
    required this.unitPrice,
    required this.lineTotal,
  });

  factory OrderLineDto.fromJson(Map<String, dynamic> j) => OrderLineDto(
    name: j["name"]?.toString() ?? "",
    quantity: (j["quantity"] as num).toInt(),
    unitPrice: (j["unitPrice"] as num).toDouble(),
    lineTotal: (j["lineTotal"] as num).toDouble(),
  );
}

class OrderDetailDto {
  final int id;
  final String status;
  final String currency;
  final double totalAmount;
  final String? addressName;
  final String? addressLine1;
  final List<OrderLineDto> items;

  OrderDetailDto({
    required this.id,
    required this.status,
    required this.currency,
    required this.totalAmount,
    required this.addressName,
    required this.addressLine1,
    required this.items,
  });

  factory OrderDetailDto.fromJson(Map<String, dynamic> j) => OrderDetailDto(
    id: (j["id"] as num).toInt(),
    status: j["status"]?.toString() ?? "",
    currency: j["currency"]?.toString() ?? "TRY",
    totalAmount: (j["totalAmount"] as num).toDouble(),
    addressName: j["addressName"]?.toString(),
    addressLine1: j["addressLine1"]?.toString(),
    items: (j["items"] as List).map((e) => OrderLineDto.fromJson(e)).toList(),
  );
}
