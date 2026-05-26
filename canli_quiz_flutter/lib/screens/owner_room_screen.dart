import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';

import '../socket/socket_message_factory.dart';
import '../socket/web_socket_manager.dart';
import 'quiz_screen.dart';

class OwnerRoomScreen extends StatefulWidget {
  final String roomCode;
  final String username;
  final int questionTime;

  const OwnerRoomScreen({
    super.key,
    required this.roomCode,
    required this.username,
    required this.questionTime,
  });

  @override
  State<OwnerRoomScreen> createState() => _OwnerRoomScreenState();
}

class _OwnerRoomScreenState extends State<OwnerRoomScreen> {
  final TextEditingController _questionController = TextEditingController();

  StreamSubscription<String>? _socketSubscription;

  String _playersText = "Oyuncular bekleniyor...";
  String _statusText = "";
  int _questionCount = 0;

  final List<TextEditingController> _optionControllers = [
    TextEditingController(),
    TextEditingController(),
  ];

  int _selectedCorrectIndex = -1;

  @override
  void initState() {
    super.initState();

    _socketSubscription = WebSocketManager.instance.messages.listen(
      _handleSocketMessage,
      onError: (error) {
        setState(() {
          _statusText = "Bağlantı hatası: $error";
        });
      },
    );
  }

  @override
  void dispose() {
    _socketSubscription?.cancel();
    _questionController.dispose();

    for (final controller in _optionControllers) {
      controller.dispose();
    }

    super.dispose();
  }

