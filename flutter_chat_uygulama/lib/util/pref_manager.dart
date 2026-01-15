import 'package:shared_preferences/shared_preferences.dart';

class PrefManager {
  static const String _keyKullaniciId = 'kullanici_id';

  Future<void> kaydetKullaniciId(int id) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_keyKullaniciId, id);
  }

  Future<int> getirKullaniciId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getInt(_keyKullaniciId) ?? -1;
  }

  Future<void> temizleKullanici() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyKullaniciId);
  }

  Future<bool> kullaniciVarMi() async {
    return (await getirKullaniciId()) != -1;
  }
}
