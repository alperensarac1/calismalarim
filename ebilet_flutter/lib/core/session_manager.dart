import 'package:shared_preferences/shared_preferences.dart';

import '../models/user_model.dart';

/// SessionManager
///
/// Kullanıcı giriş yaptıktan sonra token ve temel kullanıcı bilgilerini
/// SharedPreferences içinde saklar.
///
/// Android SharedPreferences mantığı ile aynıdır.
class SessionManager {
  static const String _keyUserId = 'user_id';
  static const String _keyFullName = 'full_name';
  static const String _keyEmail = 'email';
  static const String _keyPhone = 'phone';
  static const String _keyRole = 'role';
  static const String _keyApiToken = 'api_token';
  static const String _keyIsLoggedIn = 'is_logged_in';

  /// Kullanıcıyı kaydeder.
  static Future<void> saveUser(UserModel user) async {
    final prefs = await SharedPreferences.getInstance();

    await prefs.setInt(_keyUserId, user.id);
    await prefs.setString(_keyFullName, user.fullName);
    await prefs.setString(_keyEmail, user.email);
    await prefs.setString(_keyPhone, user.phone ?? '');
    await prefs.setString(_keyRole, user.role);
    await prefs.setString(_keyApiToken, user.apiToken ?? '');
    await prefs.setBool(_keyIsLoggedIn, true);
  }

  static Future<bool> isLoggedIn() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyIsLoggedIn) ?? false;
  }

  static Future<String> getApiToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyApiToken) ?? '';
  }

  static Future<String> getFullName() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyFullName) ?? '';
  }

  static Future<String> getEmail() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyEmail) ?? '';
  }

  static Future<String> getRole() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyRole) ?? 'user';
  }

  static Future<bool> isStaffOrAdmin() async {
    final role = await getRole();
    return role == 'staff' || role == 'admin';
  }

  /// Çıkış yapar.
  static Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();

    await prefs.remove(_keyUserId);
    await prefs.remove(_keyFullName);
    await prefs.remove(_keyEmail);
    await prefs.remove(_keyPhone);
    await prefs.remove(_keyRole);
    await prefs.remove(_keyApiToken);
    await prefs.remove(_keyIsLoggedIn);
  }
}