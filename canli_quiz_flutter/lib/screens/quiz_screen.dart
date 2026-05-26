import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';

import '../models/quiz_models.dart';
import '../socket/socket_message_factory.dart';
import '../socket/web_socket_manager.dart';
import 'winner_screen.dart';

class QuizScreen extends StatefulWidget {
  final String roomCode;
  final String username;
  final int questionTime;
  final bool isOwner;

  const QuizScreen({
    super.key,
    required this.roomCode,
    required this.username,
    required this.questionTime,
    required this.isOwner,
  });

  @override
  State<QuizScreen> createState() => _QuizScreenState();
}

enum OptionVisualState {
  normal,
  waiting,
  correct,
  wrong,
}

class _QuizScreenState extends State<QuizScreen> {
  StreamSubscription<String>? _socketSubscription;
  Timer? _timer;

  QuestionData? _questionData;

  int _remainingTime = 20;
  int _selectedAnswerIndex = -1;
  int _currentCorrectIndex = -1;

  bool _answeredCurrentQuestion = false;

  String _answerResultText = "";
  String _scoreboardText = "Puan tablosu bekleniyor...";

  @override
  void initState() {
    super.initState();

    _remainingTime = widget.questionTime;

    _socketSubscription = WebSocketManager.instance.messages.listen(
      _handleSocketMessage,
      onError: (error) {
        setState(() {
          _answerResultText = "Bağlantı hatası: $error";
        });
      },
    );
  }

  @override
  void dispose() {
    _socketSubscription?.cancel();
    _timer?.cancel();
    super.dispose();
  }

