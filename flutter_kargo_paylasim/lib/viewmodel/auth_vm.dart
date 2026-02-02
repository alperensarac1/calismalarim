import 'package:flutter/foundation.dart';
import '../service/api_client.dart';
import '../service/api_resp.dart';
import '../service/endpoints.dart';
import '../storage/token_store.dart';

class LoginData {
  final String token;
  final int userId;

  LoginData({required this.token, required this.userId});

  factory LoginData.fromJson(Map<String, dynamic> j) => LoginData(
    token: (j["token"] ?? "") as String,
    userId: (j["user_id"] ?? 0) as int,
  );
}

class RegisterData {
  final int userId;
  final int addressId;

  RegisterData({required this.userId, required this.addressId});

  factory RegisterData.fromJson(Map<String, dynamic> j) => RegisterData(
    userId: (j["user_id"] ?? 0) as int,
    addressId: (j["address_id"] ?? 0) as int,
  );
}

class AuthVM extends ChangeNotifier {
  final ApiClient api;
  final TokenStore tokenStore;

  AuthVM(this.api, this.tokenStore);

  bool isLoading = false;
  String? errorText;

  Future<bool> login(String phone, String password) async {
    isLoading = true;
    errorText = null;
    notifyListeners();

    try {
      final j = await api.postJson(Endpoints.login, {
        "phone": phone.trim(),
        "password": password,
      });

      final r = ApiResp.fromJson(j, (d) => LoginData.fromJson(d as Map<String, dynamic>));
      if (!r.ok || r.data == null) throw ApiError(r.error ?? "Login failed");

      await tokenStore.setToken(r.data!.token);
      return true;
    } catch (e) {
      errorText = e.toString();
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> register({
    required String first,
    required String last,
    required String phone,
    required String tc,
    required String password,
    required String addressTitle,
    required String city,
    required String district,
    required String neighborhood,
    required String addressLine,
    required String postal,
  }) async {
    isLoading = true;
    errorText = null;
    notifyListeners();

    try {
      final j = await api.postJson(Endpoints.register, {
        "phone": phone.trim(),
        "first_name": first.trim(),
        "last_name": last.trim(),
        "tc_no": tc.trim(),
        "password": password,

        "address_title": addressTitle.trim(),
        "city": city.trim(),
        "district": district.trim(),
        "neighborhood": neighborhood.trim(),
        "address_line": addressLine.trim(),
        "postal_code": postal.trim(),
      });

      final r = ApiResp.fromJson(j, (d) => RegisterData.fromJson(d as Map<String, dynamic>));
      if (!r.ok || r.data == null) throw ApiError(r.error ?? "Register failed");

      return true;
    } catch (e) {
      errorText = e.toString();
      return false;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
