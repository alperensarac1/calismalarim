import 'package:geolocator/geolocator.dart';

class LocationService {
  Future<Position?> getBestLocation() async {
    // izin
    var perm = await Geolocator.checkPermission();
    if (perm == LocationPermission.denied) {
      perm = await Geolocator.requestPermission();
    }
    if (perm == LocationPermission.denied ||
        perm == LocationPermission.deniedForever) {
      return null;
    }

    // servis açık mı
    final enabled = await Geolocator.isLocationServiceEnabled();
    if (!enabled) return null;

    // güncel dene
    try {
      final pos = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
        timeLimit: const Duration(seconds: 8),
      );
      if (isUsable(pos)) return pos;
    } catch (_) {}

    // last known fallback
    try {
      final last = await Geolocator.getLastKnownPosition();
      if (last != null && isUsable(last)) return last;
    } catch (_) {}

    return null;
  }

  /// Android isLocationUsable ile aynı:
  /// - 0,0 değil
  /// - 2dk’dan eski değil
  /// - accuracy 100m’den kötü değil
  bool isUsable(Position p) {
    if (p.latitude == 0.0 && p.longitude == 0.0) return false;

    final ts = p.timestamp;
    if (ts != null) {
      final age = DateTime.now().difference(ts);
      if (age > const Duration(minutes: 2)) return false;
    }

    if (p.accuracy > 100) return false;

    return true;
  }
}
