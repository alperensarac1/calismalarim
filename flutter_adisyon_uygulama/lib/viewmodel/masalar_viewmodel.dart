import 'package:flutter/foundation.dart';

import '../model/masa.dart';
import '../service/services.dart';
import '../service/services_impl.dart';
import '../utils/extension.dart';

class MasalarViewModel extends ChangeNotifier {
  final Services _dao;

  MasalarViewModel({Services? dao}) : _dao = dao ?? ServicesImpl.getInstance();

  List<Masa> masalar = const [];
  bool birlesmeSonucu = false;
  bool loading = false;

  Future<void> masalariYukle() async {
    loading = true;
    notifyListeners();

    try {
      final liste = await _dao.masalariGetir();
      masalar = liste;
    } catch (e, st) {
      logE('MasalarVM', e, st);
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<void> masaEkle({VoidCallback? onSuccess}) async {
    try {
      await _dao.masaEkle();
      onSuccess?.call();
      // istersen otomatik refresh:
      await masalariYukle();
    } catch (e, st) {
      logE('MasalarVM', e, st);
    }
  }

  Future<void> masaSil(int masaId) async {
    try {
      await _dao.masaSil(masaId);
      // istersen listeden düş:
      masalar = masalar.where((m) => m.id != masaId).toList();
      notifyListeners();
    } catch (e, st) {
      logE('MasalarVM', e, st);
    }
  }

  Future<void> masaBirlestir(int anaId, int bId) async {
    try {
      await _dao.masaBirlestir(anaId, bId);
      birlesmeSonucu = true;
      notifyListeners();
      // istersen refresh:
      await masalariYukle();
    } catch (e, st) {
      logE('MasalarVM', e, st);
    }
  }

  void guncelleMasa(Masa masa) {
    final list = masalar.toList();
    final index = list.indexWhere((x) => x.id == masa.id);
    if (index != -1) {
      list[index] = masa;
      masalar = list;
      notifyListeners();
    }
  }
}
