// viewmodel/haberler_viewmodel.dart
import 'package:flutter/foundation.dart';

import '../dao/haber_dao.dart';
import '../model/haber_model.dart';
import '../model/haber_turu_model.dart';

class HaberlerViewModel extends ChangeNotifier {
  final HaberDao haberDao;

  HaberlerViewModel({HaberDao? dao}) : haberDao = dao ?? HaberDao();

  // Kotlin: _tumHaberler, _filtrelenmisHaberler
  List<HaberModel> tumHaberler = [];
  List<HaberModel> filtrelenmisHaberler = [];

  // Kotlin: _kategoriler
  List<HaberTuruModel> kategoriler = [];

  // Kotlin: _haber
  HaberModel? haber;

  // Kotlin: _sonHaberler, _gundemHaberler, _sonDakikaHaberler
  List<HaberModel> sonHaberler = [];
  List<HaberModel> gundemHaberler = [];
  List<HaberModel> sonDakikaHaberler = [];

  bool loading = false;
  String? error;

  // Kotlin: fun loadData()
  Future<void> loadData() async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      final haberList = await haberDao.getHaberler() ?? [];
      tumHaberler = haberList;
      filtrelenmisHaberler = List.of(haberList);

      kategoriler = await haberDao.getKategoriler() ?? [];
    } catch (e) {
      error = e.toString();
      tumHaberler = [];
      filtrelenmisHaberler = [];
      kategoriler = [];
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  // Kotlin: fun getHaberById(haberId: Int)
  Future<void> getHaberById(int haberId) async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      haber = await haberDao.getHaberById(haberId);
    } catch (e) {
      error = e.toString();
      haber = null;
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  // Kotlin: fun filtreleKategori(turId: Int?)
  void filtreleKategori(int? turId) {
    if (turId == null) {
      filtrelenmisHaberler = List.of(tumHaberler);
    } else {
      filtrelenmisHaberler = tumHaberler.where((h) => h.tur_id == turId).toList();
    }
    notifyListeners();
  }

  // Kotlin: fun loadSon3Haber()
  Future<void> loadSon3Haber() async {
    try {
      sonHaberler = await haberDao.getSon3Haber() ?? [];
      notifyListeners();
    } catch (_) {}
  }

  // Kotlin: fun loadGundemHaberler()
  Future<void> loadGundemHaberler() async {
    try {
      gundemHaberler = await haberDao.getGundemHaberler() ?? [];
      notifyListeners();
    } catch (_) {}
  }

  // Kotlin: fun loadSonDakikaHaberler()
  Future<void> loadSonDakikaHaberler() async {
    try {
      sonDakikaHaberler = await haberDao.getSonDakikaHaberler() ?? [];
      notifyListeners();
    } catch (_) {}
  }

  // Kotlin: fun loadKategoriler()
  Future<void> loadKategoriler() async {
    try {
      kategoriler = await haberDao.getKategoriler() ?? [];
      notifyListeners();
    } catch (_) {}
  }

  // UI tarafı Kotlin'deki `val haberler: StateFlow<List<HaberModel>>` gibi kullanılsın diye:
  List<HaberModel> get haberler => filtrelenmisHaberler;
}
