import 'package:flutter/material.dart';

import '../models/quiz_models.dart';
import '../socket/web_socket_manager.dart';
import 'home_screen.dart';

class WinnerScreen extends StatelessWidget {
  final List<ScoreItem> winners;
  final List<ScoreItem> scoreboard;

  const WinnerScreen({
    super.key,
    required this.winners,
    required this.scoreboard,
  });

  String _buildWinnersText() {
    if (winners.isEmpty) {
      return "Kazanan bulunamadı.";
    }

    final buffer = StringBuffer();

    for (int i = 0; i < winners.length; i++) {
      String medal = "";

      if (i == 0) {
        medal = "🥇";
      } else if (i == 1) {
        medal = "🥈";
      } else if (i == 2) {
        medal = "🥉";
      }

      buffer.writeln("$medal ${winners[i].username}");
      buffer.writeln("${winners[i].score} puan");
      buffer.writeln();
    }

    return buffer.toString().trim();
  }

  String _buildScoreboardText() {
    if (scoreboard.isEmpty) {
      return "Puan tablosu yok.";
    }

    final buffer = StringBuffer();
    buffer.writeln("Genel Sıralama:");
    buffer.writeln();

    for (int i = 0; i < scoreboard.length; i++) {
      buffer.writeln(
        "${i + 1}. ${scoreboard[i].username} - ${scoreboard[i].score} puan",
      );
    }

    return buffer.toString();
  }

  void _backHome(BuildContext context) {
    WebSocketManager.instance.disconnect();

    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(
        builder: (_) => const HomeScreen(),
      ),
          (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Sonuç"),
        automaticallyImplyLeading: false,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Center(
          child: Column(
            children: [
              const Text(
                "Quiz Bitti",
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.w800,
                  color: Color(0xFF111827),
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 14),
              const Text(
                "Bunlar Kazandı",
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.w800,
                  color: Color(0xFF6D28D9),
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 28),
              Text(
                _buildWinnersText(),
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w800,
                  color: Color(0xFF111827),
                  height: 1.4,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 30),
              Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  _buildScoreboardText(),
                  style: const TextStyle(
                    fontSize: 15,
                    color: Color(0xFF374151),
                    height: 1.5,
                  ),
                ),
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                height: 56,
                child: ElevatedButton(
                  onPressed: () => _backHome(context),
                  child: const Text(
                    "Ana Sayfaya Dön",
                    style: TextStyle(fontSize: 17),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}