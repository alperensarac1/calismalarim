import '../models/driver_models.dart';
import '../models/ride_models.dart';
import '../network/api_client.dart';

class DriverRepository {
  final ApiClient apiClient;

  DriverRepository({
    required this.apiClient,
  });

  Future<DriverProfileResponse> setOnline(bool isOnline) async {
    final json = await apiClient.put(
      "driver/online-status",
      {
        "is_online": isOnline,
      },
    );

    return DriverProfileResponse.fromJson(json);
  }

  Future<DriverProfileResponse> updateLocation({
    required double lat,
    required double lng,
  }) async {
    final json = await apiClient.put(
      "driver/location",
      {
        "lat": lat,
        "lng": lng,
      },
    );

    return DriverProfileResponse.fromJson(json);
  }

  Future<AvailableRideListResponse> getAvailableRides() async {
    final json = await apiClient.get("driver/available-rides");
    return AvailableRideListResponse.fromJson(json);
  }

  Future<RideResponse> acceptRide(int rideId) async {
    final json = await apiClient.put(
      "driver/rides/$rideId/accept",
      {},
    );

    return RideResponse.fromJson(json);
  }

  Future<RideResponse> updateRideStatus({
    required int rideId,
    required String status,
    String? note,
  }) async {
    final json = await apiClient.put(
      "driver/rides/$rideId/status",
      {
        "status": status,
        "note": note,
      },
    );

    return RideResponse.fromJson(json);
  }
}