  void _handleSocketMessage(String message) {
    final Map<String, dynamic> json = jsonDecode(message);
    final type = json["type"]?.toString() ?? "";

    if (type == "new_question") {
      _handleNewQuestion(json);
    }

    if (type == "answer_result") {
      _handleAnswerResult(json);
    }

    if (type == "scoreboard_updated") {
      final scoreboard = json["scoreboard"];
      setState(() {
        _scoreboardText = _buildScoreboardText(
          scoreboard is List ? scoreboard : [],
        );
      });
    }

    if (type == "time_up") {
      _handleTimeUp(json);
    }

    if (type == "quiz_finished") {
      _timer?.cancel();

      final winnersRaw = json["winners"];
      final scoreboardRaw = json["scoreboard"];

      final winners = _parseScoreList(winnersRaw is List ? winnersRaw : []);
      final scoreboard = _parseScoreList(scoreboardRaw is List ? scoreboardRaw : []);

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => WinnerScreen(
            winners: winners,
            scoreboard: scoreboard,
          ),
        ),
      );
    }

    if (type == "answer_rejected") {
      setState(() {
        _answerResultText = json["message"]?.toString() ?? "Cevap reddedildi.";
      });
    }

    if (type == "error") {
      setState(() {
        _answerResultText = json["message"]?.toString() ?? "Bilinmeyen hata oluştu.";
      });
    }
  }

  void _handleNewQuestion(Map<String, dynamic> json) {
    _timer?.cancel();

    final question = QuestionData.fromJson(json);

    setState(() {
      _questionData = question;
      _remainingTime = question.questionTime;
      _selectedAnswerIndex = -1;
      _currentCorrectIndex = -1;
      _answeredCurrentQuestion = false;
      _answerResultText = "";
      _scoreboardText = _buildScoreboardText(
        json["scoreboard"] is List ? json["scoreboard"] : [],
      );
    });

    _startLocalTimer(question.questionTime);
  }

  void _handleAnswerResult(Map<String, dynamic> json) {
    final isCorrect = json["is_correct"] == true;
    final earnedScore = json["earned_score"] is int ? json["earned_score"] : 0;
    final totalScore = json["total_score"] is int ? json["total_score"] : 0;

    setState(() {
      if (isCorrect) {
        _answerResultText =
        "Doğru cevap! +$earnedScore puan | Toplam: $totalScore";
      } else {
        _answerResultText = "Yanlış cevap. Puan kazanamadın.";
      }
    });
  }

  void _handleTimeUp(Map<String, dynamic> json) {
    _timer?.cancel();

    final correctIndex =
    json["correct_index"] is int ? json["correct_index"] : -1;

    setState(() {
      _remainingTime = 0;
      _currentCorrectIndex = correctIndex;

      if (correctIndex >= 0) {
        _answerResultText =
        "Süre bitti. Doğru cevap: ${_indexToLetter(correctIndex)}";
      } else {
        _answerResultText = "Süre bitti.";
      }

      _scoreboardText = _buildScoreboardText(
        json["scoreboard"] is List ? json["scoreboard"] : [],
      );
    });
  }

  void _startLocalTimer(int seconds) {
    _timer?.cancel();

    int remaining = seconds;

    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      remaining--;

      if (!mounted) return;

      if (remaining <= 0) {
        setState(() {
          _remainingTime = 0;
        });

        timer.cancel();
      } else {
        setState(() {
          _remainingTime = remaining;
        });
      }
    });
  }

  void _submitAnswer(int answerIndex) {
    if (_answeredCurrentQuestion) {
      setState(() {
        _answerResultText = "Bu soruya zaten cevap verdin.";
      });
      return;
    }

    setState(() {
      _answeredCurrentQuestion = true;
      _selectedAnswerIndex = answerIndex;
      _answerResultText = "Cevabın gönderildi...";
    });

    WebSocketManager.instance.send(
      SocketMessageFactory.submitAnswer(
        roomCode: widget.roomCode,
        username: widget.username,
        answerIndex: answerIndex,
      ),
    );
  }

  List<ScoreItem> _parseScoreList(List rawList) {
    return rawList
        .whereType<Map>()
        .map((item) => ScoreItem.fromJson(Map<String, dynamic>.from(item)))
        .toList();
  }

  String _buildScoreboardText(List rawList) {
    if (rawList.isEmpty) {
      return "Puan tablosu bekleniyor...";
    }

    final scores = _parseScoreList(rawList);

    final buffer = StringBuffer();
    buffer.writeln("Puan Tablosu:");
    buffer.writeln();

    for (int i = 0; i < scores.length; i++) {
      buffer.writeln(
        "${i + 1}. ${scores[i].username} - ${scores[i].score} puan",
      );
    }

    return buffer.toString();
  }

  OptionVisualState _getOptionVisualState(int index) {
    if (_currentCorrectIndex >= 0 && index == _currentCorrectIndex) {
      return OptionVisualState.correct;
    }

    if (_currentCorrectIndex >= 0 &&
        _selectedAnswerIndex >= 0 &&
        index == _selectedAnswerIndex &&
        _selectedAnswerIndex != _currentCorrectIndex) {
      return OptionVisualState.wrong;
    }

    if (_answeredCurrentQuestion &&
        _selectedAnswerIndex == index &&
        _currentCorrectIndex == -1) {
      return OptionVisualState.waiting;
    }

    return OptionVisualState.normal;
  }

  Color _optionBackground(OptionVisualState state) {
    switch (state) {
      case OptionVisualState.waiting:
        return const Color(0xFFFEF3C7);
      case OptionVisualState.correct:
        return const Color(0xFFDCFCE7);
      case OptionVisualState.wrong:
        return const Color(0xFFFEE2E2);
      case OptionVisualState.normal:
        return Colors.white;
    }
  }

  Color _optionBorder(OptionVisualState state) {
    switch (state) {
      case OptionVisualState.waiting:
        return const Color(0xFFF59E0B);
      case OptionVisualState.correct:
        return const Color(0xFF16A34A);
      case OptionVisualState.wrong:
        return const Color(0xFFDC2626);
      case OptionVisualState.normal:
        return const Color(0xFFD1D5DB);
    }
  }

  Color _optionTextColor(OptionVisualState state) {
    switch (state) {
      case OptionVisualState.waiting:
        return const Color(0xFF92400E);
      case OptionVisualState.correct:
        return const Color(0xFF166534);
      case OptionVisualState.wrong:
        return const Color(0xFF991B1B);
      case OptionVisualState.normal:
        return const Color(0xFF111827);
    }
  }

  String _indexToLetter(int index) {
    if (index >= 0 && index < 26) {
      return String.fromCharCode(65 + index);
    }

    return "${index + 1}";
  }

  @override
  Widget build(BuildContext context) {
    final question = _questionData;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Quiz"),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: question == null
            ? const Text(
          "Soru bekleniyor...",
          style: TextStyle(
            fontSize: 18,
            color: Color(0xFF374151),
          ),
        )
            : Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "Quiz",
              style: TextStyle(
                fontSize: 27,
                fontWeight: FontWeight.w800,
                color: Color(0xFF111827),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              "Soru ${question.questionNumber} / ${question.totalQuestions}",
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF6B7280),
              ),
            ),
            const SizedBox(height: 18),
            Text(
              _remainingTime > 0
                  ? "Süre: $_remainingTime"
                  : "Süre bitti",
              style: const TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.w800,
                color: Color(0xFFDC2626),
              ),
            ),
            const SizedBox(height: 22),
            Text(
              question.questionText,
              style: const TextStyle(
                fontSize: 21,
                fontWeight: FontWeight.w800,
                color: Color(0xFF111827),
                height: 1.35,
              ),
            ),
            const SizedBox(height: 22),
            ...List.generate(question.options.length, (index) {
              final state = _getOptionVisualState(index);

              return Padding(
                padding: const EdgeInsets.only(bottom: 14),
                child: SizedBox(
                  width: double.infinity,
                  child: OutlinedButton(
                    style: OutlinedButton.styleFrom(
                      backgroundColor: _optionBackground(state),
                      side: BorderSide(
                        color: _optionBorder(state),
                        width: 1,
                      ),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14),
                      ),
                    ),
                    onPressed: _answeredCurrentQuestion ||
                        _remainingTime <= 0
                        ? null
                        : () => _submitAnswer(index),
                    child: Text(
                      "${_indexToLetter(index)}) ${question.options[index]}",
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w800,
                        color: _optionTextColor(state),
                      ),
                    ),
                  ),
                ),
              );
            }),
            const SizedBox(height: 22),
            Text(
              _answerResultText,
              style: const TextStyle(
                fontSize: 17,
                fontWeight: FontWeight.w800,
                color: Color(0xFF374151),
              ),
            ),
            const SizedBox(height: 24),
            Text(
              _scoreboardText,
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF111827),
                height: 1.5,
              ),
            ),
          ],
        ),
      ),
    );
  }
}