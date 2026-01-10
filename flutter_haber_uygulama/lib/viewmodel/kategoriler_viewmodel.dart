// viewmodel/kategoriler_viewmodel.dart
import 'package:flutter/foundation.dart';

import '../dao/haber_dao.dart';
import '../model/haber_model.dart';

class KategorilerViewModel extends ChangeNotifier {
  final HaberDao haberDao;

  KategorilerViewModel({required this.haberDao});

  List<HaberModel> kategoriHaberleri = [];

  bool loading = false;
  String? error;

  Future<void> loadKategoriHaberleri(int turId) async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      final tumHaberler = await haberDao.getHaberler();
      kategoriHaberleri = (tumHaberler ?? []).where((h) => h.tur_id == turId).toList();
    } catch (e) {
      error = e.toString();
      kategoriHaberleri = [];
    } finally {
      loading = false;
      notifyListeners();
    }
  }
}
