import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/alarm/Alarm.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/cop_kutusu/CopKutusu.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/galeri/Galeri.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/hesap_makinesi/HesapMakinesi.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/kamera/Kamera.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/muzikcalar/MuzikCalar.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/qrokuyucu/QRKodOkuyucu.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/rehber/entity/RehberDao.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/rehber/view/Rehber.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/takvim/Takvim.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/tarayici/Tarayici.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/telefon/Telefon.dart';

import '../ana_ekran/UygulamaEkrani.dart';
import 'mesajlasma/Mesajlasma.dart';
import 'not_defteri/NotDefteriAnasayfa.dart';
import 'not_defteri/NotDefteriDetaysayfa.dart';


final GoRouter router = GoRouter(
  routes: [
    GoRoute(
      path: '/',
      builder: (context, state) => UygulamaEkraniScreen(),
    ),
    GoRoute(
      path: '/cop_kutusu',
      builder: (context, state) => CopKutusuScreen(),
    ),
    GoRoute(
      path: '/rehber',
      builder: (context, state) => RehberEkrani(vt: RehberVeritabaniYardimcisi(),onRefresh: (){}),
    ),
    GoRoute(
      path: '/mesajlasma',
      builder: (context, state) => MesajlasmaEkrani(),
    ),
    GoRoute(
      path: '/hesap_makinesi',
      builder: (context, state) => HesapMakinesiScreen(),
    ),
    GoRoute(
      path: '/telefon',
      builder: (context, state) => Telefon(),
    ),
    GoRoute(
      path: '/tarayici',
      builder: (context, state) => TarayiciEkrani(),
    ),
    GoRoute(
      path: '/alarm',
      builder: (context, state) => AlarmScreen(),
    ),
    GoRoute(
      path: '/muzik_calar',
      builder: (context, state) => MuzikCalar(),
    ),
    GoRoute(
      path: '/kamera',
      builder: (context, state) => KameraEkrani(),
    ),
    GoRoute(
      path: '/galeri',
      builder: (context, state) => GaleriScreen(),
    ),
    GoRoute(
      path: '/takvim',
      builder: (context, state) => Takvim(),
    ),
    GoRoute(
      path: '/qr_kod_okuyucu',
      builder: (context, state) => QrOkuyucuScreen(),
    ),
    GoRoute(
      path: '/not_defteri',
      builder: (context, state) => NotDefteriAnasayfa(),
    ),
  ],
);
