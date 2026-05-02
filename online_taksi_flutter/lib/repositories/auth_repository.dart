import '../models/auth_models.dart';
import '../network/api_client.dart';

class AuthRepository {
  final ApiClient apiClient;

  AuthRepository({
    required this.apiClient,
  });

  Future<AuthResponse> login({
    required String phone,
    required String password,
  }) async {
    final json = await apiClient.post(
      "auth/login",
      LoginRequest(phone: phone, password: password).toJson(),
    );

    return AuthResponse.fromJson(json);
  }

  Future<AuthResponse> registerCustomer({
    required String fullName,
    required String phone,
    required String? email,
    required String password,
  }) async {
    final json = await apiClient.post(
      "auth/register",
      RegisterRequest(
        fullName: fullName,
        phone: phone,
        email: email,
        password: password,
        role: "customer",
      ).toJson(),
    );

    return AuthResponse.fromJson(json);
  }
}
