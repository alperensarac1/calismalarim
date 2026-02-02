import 'package:shared_preferences/shared_preferences.dart';

class TokenStore {
  static const _k = "cargo_token";

  Future<String?> getToken() async {
    final sp = await SharedPreferences.getInstance();
    return sp.getString(_k);
  }

  Future<void> setToken(String token) async {
    final sp = await SharedPreferences.getInstance();
    await sp.setString(_k, token);
  }

  Future<void> clear() async {
    final sp = await SharedPreferences.getInstance();
    await sp.remove(_k);
  }
}
