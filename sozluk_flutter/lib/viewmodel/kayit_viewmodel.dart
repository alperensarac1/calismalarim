import 'package:flutter/foundation.dart';

import '../dao/sozluk_dao.dart';
import '../model/simple_response.dart';


class KayitViewModel extends ChangeNotifier {
  final SozlukDao _dao;

  KayitViewModel({SozlukDao? dao}) : _dao = dao ?? SozlukDao();

  SimpleResponse? registerResult;
  bool loading = false;

  Future<void> register({
    required String username,
    required String password,
    required String email,
  }) async {
    loading = true;
    registerResult = null;
    notifyListeners();

    try {
      registerResult = await _dao.register(
        username: username,
        password: password,
        email: email,
      );
    } catch (_) {
      registerResult = SimpleResponse(success: false, message: 'Bağlantı hatası');
    }

    loading = false;
    notifyListeners();
  }
}
