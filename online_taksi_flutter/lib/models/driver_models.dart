class DriverProfileResponse {
  final int userId;
  final bool isOnline;
  final double? currentLat;
  final double? currentLng;

  DriverProfileResponse({
    required this.userId,
    required this.isOnline,
    required this.currentLat,
    required this.currentLng,
  });

  factory DriverProfileResponse.fromJson(Map<String, dynamic> json) {
    return DriverProfileResponse(
      userId: json["user_id"],
      isOnline: json["is_online"],
      currentLat: json["current_lat"] == null ? null : (json["current_lat"] as num).toDouble(),
      currentLng: json["current_lng"] == null ? null : (json["current_lng"] as num).toDouble(),
    );
  }
}

class AvailableRideItem {
  final int id;
  final int customerId;
  final double pickupLat;
  final double pickupLng;
  final String pickupAddress;
  final double dropoffLat;
  final double dropoffLng;
  final String dropoffAddress;
  final String status;
  final double? estimatedFare;

  AvailableRideItem({
    required this.id,
    required this.customerId,
    required this.pickupLat,
    required this.pickupLng,
    required this.pickupAddress,
    required this.dropoffLat,
    required this.dropoffLng,
    required this.dropoffAddress,
    required this.status,
    required this.estimatedFare,
  });

  factory AvailableRideItem.fromJson(Map<String, dynamic> json) {
    return AvailableRideItem(
      id: json["id"] ?? json["ride_id"],
      customerId: json["customer_id"],
      pickupLat: (json["pickup_lat"] as num).toDouble(),
      pickupLng: (json["pickup_lng"] as num).toDouble(),
      pickupAddress: json["pickup_address"],
      dropoffLat: (json["dropoff_lat"] as num).toDouble(),
      dropoffLng: (json["dropoff_lng"] as num).toDouble(),
      dropoffAddress: json["dropoff_address"],
      status: json["status"],
      estimatedFare: json["estimated_fare"] == null ? null : (json["estimated_fare"] as num).toDouble(),
    );
  }
}

class AvailableRideListResponse {
  final List<AvailableRideItem> rides;

  AvailableRideListResponse({required this.rides});

  factory AvailableRideListResponse.fromJson(Map<String, dynamic> json) {
    final list = json["rides"] as List<dynamic>? ?? [];
    return AvailableRideListResponse(
      rides: list.map((e) => AvailableRideItem.fromJson(e)).toList(),
    );
  }
}
