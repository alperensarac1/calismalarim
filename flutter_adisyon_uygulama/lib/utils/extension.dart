import 'package:flutter/widgets.dart';
import 'package:flutter_adisyon_uygulama/utils/size_type.dart';

extension SizeExtension on Widget {
  Widget sizeBelirle({
    SizeType width = SizeType.wrapContent,
    SizeType height = SizeType.wrapContent,
  }) {
    return SizedBox(
      width: width.toSize(),
      height: height.toSize(),
      child: this,
    );
  }

  Widget sizeBelirlePx({
    double? width,
    double? height,
  }) {
    return SizedBox(
      width: width,
      height: height,
      child: this,
    );
  }
}
void logE(String tag, Object e, [StackTrace? st]) {
  // debugPrint daha güvenli (uzun log kesmez)
  // ignore: avoid_print
  print('[$tag] $e');
  if (st != null) {
    // ignore: avoid_print
    print(st);
  }
}
// lib/utils/extensions.dart

extension FiyatExtension on double {
  String fiyatYaz() => '${toStringAsFixed(2)} ₺';
}
