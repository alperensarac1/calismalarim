import 'package:flutter/foundation.dart';

import '../dao/sozluk_dao.dart';
import '../model/simple_response.dart';
import '../util/states.dart';

class EntryEkleViewModel extends ChangeNotifier {
  final SozlukDao _dao;

  EntryEkleViewModel({SozlukDao? dao}) : _dao = dao ?? SozlukDao();

  AddEntryUiState ui = const AddEntryUiState();
  SimpleResponse? addResult;

  Future<void> addEntry({
    required int userId,
    required String title,
    required String content,
  }) async {
    ui = ui.copyWith(loading: true, error: null);
    addResult = null;
    notifyListeners();

    try {
      final res = await _dao.addEntry(
        userId: userId,
        title: title,
        content: content,
      );

      ui = ui.copyWith(loading: false, error: null);
      addResult = res;

      if (res.success == false && (res.message?.isNotEmpty ?? false)) {
        ui = ui.copyWith(error: res.message);
      }
    } catch (_) {
      ui = ui.copyWith(loading: false, error: 'Bağlantı hatası');
      addResult = SimpleResponse(success: false, message: 'Bağlantı hatası');
    }

    notifyListeners();
  }

  void clearResult() {
    addResult = null;
    notifyListeners();
  }
}
