import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:kahve_qr_olusturucu/model/UrunCevap.dart';
import 'package:kahve_qr_olusturucu/model/UrunKategori.dart';
import 'package:kahve_qr_olusturucu/model/urun.dart';
import 'package:kahve_qr_olusturucu/services/Services.dart';

class UrunlerimizState {
  final List<Urun> kampanyalar;
  final List<Urun> icecekler;
  final List<Urun> atistirmaliklar;
  final bool isLoading;

  UrunlerimizState({
    this.kampanyalar = const [],
    this.icecekler = const [],
    this.atistirmaliklar = const [],
    this.isLoading = true,
  });

  UrunlerimizState copyWith({
    List<Urun>? kampanyalar,
    List<Urun>? icecekler,
    List<Urun>? atistirmaliklar,
    bool? isLoading,
  }) {
    return UrunlerimizState(
      kampanyalar: kampanyalar ?? this.kampanyalar,
      icecekler: icecekler ?? this.icecekler,
      atistirmaliklar: atistirmaliklar ?? this.atistirmaliklar,
      isLoading: isLoading ?? this.isLoading,
    );
  }
}

class UrunlerimizVM extends StateNotifier<UrunlerimizState> {
  final Services servis;

  UrunlerimizVM(this.servis) : super(UrunlerimizState()) {
    _urunleriYukle();
  }

  Future<void> _urunleriYukle() async {
    try {
      state = state.copyWith(isLoading: true);

      final iceceklerCevap = await servis.kahveWithKategoriId(UrunKategori.ICECEKLER);
      final atistirmalikCevap = await servis.kahveWithKategoriId(UrunKategori.ATISTIRMALIKLAR);
      final tumUrunler = await servis.tumKahveler();

      final kampanyalar = tumUrunler.kahveUrun.where((u) => u.urunIndirim == 1).toList();

      state = state.copyWith(
        icecekler: iceceklerCevap.kahveUrun,
        atistirmaliklar: atistirmalikCevap.kahveUrun,
        kampanyalar: kampanyalar,
        isLoading: false,
      );
    } catch (e) {
      print(" Hata: Ürünler yüklenirken: $e");
      state = state.copyWith(isLoading: false);
    }
  }

  Future<void> yenile() async {
    await _urunleriYukle();
  }
}
