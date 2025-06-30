import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart'; // GirisEkraniGraph'ın olduğu dosyayı içe aktar
import 'package:isletimsistemicalismasidart/giris_ekrani/GirisEkraniGraph.dart';
import 'package:isletimsistemicalismasidart/uygulamalar/not_defteri/NotViewModel.dart';
import 'package:provider/provider.dart';
import 'package:timezone/data/latest.dart' as tz;

import 'ana_ekran/entity/Const.dart';


void main() async{
  tz.initializeTimeZones();
  WidgetsFlutterBinding.ensureInitialized();
  await Const.initialize();
  runApp(MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) => NotDefteriViewModel()),
    ],
    child: GirisEkraniGraph(),
  ),
  );
}


