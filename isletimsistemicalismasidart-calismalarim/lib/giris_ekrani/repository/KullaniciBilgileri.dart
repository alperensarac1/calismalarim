import 'package:shared_preferences/shared_preferences.dart';

class KullaniciBilgileri {


  late SharedPreferences _sp;

  Future<void> init() async {
    _sp = await SharedPreferences.getInstance();
  }

  bool sifreKaydiOlusturulmusMu() {
    return _sp.getBool("kayitolusturulmusmu") ?? false;
  }

  Future<void> sifreKaydi(String sifre) async {
    await _sp.setString("sifre", sifre);
    await _sp.setBool("kayitolusturulmusmu", true);
  }

  bool sifreSorgulama(String dogrulanacakSifre) {
    return dogrulanacakSifre == (_sp.getString("sifre") ?? "");
  }

  Future<void> guvenlikSorusuKaydet(String guvenlikSorusu) async {
    await _sp.setString("guvenliksorusu", guvenlikSorusu);
  }

  String guvenlikSorusuGetir() {
    return _sp.getString("guvenliksorusu") ?? "";
  }

  Future<void> guvenlikSorusuCevapKaydet(String guvenlikSorusuCevap) async {
    await _sp.setString("guvenliksorusucevap", guvenlikSorusuCevap);
  }

  bool guvenlikSorusuDogrula(String dogrulanacakCevap) {
    return dogrulanacakCevap == (_sp.getString("guvenliksorusucevap") ?? "");
  }

  Future<void> sifreSorulsunMuDegistir(bool sifreSorulsunMu) async {
    await _sp.setBool("sifresorulsunmu", sifreSorulsunMu);
  }

  bool sifreSorulsunMu() {
    return _sp.getBool("sifresorulsunmu") ?? true;
  }

  Future<void> sifreDegistir(String degistirilecekSifre) async {
    await _sp.setString("sifre", degistirilecekSifre);
  }

  Future<void> kullaniciBilgileriSifirla() async {
    await _sp.remove("kayitolusturulmusmu");
    await _sp.remove("sifre");
    await _sp.remove("guvenliksorusu");
    await _sp.remove("sifresorulsunmu");
  }
}
