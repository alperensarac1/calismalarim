import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:uuid/uuid.dart';

class DogrulamaKoduOlustur {
  final ValueNotifier<String> dogrulamaKoduNotifier = ValueNotifier<String>("");

  void dogrulamaKodunuOlustur() {
    dogrulamaKoduNotifier.value = randomIdOlustur();
  }

  String randomIdOlustur() {
    var uuid = const Uuid();
    return uuid.v4();
  }
}
