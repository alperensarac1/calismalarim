import 'package:flutter/foundation.dart';

import '../dao/sozluk_dao.dart';
import '../model/simple_response.dart';


class GirisViewModel extends ChangeNotifier {
  final SozlukDao _dao;

  GirisViewModel({SozlukDao? dao}) : _dao = dao ?? SozlukDao();

  SimpleResponse? loginResult;
  bool loading = false;

  Future<void> login({
    required String username,
    required String password,
  }) async {
    loading = true;
    loginResult = null;
    notifyListeners();

    try {
      loginResult = await _dao.login(username: username, password: password);
    } catch (_) {
      loginResult = SimpleResponse(success: false, message: 'Bağlantı hatası');
    }

    loading = false;
    notifyListeners();
  }
}
