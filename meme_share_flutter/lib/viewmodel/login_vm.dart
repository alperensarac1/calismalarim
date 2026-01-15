import 'package:flutter/foundation.dart';
import '../model/kullanici_response.dart';
import '../service/meme_service.dart';
class LoginVM extends ChangeNotifier {
  final MemeApiService api;

  LoginVM({required this.api});

  bool isLoading = false;
  String? error;

  KullaniciResponse? loginResult;

  Future<void> loginUser(String username, String password) async {
    isLoading = true;
    error = null;
    notifyListeners();

    try {
      final res = await api.loginUser(username: username, password: password);
      loginResult = res;

      // Kotlin'deki fallback mantığı
      if (res.success != true) {
        error = res.message.isNotEmpty ? res.message : 'Giriş başarısız';
      }
    } catch (e) {
      loginResult = KullaniciResponse(success: false, message: 'Bağlantı hatası: $e', userId: -1);
      error = loginResult!.message;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
