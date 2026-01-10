// viewmodel/haber_detay_viewmodel.dart
import 'package:flutter/foundation.dart';

import '../dao/haber_dao.dart';
import '../model/yorum_insert_request.dart';
import '../model/yorum_model.dart';

class HaberDetayViewModel extends ChangeNotifier {
  final HaberDao haberDao;

  HaberDetayViewModel({HaberDao? dao}) : haberDao = dao ?? HaberDao();

  // Kotlin: private val _yorumlar = MutableStateFlow<List<YorumModel>>(emptyList())
  List<YorumModel> yorumlar = [];

  bool loading = false;
  String? error;

  // Kotlin: fun loadYorumlar(haberId: Int)
  Future<void> loadYorumlar(int haberId) async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      yorumlar = await haberDao.getYorumlar(haberId) ?? [];
    } catch (e) {
      error = e.toString();
      yorumlar = [];
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  // Kotlin: fun yorumEkle(haberId: Int, takmaAd: String, yorumMetni: String)
  Future<void> yorumEkle(int haberId, String takmaAd, String yorumMetni) async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      final resp = await haberDao.insertYorum(
        YorumInsertRequest(
          haber_id: haberId,
          takma_ad: takmaAd,
          yorum_metni: yorumMetni,
        ),
      );

      if (resp?.success == true) {
        await loadYorumlar(haberId);
      } else {
        error = resp?.error ?? 'Yorum eklenemedi';
      }
    } catch (e) {
      error = e.toString();
    } finally {
      loading = false;
      notifyListeners();
    }
  }
}
