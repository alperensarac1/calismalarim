class CreateRideRequest {
  final double pickupLat;
  final double pickupLng;
  final String pickupAddress;
  final double dropoffLat;
  final double dropoffLng;
  final String dropoffAddress;

  CreateRideRequest({
    required this.pickupLat,
    required this.pickupLng,
    required this.pickupAddress,
    required this.dropoffLat,
    required this.dropoffLng,
    required this.dropoffAddress,
  });

  Map<String, dynamic> toJson() => {
    "pickup_lat": pickupLat,
    "pickup_lng": pickupLng,
    "pickup_address": pickupAddress,
    "dropoff_lat": dropoffLat,
    "dropoff_lng": dropoffLng,
    "dropoff_address": dropoffAddress,
  };
}

class RideResponse {
  final int id;
  final int customerId;
  final int? assignedDriverId;
  final double pickupLat;
  final double pickupLng;
  final String pickupAddress;
  final double dropoffLat;
  final double dropoffLng;
  final String dropoffAddress;
  final String status;
  final double? estimatedFare;
  final double? finalFare;
  final String? cancelReason;

  RideResponse({
    required this.id,
    required this.customerId,
    required this.assignedDriverId,
    required this.pickupLat,
    required this.pickupLng,
    required this.pickupAddress,
    required this.dropoffLat,
    required this.dropoffLng,
    required this.dropoffAddress,
    required this.status,
    required this.estimatedFare,
    required this.finalFare,
    required this.cancelReason,
  });

  factory RideResponse.fromJson(Map<String, dynamic> json) {
    return RideResponse(
      id: json["id"],
      customerId: json["customer_id"],
      assignedDriverId: json["assigned_driver_id"],
      pickupLat: (json["pickup_lat"] as num).toDouble(),
      pickupLng: (json["pickup_lng"] as num).toDouble(),
      pickupAddress: json["pickup_address"],
      dropoffLat: (json["dropoff_lat"] as num).toDouble(),
      dropoffLng: (json["dropoff_lng"] as num).toDouble(),
      dropoffAddress: json["dropoff_address"],
      status: json["status"],
      estimatedFare: json["estimated_fare"] == null
          ? null
          : (json["estimated_fare"] as num).toDouble(),
      finalFare: json["final_fare"] == null
          ? null
          : (json["final_fare"] as num).toDouble(),
      cancelReason: json["cancel_reason"],
    );
  }
}
