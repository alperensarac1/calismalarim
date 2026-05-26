class ScoreItem {
  final String username;
  final int score;

  ScoreItem({
    required this.username,
    required this.score,
  });

  factory ScoreItem.fromJson(Map<String, dynamic> json) {
    return ScoreItem(
      username: json["username"]?.toString() ?? "-",
      score: json["score"] is int
          ? json["score"]
          : int.tryParse(json["score"]?.toString() ?? "0") ?? 0,
    );
  }
}

class QuestionData {
  final int questionNumber;
  final int totalQuestions;
  final String questionText;
  final List<String> options;
  final int questionTime;

  QuestionData({
    required this.questionNumber,
    required this.totalQuestions,
    required this.questionText,
    required this.options,
    required this.questionTime,
  });

  factory QuestionData.fromJson(Map<String, dynamic> json) {
    final rawOptions = json["options"];

    List<String> parsedOptions = [];

    if (rawOptions is List) {
      parsedOptions = rawOptions.map((e) => e.toString()).toList();
    }

    return QuestionData(
      questionNumber: json["question_number"] is int
          ? json["question_number"]
          : int.tryParse(json["question_number"]?.toString() ?? "0") ?? 0,
      totalQuestions: json["total_questions"] is int
          ? json["total_questions"]
          : int.tryParse(json["total_questions"]?.toString() ?? "0") ?? 0,
      questionText: json["question_text"]?.toString() ?? "",
      options: parsedOptions,
      questionTime: json["question_time"] is int
          ? json["question_time"]
          : int.tryParse(json["question_time"]?.toString() ?? "20") ?? 20,
    );
  }
}