import 'package:dio/dio.dart';

import '../api/api_client.dart';
import '../api/endpoints.dart';
import '../api/token_store.dart';
import 'auth_model.dart';


class AuthService {
  final ApiClient api;
  final TokenStore store;

  AuthService(this.api, this.store);

  Future<void> login(String email, String password) async {
    final res = await api.dio.post(Endpoints.login, data: {
      "email": email,
      "password": password,
    });

    final lr = LoginResponse.fromJson(res.data);
    if (lr.token.isEmpty) throw Exception("Token gelmedi");
    await store.saveToken(lr.token);
  }

  Future<void> register(String name, String email, String password) async {
    await api.dio.post(Endpoints.register, data: {
      "name": name,
      "email": email,
      "password": password,
    });
  }

  Future<void> logout() async {
    await store.clear();
  }
}
