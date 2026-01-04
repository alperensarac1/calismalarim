import 'dart:io';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:uuid/uuid.dart';
import '../data/prefs.dart';

class DeviceService {
  final Prefs prefs;
  final DeviceInfoPlugin _plugin = DeviceInfoPlugin();

  DeviceService(this.prefs);

  /// Kalıcı (persist) device_id: ilk çalıştırmada UUID üret, sonra hep aynı kalsın
  Future<String> getOrCreateDeviceId() async {
    final existing = await prefs.getDeviceId();
    if (existing != null && existing.isNotEmpty) return existing;

    final id = const Uuid().v4();
    await prefs.setDeviceId(id);
    return id;
  }

  /// Gerçek cihaz bilgisi (model/brand/os)
  Future<String> getDeviceInfo() async {
    if (Platform.isAndroid) {
      final a = await _plugin.androidInfo;
      // manufacturer + model + sdk
      return "${a.manufacturer} ${a.model} / SDK ${a.version.sdkInt}";
    }
    if (Platform.isIOS) {
      final i = await _plugin.iosInfo;
      // Apple iPhone.. / iOS 17.x
      return "Apple ${i.utsname.machine} / iOS ${i.systemVersion}";
    }
    // diğer platformlar
    return "${Platform.operatingSystem} ${Platform.operatingSystemVersion}";
  }
}
