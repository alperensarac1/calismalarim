import 'package:flutter/material.dart';

import 'models/room_model.dart';
import 'screens/broadcaster_screen.dart';
import 'screens/home_screen.dart';
import 'screens/room_list_screen.dart';
import 'screens/viewer_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const CanliYayinApp());
}

class CanliYayinApp extends StatefulWidget {
  const CanliYayinApp({super.key});

  @override
  State<CanliYayinApp> createState() => _CanliYayinAppState();
}

class _CanliYayinAppState extends State<CanliYayinApp> {
  String currentScreen = "home";
  RoomModel? selectedRoom;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "Canlı Yayın Flutter",
      debugShowCheckedModeBanner: false,
      home: _buildScreen(),
    );
  }

  Widget _buildScreen() {
    if (currentScreen == "home") {
      return HomeScreen(
        onStartBroadcast: () {
          setState(() {
            currentScreen = "broadcaster";
          });
        },
        onWatchBroadcasts: () {
          setState(() {
            currentScreen = "room_list";
          });
        },
      );
    }

    if (currentScreen == "room_list") {
      return RoomListScreen(
        onBack: () {
          setState(() {
            currentScreen = "home";
          });
        },
        onRoomTap: (room) {
          setState(() {
            selectedRoom = room;
            currentScreen = "viewer";
          });
        },
      );
    }

    if (currentScreen == "viewer" && selectedRoom != null) {
      return ViewerScreen(
        room: selectedRoom!,
        onBack: () {
          setState(() {
            currentScreen = "room_list";
          });
        },
      );
    }

    if (currentScreen == "broadcaster") {
      return BroadcasterScreen(
        onBack: () {
          setState(() {
            currentScreen = "home";
          });
        },
      );
    }

    return const Scaffold(
      body: Center(
        child: Text("Ekran bulunamadı"),
      ),
    );
  }
}