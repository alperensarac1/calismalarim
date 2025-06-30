import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/numara_kayit/NumaraKayitSp.dart';
import '../di/Providers.dart';
import '../services/Services.dart';
import '../services/ServicesImpl.dart';


class MainVM extends StateNotifier<MainState> {
  final Services servis;
  final NumaraKayitSp numaraKayitSp;

  MainVM({required this.servis, required this.numaraKayitSp}) : super(MainState()) {
    _init();
  }

  void _init() async {
    final numara = numaraKayitSp.numaraGetir();
    if (numara.isNotEmpty) {
      state = state.copyWith(telefonNumarasi: numara);
      await kullaniciEkle(numara);
      _startPeriodicCall(numara);
    }
  }

  Future<void> kullaniciEkle(String numara) async {
    if (numara.isEmpty) return;
    final response = await servis.kullaniciEkle(numara);
    state = state.copyWith(
      kahveSayisi: response.kahveSayisi,
      hediyeKahve: response.hediyeKahve,
    );
  }

  void numarayiKaydet(String numara) {
    numaraKayitSp.numaraKaydet(numara);
    state = state.copyWith(telefonNumarasi: numara);
    kullaniciEkle(numara);
    _startPeriodicCall(numara);
  }

  void _startPeriodicCall(String numara) {
    Timer.periodic(const Duration(seconds: 15), (_) {
      if (numaraKayitSp.numaraKaydedildiMi()) {
        final tel = numaraKayitSp.numaraGetir();
        if (tel.isNotEmpty) {
          kullaniciEkle(tel);
        }
      }
    });
  }

  bool checkTelefonNumarasi() {
    return numaraKayitSp.numaraKaydedildiMi();
  }
}
class MainState {
  final int kahveSayisi;
  final int hediyeKahve;
  final String? telefonNumarasi;

  const MainState({
    this.kahveSayisi = 0,
    this.hediyeKahve = 0,
    this.telefonNumarasi,
  });

  MainState copyWith({
    int? kahveSayisi,
    int? hediyeKahve,
    String? telefonNumarasi,
  }) {
    return MainState(
      kahveSayisi: kahveSayisi ?? this.kahveSayisi,
      hediyeKahve: hediyeKahve ?? this.hediyeKahve,
      telefonNumarasi: telefonNumarasi ?? this.telefonNumarasi,
    );
  }
}
