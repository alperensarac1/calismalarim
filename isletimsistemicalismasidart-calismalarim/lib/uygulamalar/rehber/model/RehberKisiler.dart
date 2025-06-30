

import 'dart:convert'; // JSON işlemleri için
import 'package:json_annotation/json_annotation.dart';

part 'RehberKisiler.g.dart'; // Kodun otomatik oluşturulacak kısmı

@JsonSerializable()
class RehberKisiler {
  int kisi_id;
  String kisi_isim;
  String kisi_numara;

  RehberKisiler({
    required this.kisi_id,
    required this.kisi_isim,
    required this.kisi_numara,
  });

  // JSON'dan nesneye dönüştürme
  factory RehberKisiler.fromJson(Map<String, dynamic> json) =>
      _$RehberKisilerFromJson(json);

  // Nesneden JSON'a dönüştürme
  Map<String, dynamic> toJson() => _$RehberKisilerToJson(this);
}

