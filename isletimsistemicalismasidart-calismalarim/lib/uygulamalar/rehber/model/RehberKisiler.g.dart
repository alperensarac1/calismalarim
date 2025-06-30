// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'RehberKisiler.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RehberKisiler _$RehberKisilerFromJson(Map<String, dynamic> json) =>
    RehberKisiler(
      kisi_id: (json['kisi_id'] as num).toInt(),
      kisi_isim: json['kisi_isim'] as String,
      kisi_numara: json['kisi_numara'] as String,
    );

Map<String, dynamic> _$RehberKisilerToJson(RehberKisiler instance) =>
    <String, dynamic>{
      'kisi_id': instance.kisi_id,
      'kisi_isim': instance.kisi_isim,
      'kisi_numara': instance.kisi_numara,
    };
