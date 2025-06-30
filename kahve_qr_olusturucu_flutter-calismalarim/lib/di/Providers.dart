import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../data/numara_kayit/NumaraKayitSp.dart';
import '../model/UrunKategori.dart';
import '../services/Services.dart';
import '../services/ServicesImpl.dart';
import '../usecases/DogrulamaKoduOlustur.dart';
import '../viewmodel/MainVM.dart';
import '../viewmodel/QRKodVM.dart';
import '../viewmodel/UrunlerimizVM.dart';

final appContextProvider = Provider<BuildContext>((ref) {
  throw UnimplementedError("⚠️ appContextProvider override edilmelidir.");
});

final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError("⚠️ sharedPreferencesProvider override edilmelidir.");
});

final numaraKayitSpProvider = Provider<NumaraKayitSp>((ref) {
  final sp = ref.watch(sharedPreferencesProvider);
  return NumaraKayitSp(sp);
});

final serviceImplProvider = Provider<ServicesImpl>((ref) {
  return ServicesImpl.getInstance();
});

final kahveServisProvider = Provider<Services>((ref) {
  return ServicesImpl.getInstance().service;
});


final dogrulamaKoduOlusturProvider = Provider<DogrulamaKoduOlustur>((ref) {
  return DogrulamaKoduOlustur();
});

final mainVMProvider = StateNotifierProvider<MainVM, MainState>((ref) {
  final servis = ref.watch(kahveServisProvider);
  final sp = ref.watch(numaraKayitSpProvider);
  return MainVM(servis: servis, numaraKayitSp: sp);
});

final qrKodProvider = StateNotifierProvider<QRKodVM, QRKodState>((ref) {
  final servis = ref.watch(kahveServisProvider);
  final sp = ref.watch(numaraKayitSpProvider);
  final dogrulamaKodu = ref.watch(dogrulamaKoduOlusturProvider);
  return QRKodVM(
    kahveServisDao: servis,
    numaraKayitSp: sp,
    dogrulamaKoduOlustur: dogrulamaKodu,
  );
});

final urunlerimizProvider = StateNotifierProvider<UrunlerimizVM, UrunlerimizState>((ref) {
  final servis = ref.watch(kahveServisProvider);
  return UrunlerimizVM(servis);
});

