import 'package:flutter/foundation.dart';
import '../model/kullanici_response.dart';
import '../service/meme_service.dart';

class RegisterVM extends ChangeNotifier {
  final MemeApiService api;

  RegisterVM({required this.api});

  bool isLoading = false;
  String? error;

  KullaniciResponse? registerResult;

  Future<void> registerUser(String username, String password) async {
    isLoading = true;
    error = null;
    notifyListeners();

    try {
      final res = await api.registerUser(username: username, password: password);
      registerResult = res;

      if (res.success != true) {
        error = res.message.isNotEmpty ? res.message : 'Sunucu hatası';
      }
    } catch (e) {
      registerResult = KullaniciResponse(success: false, message: 'Bağlantı hatası: $e', userId: -1);
      error = registerResult!.message;
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
