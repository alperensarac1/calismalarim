import 'package:flutter/material.dart';

import 'screens/room_list_screen.dart';

void main() {
  runApp(const SyncRadioApp());
}

class SyncRadioApp extends StatelessWidget {
  const SyncRadioApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "SyncRadio Flutter",
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
      ),
      home: const RoomListScreen(),
    );
  }
}