  void _handleSocketMessage(String message) {
    final Map<String, dynamic> json = jsonDecode(message);
    final type = json["type"]?.toString() ?? "";

    if (type == "player_list_updated") {
      final players = json["players"];

      if (players is List) {
        setState(() {
          _playersText = _buildPlayersText(players);
        });
      }
    }

    if (type == "question_added") {
      setState(() {
        _questionCount = json["question_count"] is int
            ? json["question_count"]
            : _questionCount + 1;

        _statusText = json["message"]?.toString() ?? "Soru eklendi.";
      });

      _clearQuestionForm();
    }

    if (type == "room_question_count_updated") {
      setState(() {
        _questionCount = json["question_count"] is int
            ? json["question_count"]
            : _questionCount;
      });
    }

    if (type == "quiz_started") {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => QuizScreen(
            roomCode: widget.roomCode,
            username: widget.username,
            questionTime: widget.questionTime,
            isOwner: true,
          ),
        ),
      );
    }

    if (type == "error") {
      setState(() {
        _statusText = json["message"]?.toString() ?? "Bilinmeyen hata oluştu.";
      });
    }
  }

  String _buildPlayersText(List players) {
    if (players.isEmpty) {
      return "Oyuncular bekleniyor...";
    }

    final buffer = StringBuffer();
    buffer.writeln("Oyuncular:");
    buffer.writeln();

    for (int i = 0; i < players.length; i++) {
      buffer.writeln("${i + 1}. ${players[i]}");
    }

    return buffer.toString();
  }

  void _addOption() {
    setState(() {
      _optionControllers.add(TextEditingController());
    });
  }

  void _deleteOption(int index) {
    if (_optionControllers.length <= 2) {
      setState(() {
        _statusText = "En az 2 şık kalmalı.";
      });
      return;
    }

    setState(() {
      final removedController = _optionControllers.removeAt(index);
      removedController.dispose();

      if (_selectedCorrectIndex == index) {
        _selectedCorrectIndex = -1;
      } else if (_selectedCorrectIndex > index) {
        _selectedCorrectIndex--;
      }
    });
  }

  void _addQuestion() {
    final questionText = _questionController.text.trim();

    if (questionText.isEmpty) {
      setState(() {
        _statusText = "Soru metni boş olamaz.";
      });
      return;
    }

    if (_selectedCorrectIndex == -1) {
      setState(() {
        _statusText = "Doğru cevabı seçmelisin.";
      });
      return;
    }

    final List<String> filledOptions = [];
    int correctIndexInFilledOptions = -1;

    for (int i = 0; i < _optionControllers.length; i++) {
      final optionText = _optionControllers[i].text.trim();

      if (optionText.isNotEmpty) {
        if (i == _selectedCorrectIndex) {
          correctIndexInFilledOptions = filledOptions.length;
        }

        filledOptions.add(optionText);
      }
    }

    if (filledOptions.length < 2) {
      setState(() {
        _statusText = "En az 2 dolu şık girmelisin.";
      });
      return;
    }

    if (correctIndexInFilledOptions == -1) {
      setState(() {
        _statusText = "Doğru cevap olarak seçtiğin şık boş olamaz.";
      });
      return;
    }

    WebSocketManager.instance.send(
      SocketMessageFactory.addQuestion(
        roomCode: widget.roomCode,
        questionText: questionText,
        options: filledOptions,
        correctIndex: correctIndexInFilledOptions,
      ),
    );

    setState(() {
      _statusText = "Soru gönderildi...";
    });
  }

  void _startQuiz() {
    if (_questionCount <= 0) {
      setState(() {
        _statusText = "Quiz başlatmak için en az 1 soru eklemelisin.";
      });
      return;
    }

    WebSocketManager.instance.send(
      SocketMessageFactory.startQuiz(
        roomCode: widget.roomCode,
      ),
    );

    setState(() {
      _statusText = "Quiz başlatma isteği gönderildi...";
    });
  }

  void _clearQuestionForm() {
    _questionController.clear();

    for (final controller in _optionControllers) {
      controller.dispose();
    }

    setState(() {
      _optionControllers.clear();
      _optionControllers.add(TextEditingController());
      _optionControllers.add(TextEditingController());
      _selectedCorrectIndex = -1;
    });
  }

  String _indexToLetter(int index) {
    if (index >= 0 && index < 26) {
      return String.fromCharCode(65 + index);
    }

    return "${index + 1}";
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Oda Sahibi"),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "Oda Sahibi Paneli",
              style: TextStyle(
                fontSize: 27,
                fontWeight: FontWeight.w800,
                color: Color(0xFF111827),
              ),
            ),
            const SizedBox(height: 18),
            Text(
              "Oda Kodu: ${widget.roomCode}",
              style: const TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.w800,
                color: Color(0xFF6D28D9),
              ),
            ),
            const SizedBox(height: 14),
            Text(
              "Kullanıcı: ${widget.username}\n"
                  "Soru Süresi: ${widget.questionTime} saniye",
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF374151),
                height: 1.5,
              ),
            ),
            const SizedBox(height: 20),
            Text(
              _playersText,
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF111827),
                height: 1.5,
              ),
            ),
            const SizedBox(height: 24),
            const Divider(),
            const SizedBox(height: 24),
            const Text(
              "Soru Ekle",
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w800,
                color: Color(0xFF111827),
              ),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: _questionController,
              minLines: 3,
              maxLines: 5,
              decoration: const InputDecoration(
                labelText: "Soru metni",
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 20),
            const Text(
              "Şıklar",
              style: TextStyle(
                fontSize: 17,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 10),
            ...List.generate(_optionControllers.length, (index) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: Row(
                  children: [
                    Radio<int>(
                      value: index,
                      groupValue: _selectedCorrectIndex,
                      onChanged: (value) {
                        setState(() {
                          _selectedCorrectIndex = value ?? -1;
                        });
                      },
                    ),
                    Expanded(
                      child: TextField(
                        controller: _optionControllers[index],
                        decoration: InputDecoration(
                          labelText: "${_indexToLetter(index)} şıkkı",
                          border: const OutlineInputBorder(),
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    SizedBox(
                      height: 52,
                      child: OutlinedButton(
                        onPressed: _optionControllers.length <= 2
                            ? null
                            : () => _deleteOption(index),
                        child: const Text("Sil"),
                      ),
                    ),
                  ],
                ),
              );
            }),
            SizedBox(
              width: double.infinity,
              height: 52,
              child: OutlinedButton(
                onPressed: _addOption,
                child: const Text("+ Şık Ekle"),
              ),
            ),
            const SizedBox(height: 22),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: _addQuestion,
                child: const Text(
                  "Soruyu Ekle",
                  style: TextStyle(fontSize: 17),
                ),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: _startQuiz,
                child: const Text(
                  "Quizi Başlat",
                  style: TextStyle(fontSize: 17),
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              "Eklenen soru: $_questionCount",
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF374151),
              ),
            ),
            const SizedBox(height: 12),
            Text(
              _statusText,
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF374151),
              ),
            ),
          ],
        ),
      ),
    );
  }
}