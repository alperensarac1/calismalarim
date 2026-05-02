import '../models/ride_models.dart';
import '../network/api_client.dart';

class RideRepository {
  final ApiClient apiClient;

  RideRepository({
    required this.apiClient,
  });

  Future<RideResponse> createRide(CreateRideRequest request) async {
    final json = await apiClient.post(
      "customer/rides",
      request.toJson(),
    );

    return RideResponse.fromJson(json);
  }
}
