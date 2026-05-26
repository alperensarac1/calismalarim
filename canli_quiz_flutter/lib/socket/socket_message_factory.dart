import 'dart:convert';

class SocketMessageFactory {
  static String createRoom({
    required String username,
    required int questionTime,
  }) {
    return jsonEncode({
      "type": "create_room",
      "username": username,
      "question_time": questionTime,
    });
  }

  static String joinRoom({
    required String roomCode,
    required String username,
  }) {
    return jsonEncode({
      "type": "join_room",
      "room_code": roomCode,
      "username": username,
    });
  }

  static String addQuestion({
    required String roomCode,
    required String questionText,
    required List<String> options,
    required int correctIndex,
  }) {
    return jsonEncode({
      "type": "add_question",
      "room_code": roomCode,
      "question_text": questionText,
      "options": options,
      "correct_index": correctIndex,
    });
  }

  static String startQuiz({
    required String roomCode,
  }) {
    return jsonEncode({
      "type": "start_quiz",
      "room_code": roomCode,
    });
  }

  static String submitAnswer({
    required String roomCode,
    required String username,
    required int answerIndex,
  }) {
    return jsonEncode({
      "type": "submit_answer",
      "room_code": roomCode,
      "username": username,
      "answer_index": answerIndex,
    });
  }
}