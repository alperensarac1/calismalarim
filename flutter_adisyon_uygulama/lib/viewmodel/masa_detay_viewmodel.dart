import 'package:flutter/foundation.dart';

import '../model/kategori.dart';
import '../model/masa.dart';
import '../model/masa_urun.dart';
import '../model/urun.dart';
import '../service/services.dart';
import '../service/services_impl.dart';
import '../utils/extension.dart';



class MasaDetayViewModel extends ChangeNotifier {
  final int masaId;
  final Services _dao;

  MasaDetayViewModel({
    required this.masaId,
    Services? dao,
  }) : _dao = dao ?? ServicesImpl.getInstance();

  List<MasaUrun> urunler = const [];
  List<Urun> tumUrunler = const [];
  List<Kategori> kategoriler = const [];
  double toplamFiyat = 0.0;
  Masa? masa;

  bool odemeTamamlandi = false;

  bool loading = false;

  Future<void> yukleTumVeriler() async {
    loading = true;
    notifyListeners();

    try {
      // 1) Masa
      final m = await _dao.masaGetir(masaId);
      masa = m;

      // 2) Masaya ait ürünler + tüm ürünler + kategoriler
      final masaUrunleri = await _dao.masaUrunleriniGetir(masaId);
      final urunListesi = await _dao.urunleriGetir();
      final katListesi = await _dao.kategorileriGetir();

      // 3) Adet eşitle
      final updated = urunListesi.map((u) {
        final mu = masaUrunleri.cast<MasaUrun?>().firstWhere(
              (x) => x != null && x.urunId == u.id,
          orElse: () => null,
        );

        // Urun modelinde urunAdet mutable idi; burada yeni değer set edelim:
        final copy = Urun(
          id: u.id,
          urunAd: u.urunAd,
          urunFiyat: u.urunFiyat,
          urunResim: u.urunResim,
          urunAdet: mu?.adet ?? 0,
          urunKategori: u.urunKategori,
        );
        return copy;
      }).toList();

      urunler = masaUrunleri;
      tumUrunler = updated;
      kategoriler = katListesi;

      toplamFiyat = masaUrunleri.fold<double>(
        0.0,
            (sum, item) => sum + item.toplamFiyat,
      );
    } catch (e, st) {
      logE('MasaDetayVM', e, st);
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<void> urunEkle(int urunId, {int adet = 1}) async {
    try {
      await _dao.urunEkleMasaya(masaId, urunId, adet);
      await yukleTumVeriler();
    } catch (e, st) {
      logE('MasaDetayVM', e, st);
    }
  }

  Future<void> urunCikar(int urunId) async {
    try {
      await _dao.urunCikar(masaId, urunId);
      await yukleTumVeriler();
    } catch (e, st) {
      logE('MasaDetayVM', e, st);
    }
  }

  Future<void> odemeAl({VoidCallback? onSuccess}) async {
    try {
      await _dao.masaOdemeYap(masaId);
      toplamFiyat = 0.0;
      odemeTamamlandi = true;
      notifyListeners();
      onSuccess?.call();
    } catch (e, st) {
      logE('MasaDetayVM', e, st);
    }
  }
}
