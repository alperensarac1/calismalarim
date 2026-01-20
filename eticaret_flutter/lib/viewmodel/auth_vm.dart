import 'package:flutter/foundation.dart';

import '../model/auth_service.dart';


class AuthVm extends ChangeNotifier {
  final AuthService service;

  bool inFlight = false;
  String? error;

  AuthVm(this.service);

  Future<bool> login(String email, String pass) async {
    inFlight = true;
    error = null;
    notifyListeners();
    try {
      await service.login(email, pass);
      return true;
    } catch (e) {
      error = e.toString();
      return false;
    } finally {
      inFlight = false;
      notifyListeners();
    }
  }

  Future<bool> register(String name, String email, String pass) async {
    inFlight = true;
    error = null;
    notifyListeners();
    try {
      await service.register(name, email, pass);
      return true;
    } catch (e) {
      error = e.toString();
      return false;
    } finally {
      inFlight = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    await service.logout();
  }
}
